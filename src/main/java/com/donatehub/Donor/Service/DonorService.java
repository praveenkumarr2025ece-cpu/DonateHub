package com.donatehub.Donor.Service;

import com.donatehub.Donor.Repository.DonorRepository;
import com.donatehub.Donor.entity.Donor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    public Donor addDonor(Donor donor) {
        return donorRepository.save(donor);
    }

    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    public Donor getDonorById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donor not found with id: " + id));
    }

    public Donor updateDonor(Long id, Donor updatedDonor) {
        Donor existingDonor = getDonorById(id);
        existingDonor.setName(updatedDonor.getName());
        existingDonor.setEmail(updatedDonor.getEmail());
        existingDonor.setPhone(updatedDonor.getPhone());
        existingDonor.setAddress(updatedDonor.getAddress());
        return donorRepository.save(existingDonor);
    }

    public void deleteDonor(Long id) {
        Donor existingDonor = getDonorById(id);
        donorRepository.delete(existingDonor);
    }
}
