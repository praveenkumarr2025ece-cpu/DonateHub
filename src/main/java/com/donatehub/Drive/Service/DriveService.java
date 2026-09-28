package com.donatehub.Drive.Service;

import com.donatehub.DonatedItem.Repository.DonatedItemRepository;
import com.donatehub.DonatedItem.entity.DonatedItem;
import com.donatehub.Drive.Repository.DriveRepository;
import com.donatehub.Drive.entity.Drive;
import com.donatehub.Recipient.Repository.RecipientRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriveService {

    private final DriveRepository driveRepository;
    private final RecipientRepository recipientRepository;
    private final DonatedItemRepository donatedItemRepository;

    public DriveService(DriveRepository driveRepository,
                        RecipientRepository recipientRepository,
                        DonatedItemRepository donatedItemRepository) {
        this.driveRepository = driveRepository;
        this.recipientRepository = recipientRepository;
        this.donatedItemRepository = donatedItemRepository;
    }

    public Drive createDrive(Drive drive) {
        validateDrive(drive);
        return driveRepository.save(drive);
    }

    public List<Drive> getAllDrives() {
        return driveRepository.findAll();
    }

    public Drive getDriveById(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drive not found with id: " + id));
    }

    public List<DonatedItem> getDriveItems(Long driveId) {
        getDriveById(driveId); // ensures drive exists
        return donatedItemRepository.findByDriveId(driveId);
    }

    public Map<String, Object> getDriveSummary(Long driveId) {
        Drive drive = getDriveById(driveId);
        List<DonatedItem> items = donatedItemRepository.findByDriveId(driveId);

        int totalQuantity = items.stream().mapToInt(DonatedItem::getQuantity).sum();
        int distributedQuantity = items.stream().mapToInt(DonatedItem::getDistributedQuantity).sum();
        int remainingQuantity = totalQuantity - distributedQuantity;

        Map<String, Object> summary = new HashMap<>();
        summary.put("driveId", drive.getId());
        summary.put("driveName", drive.getName());
        summary.put("itemCount", items.size());
        summary.put("totalQuantity", totalQuantity);
        summary.put("distributedQuantity", distributedQuantity);
        summary.put("remainingQuantity", remainingQuantity);

        return summary;
    }

    public Drive updateDrive(Long id, Drive updatedDrive) {
        Drive existingDrive = getDriveById(id);
        validateDrive(updatedDrive);

        existingDrive.setName(updatedDrive.getName());
        existingDrive.setStartDate(updatedDrive.getStartDate());
        existingDrive.setEndDate(updatedDrive.getEndDate());
        existingDrive.setDescription(updatedDrive.getDescription());
        existingDrive.setRecipientId(updatedDrive.getRecipientId());

        return driveRepository.save(existingDrive);
    }

    public void deleteDrive(Long id) {
        Drive existingDrive = getDriveById(id);
        driveRepository.delete(existingDrive);
    }

    private void validateDrive(Drive drive) {
        if (drive.getStartDate() == null || drive.getEndDate() == null) {
            throw new IllegalArgumentException("Drive must have valid start and end dates");
        }
        if (drive.getEndDate().isBefore(drive.getStartDate())) {
            throw new IllegalArgumentException("Drive end date must not be before start date");
        }
        if (drive.getRecipientId() == null) {
            throw new IllegalArgumentException("Drive must have a recipientId");
        }
        if (!recipientRepository.existsById(drive.getRecipientId())) {
            throw new IllegalArgumentException("Recipient with ID " + drive.getRecipientId() + " does not exist");
        }
    }
}
