// DonateHub - Donors Management Script

let donorsList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDonors();
});

async function loadDonors() {
    const tbody = document.getElementById('donors-table-body');
    try {
        const data = await apiFetch('/donors');
        donorsList = Array.isArray(data) ? data : [];
        renderDonors(donorsList);
        return donorsList;
    } catch (error) {
        console.error('Failed to load donors:', error);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="6" class="empty-card" style="color: var(--danger);">Failed to load donors from server.</td></tr>`;
        }
        showAlert('Could not load donors. Check server connection.', 'error');
        return [];
    }
}

function renderDonors(donors) {
    const tbody = document.getElementById('donors-table-body');
    if (!tbody) return;

    if (!Array.isArray(donors) || donors.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-card">No donors registered yet. Click "Add New Donor" to get started.</td></tr>`;
        return;
    }

    tbody.innerHTML = donors.map(donor => `
        <tr id="donor-row-${donor.id}">
            <td><span class="badge-id">#${escapeHtml(donor.id)}</span></td>
            <td><strong>${escapeHtml(donor.name)}</strong></td>
            <td><a href="mailto:${escapeHtml(donor.email)}" style="color: var(--primary); text-decoration: none;">${escapeHtml(donor.email)}</a></td>
            <td>${escapeHtml(donor.phone)}</td>
            <td>${escapeHtml(donor.address || '-')}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-xs btn-secondary" onclick="editDonor(${donor.id})">Edit</button>
                    <button class="btn btn-xs btn-danger" onclick="deleteDonor(${donor.id}, '${escapeHtml(donor.name).replace(/'/g, "\\'")}')">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function openDonorModal(donor = null) {
    const titleEl = document.getElementById('donor-modal-title');
    const saveBtn = document.getElementById('donor-save-btn');
    const form = document.getElementById('donor-form');

    if (form) form.reset();

    if (donor && donor.id) {
        if (titleEl) titleEl.textContent = 'Edit Donor';
        if (saveBtn) saveBtn.textContent = 'Update Donor';
        document.getElementById('donor-id').value = donor.id;
        document.getElementById('donor-name').value = donor.name || '';
        document.getElementById('donor-email').value = donor.email || '';
        document.getElementById('donor-phone').value = donor.phone || '';
        document.getElementById('donor-address').value = donor.address || '';
    } else {
        if (titleEl) titleEl.textContent = 'Add New Donor';
        if (saveBtn) saveBtn.textContent = 'Save Donor';
        document.getElementById('donor-id').value = '';
    }

    openModal('donor-modal');
}

async function editDonor(id) {
    try {
        const donor = await apiFetch(`/donors/${id}`);
        if (!donor || !donor.id) {
            throw new Error('Donor not found');
        }
        openDonorModal(donor);
    } catch (error) {
        console.warn(`Donor with ID ${id} cannot be edited:`, error);
        showAlert('Donor no longer exists or could not be found.', 'error');
        await loadDonors();
    }
}

async function saveDonor(event) {
    event.preventDefault();
    const id = document.getElementById('donor-id').value;
    const name = document.getElementById('donor-name').value.trim();
    const email = document.getElementById('donor-email').value.trim();
    const phone = document.getElementById('donor-phone').value.trim();
    const address = document.getElementById('donor-address').value.trim();

    const payload = { name, email, phone, address };

    try {
        if (id) {
            await apiFetch(`/donors/${id}`, {
                method: 'PUT',
                body: payload
            });
            showAlert(`Donor "${name}" updated successfully!`, 'success');
        } else {
            await apiFetch('/donors', {
                method: 'POST',
                body: payload
            });
            showAlert(`Donor "${name}" added successfully!`, 'success');
        }
        closeModal('donor-modal');
        const form = document.getElementById('donor-form');
        if (form) form.reset();
        document.getElementById('donor-id').value = '';
        await loadDonors();
    } catch (error) {
        console.error('Error saving donor:', error);
        showAlert(error.message || 'Failed to save donor.', 'error');
    }
}

async function deleteDonor(id, name) {
    if (!confirm(`Are you sure you want to delete donor "${name}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/donors/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete donor. Note: Donors linked to donated items cannot be deleted.');
        }

        showAlert(`Donor "${name}" deleted successfully.`, 'success');

        // Immediately remove from in-memory list and UI table
        donorsList = donorsList.filter(d => Number(d.id) !== Number(id));
        renderDonors(donorsList);

        // If modal was open with this donor, close it and clear stale ID
        const activeModalId = document.getElementById('donor-id')?.value;
        if (activeModalId && Number(activeModalId) === Number(id)) {
            closeModal('donor-modal');
            const form = document.getElementById('donor-form');
            if (form) form.reset();
            document.getElementById('donor-id').value = '';
        }

        // Fresh sync with GET /donors
        await loadDonors();
    } catch (error) {
        console.error('Error deleting donor:', error);
        showAlert(error.message || 'Failed to delete donor. Note: Donors linked to donated items cannot be deleted.', 'error');
        await loadDonors();
    }
}
