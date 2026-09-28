// DonateHub - Drives Management Script

let drivesList = [];
let recipientsList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDrives();
});

async function loadDrives() {
    const tbody = document.getElementById('drives-table-body');
    try {
        const [drives, recipients] = await Promise.all([
            apiFetch('/drives'),
            apiFetch('/recipients').catch(() => [])
        ]);

        drivesList = Array.isArray(drives) ? drives : [];
        recipientsList = Array.isArray(recipients) ? recipients : [];

        renderDrives(drivesList);
        return drivesList;
    } catch (error) {
        console.error('Failed to load drives:', error);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-card" style="color: var(--danger);">Failed to load drives from server.</td></tr>`;
        }
        showAlert('Could not load drives. Check server connection.', 'error');
        return [];
    }
}

function getRecipientName(recipientId) {
    if (!recipientId) return 'None';
    const recipient = recipientsList.find(r => r.id === Number(recipientId));
    return recipient ? (recipient.organizationName || recipient.name) : `Recipient #${recipientId}`;
}

function renderDrives(drives) {
    const tbody = document.getElementById('drives-table-body');
    if (!tbody) return;

    if (!Array.isArray(drives) || drives.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-card">No donation drives scheduled. Click "+ Create New Drive" to add one.</td></tr>`;
        return;
    }

    tbody.innerHTML = drives.map(drive => `
        <tr id="drive-row-${drive.id}">
            <td><span class="badge-id">#${escapeHtml(drive.id)}</span></td>
            <td><strong>${escapeHtml(drive.name)}</strong></td>
            <td>${escapeHtml(getRecipientName(drive.recipientId))}</td>
            <td>${escapeHtml(drive.startDate || '-')}</td>
            <td>${escapeHtml(drive.endDate || '-')}</td>
            <td>${escapeHtml(drive.description || '-')}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-xs btn-primary" onclick="viewDriveSummary(${drive.id}, '${escapeHtml(drive.name).replace(/'/g, "\\'")}')">Summary</button>
                    <button class="btn btn-xs btn-secondary" onclick="editDrive(${drive.id})">Edit</button>
                    <button class="btn btn-xs btn-danger" onclick="deleteDrive(${drive.id}, '${escapeHtml(drive.name).replace(/'/g, "\\'")}')">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function populateRecipientDropdown(selectedRecipientId = null) {
    const select = document.getElementById('drive-recipient-id');
    if (!select) return;

    select.innerHTML = '<option value="">-- Select Recipient Partner (Required) --</option>';

    recipientsList.forEach(recip => {
        const option = document.createElement('option');
        option.value = recip.id;
        option.textContent = `${recip.name}${recip.organizationName ? ' (' + recip.organizationName + ')' : ''}`;
        if (selectedRecipientId && Number(selectedRecipientId) === Number(recip.id)) {
            option.selected = true;
        }
        select.appendChild(option);
    });
}

async function openDriveModal(drive = null) {
    // Ensure recipients are fresh
    try {
        const data = await apiFetch('/recipients');
        recipientsList = Array.isArray(data) ? data : [];
    } catch (e) {
        console.warn('Could not load recipients for dropdown');
    }

    const titleEl = document.getElementById('drive-modal-title');
    const saveBtn = document.getElementById('drive-save-btn');
    const form = document.getElementById('drive-form');

    if (form) form.reset();

    if (drive && drive.id) {
        if (titleEl) titleEl.textContent = 'Edit Donation Drive';
        if (saveBtn) saveBtn.textContent = 'Update Drive';
        document.getElementById('drive-id').value = drive.id;
        document.getElementById('drive-name').value = drive.name || '';
        document.getElementById('drive-start-date').value = drive.startDate || '';
        document.getElementById('drive-end-date').value = drive.endDate || '';
        document.getElementById('drive-description').value = drive.description || '';
        populateRecipientDropdown(drive.recipientId);
    } else {
        if (titleEl) titleEl.textContent = 'Create Donation Drive';
        if (saveBtn) saveBtn.textContent = 'Save Drive';
        document.getElementById('drive-id').value = '';
        populateRecipientDropdown();
    }

    openModal('drive-modal');
}

async function editDrive(id) {
    try {
        const drive = await apiFetch(`/drives/${id}`);
        if (!drive || !drive.id) {
            throw new Error('Drive not found');
        }
        await openDriveModal(drive);
    } catch (error) {
        console.warn(`Drive with ID ${id} cannot be edited:`, error);
        showAlert('Drive no longer exists or could not be found.', 'error');
        await loadDrives();
    }
}

async function saveDrive(event) {
    event.preventDefault();
    const id = document.getElementById('drive-id').value;
    const name = document.getElementById('drive-name').value.trim();
    const recipientId = document.getElementById('drive-recipient-id').value;
    const startDate = document.getElementById('drive-start-date').value;
    const endDate = document.getElementById('drive-end-date').value;
    const description = document.getElementById('drive-description').value.trim();

    if (!recipientId) {
        showAlert('Please select a recipient partner for this drive.', 'error');
        return;
    }

    if (startDate && endDate && startDate > endDate) {
        showAlert('Start date cannot be after end date.', 'error');
        return;
    }

    const payload = {
        name,
        recipientId: recipientId ? Number(recipientId) : null,
        startDate,
        endDate,
        description
    };

    try {
        if (id) {
            await apiFetch(`/drives/${id}`, {
                method: 'PUT',
                body: payload
            });
            showAlert(`Drive "${name}" updated successfully!`, 'success');
        } else {
            await apiFetch('/drives', {
                method: 'POST',
                body: payload
            });
            showAlert(`Drive "${name}" created successfully!`, 'success');
        }
        closeModal('drive-modal');
        const form = document.getElementById('drive-form');
        if (form) form.reset();
        document.getElementById('drive-id').value = '';
        await loadDrives();
    } catch (error) {
        console.error('Error saving drive:', error);
        showAlert(error.message || 'Failed to save drive.', 'error');
    }
}

async function deleteDrive(id, name) {
    if (!confirm(`Are you sure you want to delete drive "${name}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/drives/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete drive. Note: Drives containing donated items cannot be deleted.');
        }

        showAlert(`Drive "${name}" deleted successfully.`, 'success');

        // Immediately remove from in-memory list and UI table
        drivesList = drivesList.filter(d => Number(d.id) !== Number(id));
        renderDrives(drivesList);

        // If modal was open with this drive, close it and clear stale ID
        const activeModalId = document.getElementById('drive-id')?.value;
        if (activeModalId && Number(activeModalId) === Number(id)) {
            closeModal('drive-modal');
            const form = document.getElementById('drive-form');
            if (form) form.reset();
            document.getElementById('drive-id').value = '';
        }

        // Close summary modal if open for this drive
        closeModal('drive-summary-modal');

        // Fresh sync with GET /drives
        await loadDrives();
    } catch (error) {
        console.error('Error deleting drive:', error);
        showAlert(error.message || 'Failed to delete drive. Note: Drives containing donated items cannot be deleted.', 'error');
        await loadDrives();
    }
}

async function viewDriveSummary(id, name) {
    document.getElementById('summary-drive-title').textContent = `Summary: ${name}`;
    document.getElementById('summary-total-qty').textContent = '...';
    document.getElementById('summary-distributed-qty').textContent = '...';
    document.getElementById('summary-remaining-qty').textContent = '...';
    
    const tbody = document.getElementById('summary-items-body');
    tbody.innerHTML = `<tr><td colspan="6" class="empty-card">Loading items...</td></tr>`;

    openModal('drive-summary-modal');

    try {
        const [summary, items] = await Promise.all([
            apiFetch(`/drives/${id}/summary`),
            apiFetch(`/drives/${id}/items`)
        ]);

        document.getElementById('summary-total-qty').textContent = summary.totalQuantity ?? 0;
        document.getElementById('summary-distributed-qty').textContent = summary.distributedQuantity ?? 0;
        document.getElementById('summary-remaining-qty').textContent = summary.remainingQuantity ?? 0;

        if (!Array.isArray(items) || items.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-card">No donated items linked to this drive yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = items.map(item => {
            const qty = Number(item.quantity) || 0;
            const dist = Number(item.distributedQuantity) || 0;
            const remaining = qty - dist;

            return `
                <tr>
                    <td><strong>${escapeHtml(item.name)}</strong></td>
                    <td><span class="badge badge-category">${escapeHtml(item.category)}</span></td>
                    <td><span class="badge badge-condition">${escapeHtml(item.condition)}</span></td>
                    <td>${qty}</td>
                    <td>${dist}</td>
                    <td><span class="badge-stock ${remaining > 0 ? 'stock-in' : 'stock-out'}">${remaining}</span></td>
                </tr>
            `;
        }).join('');

    } catch (error) {
        console.error('Error loading drive summary:', error);
        tbody.innerHTML = `<tr><td colspan="6" class="empty-card" style="color: var(--danger);">Failed to load summary.</td></tr>`;
        showAlert('Could not load drive summary details.', 'error');
    }
}
