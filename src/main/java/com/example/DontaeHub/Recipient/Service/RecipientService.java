package com.example.DontaeHub.Recipient.Service;

import com.example.DontaeHub.Recipient.Repository.RecipientRepository;
import com.example.DontaeHub.Recipient.entity.Recipient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    // CREATE
    public Recipient addRecipient(Recipient recipient) {
        return recipientRepository.save(recipient);
    }

    // READ ALL
    public List<Recipient> getAllRecipients() {
        return recipientRepository.findAll();
    }

    // READ BY ID
    public Optional<Recipient> getRecipientById(Long id) {
        return recipientRepository.findById(id);
    }

    // UPDATE
    public Recipient updateRecipient(Long id, Recipient updatedRecipient) {

        Recipient existingRecipient = recipientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Recipient not found"));

        existingRecipient.setName(updatedRecipient.getName());
        existingRecipient.setOrganizationName(updatedRecipient.getOrganizationName());
        existingRecipient.setContactPerson(updatedRecipient.getContactPerson());
        existingRecipient.setPhone(updatedRecipient.getPhone());
        existingRecipient.setAddress(updatedRecipient.getAddress());

        return recipientRepository.save(existingRecipient);
    }

    // DELETE
    public void deleteRecipient(Long id) {
        recipientRepository.deleteById(id);
    }
}
