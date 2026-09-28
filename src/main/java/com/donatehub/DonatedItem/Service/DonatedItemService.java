package com.donatehub.DonatedItem.Service;

import com.donatehub.DonatedItem.Repository.DonatedItemRepository;
import com.donatehub.DonatedItem.entity.DonatedItem;
import com.donatehub.Donor.Repository.DonorRepository;
import com.donatehub.Drive.Repository.DriveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonatedItemService {

    private final DonatedItemRepository donatedItemRepository;
    private final DonorRepository donorRepository;
    private final DriveRepository driveRepository;

    public DonatedItemService(DonatedItemRepository donatedItemRepository,
                              DonorRepository donorRepository,
                              DriveRepository driveRepository) {
        this.donatedItemRepository = donatedItemRepository;
        this.donorRepository = donorRepository;
        this.driveRepository = driveRepository;
    }

    public DonatedItem addDonatedItem(DonatedItem item) {
        validateItem(item);
        return donatedItemRepository.save(item);
    }

    public List<DonatedItem> getAllDonatedItems() {
        return donatedItemRepository.findAll();
    }

    public DonatedItem getDonatedItemById(Long id) {
        return donatedItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donated item not found with id: " + id));
    }

    public List<DonatedItem> getItemsByDriveId(Long driveId) {
        return donatedItemRepository.findByDriveId(driveId);
    }

    public DonatedItem updateDonatedItem(Long id, DonatedItem updatedItem) {
        DonatedItem existingItem = getDonatedItemById(id);
        validateItem(updatedItem);

        existingItem.setName(updatedItem.getName());
        existingItem.setCategory(updatedItem.getCategory());
        existingItem.setCondition(updatedItem.getCondition());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setDistributedQuantity(updatedItem.getDistributedQuantity());
        existingItem.setDonorId(updatedItem.getDonorId());
        existingItem.setDriveId(updatedItem.getDriveId());

        return donatedItemRepository.save(existingItem);
    }

    public DonatedItem distributeItem(Long id, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Distribution amount must be greater than 0");
        }
        DonatedItem item = getDonatedItemById(id);
        int remaining = item.getQuantity() - item.getDistributedQuantity();
        if (amount > remaining) {
            throw new IllegalArgumentException("Cannot distribute more than remaining quantity. Remaining: " + remaining);
        }
        item.setDistributedQuantity(item.getDistributedQuantity() + amount);
        return donatedItemRepository.save(item);
    }

    public void deleteDonatedItem(Long id) {
        DonatedItem existingItem = getDonatedItemById(id);
        donatedItemRepository.delete(existingItem);
    }

    private void validateItem(DonatedItem item) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (item.getDistributedQuantity() < 0) {
            throw new IllegalArgumentException("Distributed quantity cannot be negative");
        }
        if (item.getDistributedQuantity() > item.getQuantity()) {
            throw new IllegalArgumentException("Distributed quantity cannot exceed total quantity");
        }
        if (item.getCategory() == null || (!item.getCategory().equalsIgnoreCase("Clothes") && !item.getCategory().equalsIgnoreCase("Books"))) {
            throw new IllegalArgumentException("Category must be either Clothes or Books");
        }
        if (item.getCondition() == null || (!item.getCondition().equalsIgnoreCase("New") && !item.getCondition().equalsIgnoreCase("Good") && !item.getCondition().equalsIgnoreCase("Worn"))) {
            throw new IllegalArgumentException("Condition must be New, Good, or Worn");
        }
        if (item.getDonorId() == null) {
            throw new IllegalArgumentException("Donated item must have a donorId");
        }
        if (!donorRepository.existsById(item.getDonorId())) {
            throw new IllegalArgumentException("Donor with ID " + item.getDonorId() + " does not exist");
        }
        if (item.getDriveId() == null) {
            throw new IllegalArgumentException("Donated item must have a driveId");
        }
        if (!driveRepository.existsById(item.getDriveId())) {
            throw new IllegalArgumentException("Drive with ID " + item.getDriveId() + " does not exist");
        }
    }
}
