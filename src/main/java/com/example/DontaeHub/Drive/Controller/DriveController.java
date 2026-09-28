package com.example.DontaeHub.Drive.Controller;

import com.example.DontaeHub.Drive.Service.DriveService;
import com.example.DontaeHub.Drive.entity.Drive;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/drives")
public class DriveController {

    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    // CREATE
    @PostMapping
    public Drive createDrive(@RequestBody Drive drive) {
        return driveService.createDrive(drive);
    }

    // READ ALL
    @GetMapping
    public List<Drive> getAllDrives() {
        return driveService.getAllDrives();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Optional<Drive> getDriveById(@PathVariable Long id) {
        return driveService.getDriveById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Drive updateDrive(
            @PathVariable Long id,
            @RequestBody Drive drive) {

        return driveService.updateDrive(id, drive);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteDrive(@PathVariable Long id) {

        driveService.deleteDrive(id);

        return "Drive deleted successfully";
    }
}