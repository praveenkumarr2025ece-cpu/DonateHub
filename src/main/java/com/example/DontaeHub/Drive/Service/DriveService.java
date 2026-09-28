package com.example.DontaeHub.Drive.Service;

import com.example.DontaeHub.Drive.Repository.DriveRepository;
import com.example.DontaeHub.Drive.entity.Drive;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DriveService {

    private final DriveRepository driveRepository;

    public DriveService(DriveRepository driveRepository) {
        this.driveRepository = driveRepository;
    }

    // CREATE
    public Drive createDrive(Drive drive) {
        return driveRepository.save(drive);
    }

    // READ ALL
    public List<Drive> getAllDrives() {
        return driveRepository.findAll();
    }

    // READ BY ID
    public Optional<Drive> getDriveById(Long id) {
        return driveRepository.findById(id);
    }

    // UPDATE
    public Drive updateDrive(Long id, Drive updatedDrive) {

        Drive existingDrive = driveRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Drive not found"));

        existingDrive.setName(updatedDrive.getName());
        existingDrive.setStartDate(updatedDrive.getStartDate());
        existingDrive.setEndDate(updatedDrive.getEndDate());
        existingDrive.setDescription(updatedDrive.getDescription());
        existingDrive.setRecipientId(updatedDrive.getRecipientId());

        return driveRepository.save(existingDrive);
    }

    // DELETE
    public void deleteDrive(Long id) {
        driveRepository.deleteById(id);
    }
}