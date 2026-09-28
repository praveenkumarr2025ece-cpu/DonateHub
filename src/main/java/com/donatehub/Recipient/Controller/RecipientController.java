package com.donatehub.Recipient.Controller;

import com.donatehub.Recipient.Service.RecipientService;
import com.donatehub.Recipient.entity.Recipient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PostMapping
    public Recipient addRecipient(@RequestBody Recipient recipient) {
        return recipientService.addRecipient(recipient);
    }

    @GetMapping
    public List<Recipient> getAllRecipients() {
        return recipientService.getAllRecipients();
    }

    @GetMapping("/{id}")
    public Recipient getRecipientById(@PathVariable Long id) {
        return recipientService.getRecipientById(id);
    }

    @PutMapping("/{id}")
    public Recipient updateRecipient(@PathVariable Long id, @RequestBody Recipient recipient) {
        return recipientService.updateRecipient(id, recipient);
    }

    @DeleteMapping("/{id}")
    public String deleteRecipient(@PathVariable Long id) {
        recipientService.deleteRecipient(id);
        return "Recipient deleted successfully";
    }
}
