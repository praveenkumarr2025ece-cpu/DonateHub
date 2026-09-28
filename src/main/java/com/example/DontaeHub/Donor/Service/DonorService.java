package com.example.DontaeHub.Donor.Service;

import com.example.DontaeHub.Donor.Repository.DonorRepository;
import com.example.DontaeHub.Donor.entity.Donor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    // CREATE
    public Donor addDonor(Donor donor) {
        return donorRepository.save(donor);
    }

    // READ ALL
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    // READ BY ID
    public Optional<Donor> getDonorById(Long id) {
        return donorRepository.findById(id);
    }

    // UPDATE
    public Donor updateDonor(Long id, Donor updatedDonor) {

        Donor existingDonor = donorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Donor not found"));

        existingDonor.setName(updatedDonor.getName());
        existingDonor.setEmail(updatedDonor.getEmail());
        existingDonor.setPhone(updatedDonor.getPhone());
        existingDonor.setAddress(updatedDonor.getAddress());

        return donorRepository.save(existingDonor);
    }

    // DELETE
    public void deleteDonor(Long id) {
        donorRepository.deleteById(id);
    }
}