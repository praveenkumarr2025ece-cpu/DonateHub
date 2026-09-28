package com.example.DontaeHub.DonatedItem.Service;

import com.example.DontaeHub.DonatedItem.Repository.DonatedItemRepository;
import com.example.DontaeHub.DonatedItem.entity.DonatedItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonatedItemService {

    private final DonatedItemRepository donatedItemRepository;

    public DonatedItemService(DonatedItemRepository donatedItemRepository) {
        this.donatedItemRepository = donatedItemRepository;
    }


    public DonatedItem addDonatedItem(DonatedItem donatedItem) {
        return donatedItemRepository.save(donatedItem);
    }

    public List<DonatedItem> getAllDonatedItems() {
        return donatedItemRepository.findAll();
    }

    public Optional<DonatedItem> getDonatedItemById(Long id) {
        return donatedItemRepository.findById(id);
    }


    public DonatedItem updateDonatedItem(Long id, DonatedItem updatedItem) {

        DonatedItem existingItem =
                donatedItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Donated item not found"));

        existingItem.setName(updatedItem.getName());
        existingItem.setCategory(updatedItem.getCategory());
        existingItem.setItemCondition(updatedItem.getItemCondition());
        existingItem.setQuantity(updatedItem.getQuantity());
        existingItem.setDonorId(updatedItem.getDonorId());
        existingItem.setDriveId(updatedItem.getDriveId());

        return donatedItemRepository.save(existingItem);
    }

    public void deleteDonatedItem(Long id) {
        donatedItemRepository.deleteById(id);
    }
}