package com.donatehub;

import com.donatehub.DonatedItem.Controller.DonatedItemController;
import com.donatehub.DonatedItem.Repository.DonatedItemRepository;
import com.donatehub.DonatedItem.Service.DonatedItemService;
import com.donatehub.DonatedItem.entity.DonatedItem;
import com.donatehub.Donor.Controller.DonorController;
import com.donatehub.Donor.Repository.DonorRepository;
import com.donatehub.Donor.Service.DonorService;
import com.donatehub.Donor.entity.Donor;
import com.donatehub.Drive.Controller.DriveController;
import com.donatehub.Drive.Repository.DriveRepository;
import com.donatehub.Drive.Service.DriveService;
import com.donatehub.Drive.entity.Drive;
import com.donatehub.Recipient.Controller.RecipientController;
import com.donatehub.Recipient.Repository.RecipientRepository;
import com.donatehub.Recipient.Service.RecipientService;
import com.donatehub.Recipient.entity.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DonateHubControllerTests {

    private final Map<Long, Donor> donorDb = new ConcurrentHashMap<>();
    private final Map<Long, Recipient> recipientDb = new ConcurrentHashMap<>();
    private final Map<Long, Drive> driveDb = new ConcurrentHashMap<>();
    private final Map<Long, DonatedItem> donatedItemDb = new ConcurrentHashMap<>();

    private final AtomicLong donorSeq = new AtomicLong(1);
    private final AtomicLong recipientSeq = new AtomicLong(1);
    private final AtomicLong driveSeq = new AtomicLong(1);
    private final AtomicLong donatedItemSeq = new AtomicLong(1);

    private MockMvc donorMockMvc;
    private MockMvc recipientMockMvc;
    private MockMvc driveMockMvc;
    private MockMvc itemMockMvc;

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

        DonorService donorService = new DonorService(donorRepo);
        RecipientService recipientService = new RecipientService(recipientRepo);
        DriveService driveService = new DriveService(driveRepo, recipientRepo, itemRepo);
        DonatedItemService donatedItemService = new DonatedItemService(itemRepo, donorRepo, driveRepo);

        donorMockMvc = MockMvcBuilders.standaloneSetup(new DonorController(donorService)).build();
        recipientMockMvc = MockMvcBuilders.standaloneSetup(new RecipientController(recipientService)).build();
        driveMockMvc = MockMvcBuilders.standaloneSetup(new DriveController(driveService)).build();
        itemMockMvc = MockMvcBuilders.standaloneSetup(new DonatedItemController(donatedItemService)).build();
    }

    @Test
    void testDonorRestCrud() throws Exception {
        // 1. Create donor
        String createJson = """
                {
                    "name": "Jane Doe",
                    "email": "jane@example.com",
                    "phone": "555-0100",
                    "address": "123 Elm St"
                }
                """;
        donorMockMvc.perform(post("/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Jane Doe")));

        // 2. Get all donors
        donorMockMvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Get donor by id
        donorMockMvc.perform(get("/donors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("jane@example.com")));

        // 4. Update donor
        String updateJson = """
                {
                    "name": "Jane Smith",
                    "email": "jane.smith@example.com",
                    "phone": "555-0100",
                    "address": "456 Pine St"
                }
                """;
        donorMockMvc.perform(put("/donors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Smith")));

        // 5. Delete donor
        donorMockMvc.perform(delete("/donors/1"))
                .andExpect(status().isOk());

        donorMockMvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testRecipientRestCrud() throws Exception {
        // 1. Create recipient
        String createJson = """
                {
                    "name": "Shelter Hope",
                    "organizationName": "Hope Org",
                    "contactPerson": "Mark Taylor",
                    "phone": "555-0200",
                    "address": "789 Broadway"
                }
                """;
        recipientMockMvc.perform(post("/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.organizationName", is("Hope Org")));

        // 2. Get all
        recipientMockMvc.perform(get("/recipients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 3. Update
        String updateJson = """
                {
                    "name": "Shelter Hope Center",
                    "organizationName": "Hope Org",
                    "contactPerson": "Mark Taylor",
                    "phone": "555-9999",
                    "address": "789 Broadway"
                }
                """;
        recipientMockMvc.perform(put("/recipients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone", is("555-9999")));

        // 4. Delete
        recipientMockMvc.perform(delete("/recipients/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDriveAndItemRestCrudAndDistribution() throws Exception {
        // Setup recipient and donor directly in DB
        recipientDb.put(1L, new Recipient(1L, "Recipient Center", "Center Org", "Alice", "123", "Addr"));
        donorDb.put(1L, new Donor(1L, "David", "david@example.com", "555-0300", "Street"));

        // 1. Create Drive
        String driveJson = """
                {
                    "name": "Book Drive 2026",
                    "startDate": "2026-10-01",
                    "endDate": "2026-10-15",
                    "description": "Collection of academic books",
                    "recipientId": 1
                }
                """;
        driveMockMvc.perform(post("/drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(driveJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Book Drive 2026")));

        // 2. Create Donated Item
        String itemJson = """
                {
                    "name": "History Books",
                    "category": "Books",
                    "condition": "New",
                    "quantity": 40,
                    "distributedQuantity": 0,
                    "donorId": 1,
                    "driveId": 1
                }
                """;
        itemMockMvc.perform(post("/donated-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.quantity", is(40)))
                .andExpect(jsonPath("$.distributedQuantity", is(0)));

        // 3. Distribute items: distribute 15 items
        itemMockMvc.perform(put("/donated-items/1/distribute?quantity=15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distributedQuantity", is(15)));

        // 4. Verify drive items endpoint
        driveMockMvc.perform(get("/drives/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("History Books")));

        // 5. Verify drive summary endpoint
        driveMockMvc.perform(get("/drives/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity", is(40)))
                .andExpect(jsonPath("$.distributedQuantity", is(15)))
                .andExpect(jsonPath("$.remainingQuantity", is(25)));

        // 6. Delete item
        itemMockMvc.perform(delete("/donated-items/1"))
                .andExpect(status().isOk());

        // 7. Delete drive
        driveMockMvc.perform(delete("/drives/1"))
                .andExpect(status().isOk());
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
