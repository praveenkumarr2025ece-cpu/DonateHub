// DonateHub - Recipients Management Script

let recipientsList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadRecipients();
});

async function loadRecipients() {
    const tbody = document.getElementById('recipients-table-body');
    try {
        const data = await apiFetch('/recipients');
        recipientsList = Array.isArray(data) ? data : [];
        renderRecipients(recipientsList);
        return recipientsList;
    } catch (error) {
        console.error('Failed to load recipients:', error);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-card" style="color: var(--danger);">Failed to load recipients from server.</td></tr>`;
        }
        showAlert('Could not load recipients. Check server connection.', 'error');
        return [];
    }
}

function renderRecipients(recipients) {
    const tbody = document.getElementById('recipients-table-body');
    if (!tbody) return;

    if (!Array.isArray(recipients) || recipients.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-card">No recipients registered yet. Click "Add Recipient" to add one.</td></tr>`;
        return;
    }

    tbody.innerHTML = recipients.map(r => `
        <tr id="recipient-row-${r.id}">
            <td><span class="badge-id">#${escapeHtml(r.id)}</span></td>
            <td><strong>${escapeHtml(r.name)}</strong></td>
            <td>${escapeHtml(r.organizationName || '-')}</td>
            <td>${escapeHtml(r.contactPerson || '-')}</td>
            <td>${escapeHtml(r.phone)}</td>
            <td>${escapeHtml(r.address || '-')}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-xs btn-secondary" onclick="editRecipient(${r.id})">Edit</button>
                    <button class="btn btn-xs btn-danger" onclick="deleteRecipient(${r.id}, '${escapeHtml(r.name).replace(/'/g, "\\'")}')">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openRecipientModal(recipient = null) {
    const titleEl = document.getElementById('recipient-modal-title');
    const saveBtn = document.getElementById('recipient-save-btn');
    const form = document.getElementById('recipient-form');

    if (form) form.reset();

    if (recipient && recipient.id) {
        if (titleEl) titleEl.textContent = 'Edit Recipient';
        if (saveBtn) saveBtn.textContent = 'Update Recipient';
        document.getElementById('recipient-id').value = recipient.id;
        document.getElementById('recipient-name').value = recipient.name || '';
        document.getElementById('recipient-org').value = recipient.organizationName || '';
        document.getElementById('recipient-contact').value = recipient.contactPerson || '';
        document.getElementById('recipient-phone').value = recipient.phone || '';
        document.getElementById('recipient-address').value = recipient.address || '';
    } else {
        if (titleEl) titleEl.textContent = 'Add New Recipient';
        if (saveBtn) saveBtn.textContent = 'Save Recipient';
        document.getElementById('recipient-id').value = '';
    }

    openModal('recipient-modal');
}

async function editRecipient(id) {
    try {
        const recipient = await apiFetch(`/recipients/${id}`);
        if (!recipient || !recipient.id) {
            throw new Error('Recipient not found');
        }
        openRecipientModal(recipient);
    } catch (error) {
        console.warn(`Recipient with ID ${id} cannot be edited:`, error);
        showAlert('Recipient no longer exists or could not be found.', 'error');
        await loadRecipients();
    }
}

async function saveRecipient(event) {
    event.preventDefault();
    const id = document.getElementById('recipient-id').value;
    const name = document.getElementById('recipient-name').value.trim();
    const organizationName = document.getElementById('recipient-org').value.trim();
    const contactPerson = document.getElementById('recipient-contact').value.trim();
    const phone = document.getElementById('recipient-phone').value.trim();
    const address = document.getElementById('recipient-address').value.trim();

    const payload = { name, organizationName, contactPerson, phone, address };

    try {
        if (id) {
            await apiFetch(`/recipients/${id}`, {
                method: 'PUT',
                body: payload
            });
            showAlert(`Recipient "${name}" updated successfully!`, 'success');
        } else {
            await apiFetch('/recipients', {
                method: 'POST',
                body: payload
            });
            showAlert(`Recipient "${name}" added successfully!`, 'success');
        }
        closeModal('recipient-modal');
        const form = document.getElementById('recipient-form');
        if (form) form.reset();
        document.getElementById('recipient-id').value = '';
        await loadRecipients();
    } catch (error) {
        console.error('Error saving recipient:', error);
        showAlert(error.message || 'Failed to save recipient.', 'error');
    }
}

async function deleteRecipient(id, name) {
    if (!confirm(`Are you sure you want to delete recipient "${name}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/recipients/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete recipient. Note: Recipients linked to drives cannot be deleted.');
        }

        showAlert(`Recipient "${name}" deleted successfully.`, 'success');

        // Immediately remove from in-memory list and UI table
        recipientsList = recipientsList.filter(r => Number(r.id) !== Number(id));
        renderRecipients(recipientsList);

        // If modal was open with this recipient, close it and clear stale ID
        const activeModalId = document.getElementById('recipient-id')?.value;
        if (activeModalId && Number(activeModalId) === Number(id)) {
            closeModal('recipient-modal');
            const form = document.getElementById('recipient-form');
            if (form) form.reset();
            document.getElementById('recipient-id').value = '';
        }

        // Fresh sync with GET /recipients
        await loadRecipients();
    } catch (error) {
        console.error('Error deleting recipient:', error);
        showAlert(error.message || 'Failed to delete recipient. Note: Recipients linked to drives cannot be deleted.', 'error');
        await loadRecipients();
    }
}
