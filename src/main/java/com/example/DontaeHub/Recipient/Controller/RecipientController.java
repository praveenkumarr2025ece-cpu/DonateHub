package com.example.DontaeHub.Recipient.Controller;

import com.example.DontaeHub.Recipient.Service.RecipientService;
import com.example.DontaeHub.Recipient.entity.Recipient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    // CREATE
    @PostMapping
    public Recipient addRecipient(@RequestBody Recipient recipient) {
        return recipientService.addRecipient(recipient);
    }

    // READ ALL
    @GetMapping
    public List<Recipient> getAllRecipients() {
        return recipientService.getAllRecipients();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public Optional<Recipient> getRecipientById(
            @PathVariable Long id) {

        return recipientService.getRecipientById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Recipient updateRecipient(
            @PathVariable Long id,
            @RequestBody Recipient recipient) {

        return recipientService.updateRecipient(id, recipient);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteRecipient(@PathVariable Long id) {

        recipientService.deleteRecipient(id);

        return "Recipient deleted successfully";
    }
}
