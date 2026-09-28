package com.donatehub.Recipient.Service;

import com.donatehub.Recipient.Repository.RecipientRepository;
import com.donatehub.Recipient.entity.Recipient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipientService {

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    public Recipient addRecipient(Recipient recipient) {
        return recipientRepository.save(recipient);
    }

    public List<Recipient> getAllRecipients() {
        return recipientRepository.findAll();
    }

    public Recipient getRecipientById(Long id) {
        return recipientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipient not found with id: " + id));
    }

    public Recipient updateRecipient(Long id, Recipient updatedRecipient) {
        Recipient existingRecipient = getRecipientById(id);
        existingRecipient.setName(updatedRecipient.getName());
        existingRecipient.setOrganizationName(updatedRecipient.getOrganizationName());
        existingRecipient.setContactPerson(updatedRecipient.getContactPerson());
        existingRecipient.setPhone(updatedRecipient.getPhone());
        existingRecipient.setAddress(updatedRecipient.getAddress());
        return recipientRepository.save(existingRecipient);
    }

    public void deleteRecipient(Long id) {
        Recipient existingRecipient = getRecipientById(id);
        recipientRepository.delete(existingRecipient);
    }
}
