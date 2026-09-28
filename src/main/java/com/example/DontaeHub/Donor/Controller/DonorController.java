package com.example.DontaeHub.Donor.Controller;

import com.example.DontaeHub.Donor.Service.DonorService;
import com.example.DontaeHub.Donor.entity.Donor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    // CREATE
    @PostMapping
    public Donor addDonor(@RequestBody Donor donor) {
        return donorService.addDonor(donor);
    }

    // READ ALL
    @GetMapping
    public List<Donor> getAllDonors() {
        return donorService.getAllDonors();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Optional<Donor> getDonorById(@PathVariable Long id) {
        return donorService.getDonorById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Donor updateDonor(
            @PathVariable Long id,
            @RequestBody Donor donor) {

        return donorService.updateDonor(id, donor);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteDonor(@PathVariable Long id) {

        donorService.deleteDonor(id);

        return "Donor deleted successfully";
    }
}