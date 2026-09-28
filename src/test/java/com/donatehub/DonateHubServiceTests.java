package com.donatehub;

import com.donatehub.DonatedItem.Repository.DonatedItemRepository;
import com.donatehub.DonatedItem.Service.DonatedItemService;
import com.donatehub.DonatedItem.entity.DonatedItem;
import com.donatehub.Donor.Repository.DonorRepository;
import com.donatehub.Donor.Service.DonorService;
import com.donatehub.Donor.entity.Donor;
import com.donatehub.Drive.Repository.DriveRepository;
import com.donatehub.Drive.Service.DriveService;
import com.donatehub.Drive.entity.Drive;
import com.donatehub.Recipient.Repository.RecipientRepository;
import com.donatehub.Recipient.Service.RecipientService;
import com.donatehub.Recipient.entity.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class DonateHubServiceTests {

    private final Map<Long, Donor> donorDb = new ConcurrentHashMap<>();
    private final Map<Long, Recipient> recipientDb = new ConcurrentHashMap<>();
    private final Map<Long, Drive> driveDb = new ConcurrentHashMap<>();
    private final Map<Long, DonatedItem> donatedItemDb = new ConcurrentHashMap<>();

    private final AtomicLong donorSeq = new AtomicLong(1);
    private final AtomicLong recipientSeq = new AtomicLong(1);
    private final AtomicLong driveSeq = new AtomicLong(1);
    private final AtomicLong donatedItemSeq = new AtomicLong(1);

    private DonorService donorService;
    private RecipientService recipientService;
    private DriveService driveService;
    private DonatedItemService donatedItemService;

    @BeforeEach
    void setUp() {
        donorDb.clear();
        recipientDb.clear();
        driveDb.clear();
        donatedItemDb.clear();

        DonorRepository donorRepo = createProxy(DonorRepository.class, donorDb, donorSeq);
        RecipientRepository recipientRepo = createProxy(RecipientRepository.class, recipientDb, recipientSeq);
        DriveRepository driveRepo = createProxy(DriveRepository.class, driveDb, driveSeq);
        DonatedItemRepository itemRepo = createItemRepoProxy(donatedItemDb, donatedItemSeq);

        donorService = new DonorService(donorRepo);
        recipientService = new RecipientService(recipientRepo);
        driveService = new DriveService(driveRepo, recipientRepo, itemRepo);
        donatedItemService = new DonatedItemService(itemRepo, donorRepo, driveRepo);
    }

    @Test
    void testDonorServiceCrud() {
        Donor donor = new Donor(null, "Alice", "alice@example.com", "1234567890", "123 Main St");
        Donor saved = donorService.addDonor(donor);
        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());

        List<Donor> list = donorService.getAllDonors();
        assertEquals(1, list.size());

        Donor found = donorService.getDonorById(saved.getId());
        assertEquals("alice@example.com", found.getEmail());

        donor.setName("Alice Updated");
        Donor updated = donorService.updateDonor(saved.getId(), donor);
        assertEquals("Alice Updated", updated.getName());

        donorService.deleteDonor(saved.getId());
        assertEquals(0, donorService.getAllDonors().size());
    }

    @Test
    void testRecipientServiceCrud() {
        Recipient recipient = new Recipient(null, "John", "Hope Foundation", "John Doe", "9876543210", "456 Oak Ave");
        Recipient saved = recipientService.addRecipient(recipient);
        assertNotNull(saved.getId());
        assertEquals("Hope Foundation", saved.getOrganizationName());

        Recipient found = recipientService.getRecipientById(saved.getId());
        assertEquals("John", found.getName());

        recipient.setPhone("1112223333");
        Recipient updated = recipientService.updateRecipient(saved.getId(), recipient);
        assertEquals("1112223333", updated.getPhone());

        recipientService.deleteRecipient(saved.getId());
        assertEquals(0, recipientService.getAllRecipients().size());
    }

    @Test
    void testDriveServiceValidationAndSummary() {
        // First add a recipient so foreign key constraint passes
        Recipient recipient = recipientService.addRecipient(new Recipient(null, "Jane", "Care Org", "Jane Doe", "5551234567", "789 Pine St"));

        Drive drive = new Drive(null, "Winter Warmth", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), "Clothes collection", recipient.getId());
        Drive saved = driveService.createDrive(drive);
        assertNotNull(saved.getId());
        assertEquals("Winter Warmth", saved.getName());

        // Test date validation: endDate before startDate
        Drive invalidDates = new Drive(null, "Invalid", LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1), "Desc", recipient.getId());
        assertThrows(IllegalArgumentException.class, () -> driveService.createDrive(invalidDates));

        // Test non-existing recipient
        Drive invalidRecipient = new Drive(null, "Invalid", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15), "Desc", 9999L);
        assertThrows(IllegalArgumentException.class, () -> driveService.createDrive(invalidRecipient));

        // Add items to drive and test stock summary
        Donor donor = donorService.addDonor(new Donor(null, "Bob", "bob@example.com", "9998887777", "Street 1"));
        donatedItemService.addDonatedItem(new DonatedItem(null, "Jackets", "Clothes", "Good", 50, 20, donor.getId(), saved.getId()));
        donatedItemService.addDonatedItem(new DonatedItem(null, "Math Books", "Books", "New", 30, 10, donor.getId(), saved.getId()));

        Map<String, Object> summary = driveService.getDriveSummary(saved.getId());
        assertEquals(80, summary.get("totalQuantity"));
        assertEquals(30, summary.get("distributedQuantity"));
        assertEquals(50, summary.get("remainingQuantity"));
    }

    @Test
    void testDonatedItemValidationAndDistribution() {
        Donor donor = donorService.addDonor(new Donor(null, "Charlie", "charlie@example.com", "3334445555", "Road 2"));
        Recipient recipient = recipientService.addRecipient(new Recipient(null, "Community Helpers", "Helpers Org", "Mary", "6667778888", "Avenue 3"));
        Drive drive = driveService.createDrive(new Drive(null, "Book Drive", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), "Books", recipient.getId()));

        DonatedItem item = new DonatedItem(null, "Science Books", "Books", "Good", 20, 0, donor.getId(), drive.getId());
        DonatedItem saved = donatedItemService.addDonatedItem(item);
        assertNotNull(saved.getId());
        assertEquals(20, saved.getQuantity());

        // Distribute 5 items
        DonatedItem distributed = donatedItemService.distributeItem(saved.getId(), 5);
        assertEquals(5, distributed.getDistributedQuantity());

        // Distribute more than remaining should fail (remaining is 15)
        assertThrows(IllegalArgumentException.class, () -> donatedItemService.distributeItem(saved.getId(), 16));

        // Invalid category should fail
        DonatedItem invalidCategory = new DonatedItem(null, "Food", "Food", "New", 10, 0, donor.getId(), drive.getId());
        assertThrows(IllegalArgumentException.class, () -> donatedItemService.addDonatedItem(invalidCategory));

        // Invalid condition should fail
        DonatedItem invalidCondition = new DonatedItem(null, "Pants", "Clothes", "Damaged", 10, 0, donor.getId(), drive.getId());
        assertThrows(IllegalArgumentException.class, () -> donatedItemService.addDonatedItem(invalidCondition));

        // Quantity <= 0 should fail
        DonatedItem zeroQty = new DonatedItem(null, "Pants", "Clothes", "Good", 0, 0, donor.getId(), drive.getId());
        assertThrows(IllegalArgumentException.class, () -> donatedItemService.addDonatedItem(zeroQty));
    }

    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> repoInterface, Map<Long, ?> db, AtomicLong seq) {
        return (T) Proxy.newProxyInstance(
                repoInterface.getClassLoader(),
                new Class<?>[]{repoInterface},
                new CrudInvocationHandler((Map<Long, Object>) db, seq)
        );
    }

    private DonatedItemRepository createItemRepoProxy(Map<Long, DonatedItem> db, AtomicLong seq) {
        return (DonatedItemRepository) Proxy.newProxyInstance(
                DonatedItemRepository.class.getClassLoader(),
                new Class<?>[]{DonatedItemRepository.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("findByDriveId".equals(methodName)) {
                        Long driveId = (Long) args[0];
                        return db.values().stream().filter(i -> Objects.equals(i.getDriveId(), driveId)).toList();
                    }
                    if ("findByDonorId".equals(methodName)) {
                        Long donorId = (Long) args[0];
                        return db.values().stream().filter(i -> Objects.equals(i.getDonorId(), donorId)).toList();
                    }
                    return new CrudInvocationHandler((Map<Long, Object>) (Map<?, ?>) db, seq).invoke(proxy, method, args);
                }
        );
    }

    private static class CrudInvocationHandler implements InvocationHandler {
        private final Map<Long, Object> db;
        private final AtomicLong seq;

        public CrudInvocationHandler(Map<Long, Object> db, AtomicLong seq) {
            this.db = db;
            this.seq = seq;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("save".equals(name)) {
                Object entity = args[0];
                Method getId = entity.getClass().getMethod("getId");
                Long id = (Long) getId.invoke(entity);
                if (id == null) {
                    id = seq.getAndIncrement();
                    Method setId = entity.getClass().getMethod("setId", Long.class);
                    setId.invoke(entity, id);
                }
                db.put(id, entity);
                return entity;
            } else if ("findById".equals(name)) {
                Long id = (Long) args[0];
                return Optional.ofNullable(db.get(id));
            } else if ("findAll".equals(name)) {
                return new ArrayList<>(db.values());
            } else if ("existsById".equals(name)) {
                Long id = (Long) args[0];
                return db.containsKey(id);
            } else if ("delete".equals(name)) {
                Object entity = args[0];
                Method getId = entity.getClass().getMethod("getId");
                Long id = (Long) getId.invoke(entity);
                db.remove(id);
                return null;
            } else if ("deleteById".equals(name)) {
                Long id = (Long) args[0];
                db.remove(id);
                return null;
            }
            return null;
        }
    }
}
