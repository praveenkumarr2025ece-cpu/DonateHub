package com.donatehub.DonatedItem.Controller;

import com.donatehub.DonatedItem.Service.DonatedItemService;
import com.donatehub.DonatedItem.entity.DonatedItem;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donated-items")
public class DonatedItemController {

    private final DonatedItemService donatedItemService;

    public DonatedItemController(DonatedItemService donatedItemService) {
        this.donatedItemService = donatedItemService;
    }

    @PostMapping
    public DonatedItem addDonatedItem(@RequestBody DonatedItem item) {
        return donatedItemService.addDonatedItem(item);
    }

    @GetMapping
    public List<DonatedItem> getAllDonatedItems() {
        return donatedItemService.getAllDonatedItems();
    }

    @GetMapping("/{id}")
    public DonatedItem getDonatedItemById(@PathVariable Long id) {
        return donatedItemService.getDonatedItemById(id);
    }

    @PutMapping("/{id}")
    public DonatedItem updateDonatedItem(@PathVariable Long id, @RequestBody DonatedItem item) {
        return donatedItemService.updateDonatedItem(id, item);
    }

    @PutMapping("/{id}/distribute")
    public DonatedItem distributeItem(@PathVariable Long id, @RequestParam int quantity) {
        return donatedItemService.distributeItem(id, quantity);
    }

    @DeleteMapping("/{id}")
    public String deleteDonatedItem(@PathVariable Long id) {
        donatedItemService.deleteDonatedItem(id);
        return "Donated item deleted successfully";
    }
}
