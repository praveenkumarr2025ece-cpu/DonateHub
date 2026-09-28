package com.donatehub.Drive.Controller;

import com.donatehub.DonatedItem.entity.DonatedItem;
import com.donatehub.Drive.Service.DriveService;
import com.donatehub.Drive.entity.Drive;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/drives")
public class DriveController {

    private final DriveService driveService;

    public DriveController(DriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping
    public Drive createDrive(@RequestBody Drive drive) {
        return driveService.createDrive(drive);
    }

    @GetMapping
    public List<Drive> getAllDrives() {
        return driveService.getAllDrives();
    }

    @GetMapping("/{id}")
    public Drive getDriveById(@PathVariable Long id) {
        return driveService.getDriveById(id);
    }

    @GetMapping("/{id}/items")
    public List<DonatedItem> getDriveItems(@PathVariable Long id) {
        return driveService.getDriveItems(id);
    }

    @GetMapping("/{id}/summary")
    public Map<String, Object> getDriveSummary(@PathVariable Long id) {
        return driveService.getDriveSummary(id);
    }

    @PutMapping("/{id}")
    public Drive updateDrive(@PathVariable Long id, @RequestBody Drive drive) {
        return driveService.updateDrive(id, drive);
    }

    @DeleteMapping("/{id}")
    public String deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return "Drive deleted successfully";
    }
}
