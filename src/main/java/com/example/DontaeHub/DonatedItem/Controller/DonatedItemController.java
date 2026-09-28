package com.example.DontaeHub.DonatedItem.Controller;

import com.example.DontaeHub.DonatedItem.Service.DonatedItemService;
import com.example.DontaeHub.DonatedItem.entity.DonatedItem;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/donated-items")
public class DonatedItemController {

    private final DonatedItemService donatedItemService;

    public DonatedItemController(DonatedItemService donatedItemService) {
        this.donatedItemService = donatedItemService;
    }


    @PostMapping
    public DonatedItem addDonatedItem(
            @RequestBody DonatedItem donatedItem) {

        return donatedItemService.addDonatedItem(donatedItem);
    }


    @GetMapping
    public List<DonatedItem> getAllDonatedItems() {

        return donatedItemService.getAllDonatedItems();
    }

    @GetMapping("/{id}")
    public Optional<DonatedItem> getDonatedItemById(
            @PathVariable Long id) {

        return donatedItemService.getDonatedItemById(id);
    }


    @PutMapping("/{id}")
    public DonatedItem updateDonatedItem(
            @PathVariable Long id,
            @RequestBody DonatedItem donatedItem) {

        return donatedItemService.updateDonatedItem(id, donatedItem);
    }

    @DeleteMapping("/{id}")
    public String deleteDonatedItem(
            @PathVariable Long id) {

        donatedItemService.deleteDonatedItem(id);

        return "Donated item deleted successfully";
    }
}