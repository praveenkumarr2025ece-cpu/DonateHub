// DonateHub - Donated Items & Distribution Script

let itemsList = [];
let donorsList = [];
let drivesList = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDonatedItems();
});

async function loadDonatedItems() {
    const tbody = document.getElementById('items-table-body');
    try {
        const [items, donors, drives] = await Promise.all([
            apiFetch('/donated-items'),
            apiFetch('/donors').catch(() => []),
            apiFetch('/drives').catch(() => [])
        ]);

        itemsList = Array.isArray(items) ? items : [];
        donorsList = Array.isArray(donors) ? donors : [];
        drivesList = Array.isArray(drives) ? drives : [];

        renderItems(itemsList);
        return itemsList;
    } catch (error) {
        console.error('Failed to load items:', error);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="10" class="empty-card" style="color: var(--danger);">Failed to load donated items from server.</td></tr>`;
        }
        showAlert('Could not load donated items. Check server connection.', 'error');
        return [];
    }
}

function getDonorName(donorId) {
    if (!donorId) return '-';
    const donor = donorsList.find(d => d.id === Number(donorId));
    return donor ? donor.name : `Donor #${donorId}`;
}

function getDriveName(driveId) {
    if (!driveId) return '-';
    const drive = drivesList.find(d => d.id === Number(driveId));
    return drive ? drive.name : `Drive #${driveId}`;
}

function renderItems(items) {
    const tbody = document.getElementById('items-table-body');
    if (!tbody) return;

    if (!Array.isArray(items) || items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" class="empty-card">No donated items recorded yet. Click "+ Add Donated Item" to register items.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(item => {
        const qty = Number(item.quantity) || 0;
        const dist = Number(item.distributedQuantity) || 0;
        const remaining = qty - dist;
        const canDistribute = remaining > 0;

        return `
            <tr id="item-row-${item.id}">
                <td><span class="badge-id">#${escapeHtml(item.id)}</span></td>
                <td><strong>${escapeHtml(item.name)}</strong></td>
                <td><span class="badge badge-category">${escapeHtml(item.category)}</span></td>
                <td><span class="badge badge-condition">${escapeHtml(item.condition)}</span></td>
                <td>${qty}</td>
                <td>${dist}</td>
                <td><span class="badge-stock ${remaining > 0 ? 'stock-in' : 'stock-out'}">${remaining}</span></td>
                <td>${escapeHtml(getDonorName(item.donorId))}</td>
                <td>${escapeHtml(getDriveName(item.driveId))}</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-xs ${canDistribute ? 'btn-success' : 'btn-secondary'}" 
                                ${canDistribute ? '' : 'disabled style="opacity: 0.5; cursor: not-allowed;"'} 
                                onclick="openDistributeModal(${item.id})">
                            ${canDistribute ? 'Distribute' : 'Exhausted'}
                        </button>
                        <button class="btn btn-xs btn-secondary" onclick="editItem(${item.id})">Edit</button>
                        <button class="btn btn-xs btn-danger" onclick="deleteItem(${item.id}, '${escapeHtml(item.name).replace(/'/g, "\\'")}')">Delete</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function populateDropdowns(selectedDonorId = null, selectedDriveId = null) {
    const donorSelect = document.getElementById('item-donor-id');
    const driveSelect = document.getElementById('item-drive-id');

    if (donorSelect) {
        donorSelect.innerHTML = '<option value="">-- Select Donor --</option>';
        donorsList.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.name} (${d.email})`;
            if (selectedDonorId && Number(selectedDonorId) === Number(d.id)) opt.selected = true;
            donorSelect.appendChild(opt);
        });
    }

    if (driveSelect) {
        driveSelect.innerHTML = '<option value="">-- Select Drive --</option>';
        drivesList.forEach(dr => {
            const opt = document.createElement('option');
            opt.value = dr.id;
            opt.textContent = dr.name;
            if (selectedDriveId && Number(selectedDriveId) === Number(dr.id)) opt.selected = true;
            driveSelect.appendChild(opt);
        });
    }
}

async function openItemModal(item = null) {
    // Refresh dependencies
    try {
        const [d, dr] = await Promise.all([
            apiFetch('/donors').catch(() => []),
            apiFetch('/drives').catch(() => [])
        ]);
        donorsList = Array.isArray(d) ? d : [];
        drivesList = Array.isArray(dr) ? dr : [];
    } catch (e) {
        console.warn('Could not refresh donors or drives');
    }

    const titleEl = document.getElementById('item-modal-title');
    const saveBtn = document.getElementById('item-save-btn');
    const form = document.getElementById('item-form');

    if (form) form.reset();

    if (item && item.id) {
        if (titleEl) titleEl.textContent = 'Edit Donated Item';
        if (saveBtn) saveBtn.textContent = 'Update Item';
        document.getElementById('item-id').value = item.id;
        document.getElementById('item-name').value = item.name || '';
        document.getElementById('item-category').value = item.category || 'Clothes';
        document.getElementById('item-condition').value = item.condition || 'New';
        document.getElementById('item-quantity').value = item.quantity || 1;
        populateDropdowns(item.donorId, item.driveId);
    } else {
        if (titleEl) titleEl.textContent = 'Add Donated Item';
        if (saveBtn) saveBtn.textContent = 'Save Item';
        document.getElementById('item-id').value = '';
        populateDropdowns();
    }

    openModal('item-modal');
}

async function editItem(id) {
    try {
        const item = await apiFetch(`/donated-items/${id}`);
        if (!item || !item.id) {
            throw new Error('Item not found');
        }
        await openItemModal(item);
    } catch (error) {
        console.warn(`Item with ID ${id} cannot be edited:`, error);
        showAlert('Donated item no longer exists or could not be found.', 'error');
        await loadDonatedItems();
    }
}

async function saveItem(event) {
    event.preventDefault();
    const id = document.getElementById('item-id').value;
    const name = document.getElementById('item-name').value.trim();
    const category = document.getElementById('item-category').value;
    const condition = document.getElementById('item-condition').value;
    const quantity = parseInt(document.getElementById('item-quantity').value, 10);
    const donorId = document.getElementById('item-donor-id').value;
    const driveId = document.getElementById('item-drive-id').value;

    if (!quantity || quantity <= 0) {
        showAlert('Quantity must be greater than 0.', 'error');
        return;
    }

    if (!donorId || !driveId) {
        showAlert('Please select both a donor and a drive.', 'error');
        return;
    }

    const payload = {
        name,
        category,
        condition,
        quantity,
        donorId: Number(donorId),
        driveId: Number(driveId)
    };

    try {
        if (id) {
            await apiFetch(`/donated-items/${id}`, {
                method: 'PUT',
                body: payload
            });
            showAlert(`Item "${name}" updated successfully!`, 'success');
        } else {
            await apiFetch('/donated-items', {
                method: 'POST',
                body: payload
            });
            showAlert(`Item "${name}" registered successfully!`, 'success');
        }
        closeModal('item-modal');
        const form = document.getElementById('item-form');
        if (form) form.reset();
        document.getElementById('item-id').value = '';
        await loadDonatedItems();
    } catch (error) {
        console.error('Error saving item:', error);
        showAlert(error.message || 'Failed to save donated item.', 'error');
    }
}

async function deleteItem(id, name) {
    if (!confirm(`Are you sure you want to delete item "${name}"?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/donated-items/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete item.');
        }

        showAlert(`Item "${name}" deleted successfully.`, 'success');

        // Immediately remove from in-memory list and UI table
        itemsList = itemsList.filter(it => Number(it.id) !== Number(id));
        renderItems(itemsList);

        // If modal was open with this item, close it and clear stale ID
        const activeModalId = document.getElementById('item-id')?.value;
        if (activeModalId && Number(activeModalId) === Number(id)) {
            closeModal('item-modal');
            const form = document.getElementById('item-form');
            if (form) form.reset();
            document.getElementById('item-id').value = '';
        }

        // If distribute modal was open with this item, close it
        const activeDistId = document.getElementById('distribute-item-id')?.value;
        if (activeDistId && Number(activeDistId) === Number(id)) {
            closeModal('distribute-modal');
            const distForm = document.getElementById('distribute-form');
            if (distForm) distForm.reset();
            document.getElementById('distribute-item-id').value = '';
        }

        // Fresh sync with GET /donated-items
        await loadDonatedItems();
    } catch (error) {
        console.error('Error deleting item:', error);
        showAlert(error.message || 'Failed to delete item.', 'error');
        await loadDonatedItems();
    }
}

async function openDistributeModal(id) {
    let item = null;
    try {
        item = await apiFetch(`/donated-items/${id}`);
    } catch (e) {
        showAlert('Could not load current item details for distribution.', 'error');
        await loadDonatedItems();
        return;
    }

    if (!item || !item.id) {
        showAlert('Item not found.', 'error');
        await loadDonatedItems();
        return;
    }

    const remaining = (Number(item.quantity) || 0) - (Number(item.distributedQuantity) || 0);
    if (remaining <= 0) {
        showAlert('No remaining stock available for distribution.', 'error');
        await loadDonatedItems();
        return;
    }

    document.getElementById('distribute-item-id').value = item.id;
    document.getElementById('distribute-item-name').textContent = `${item.name} (${item.category} - ${item.condition})`;
    document.getElementById('distribute-available-stock').textContent = remaining;

    const qtyInput = document.getElementById('distribute-quantity-input');
    qtyInput.value = 1;
    qtyInput.max = remaining;
    qtyInput.min = 1;

    openModal('distribute-modal');
}

async function submitDistribution(event) {
    event.preventDefault();
    const id = document.getElementById('distribute-item-id').value;
    const qtyInput = document.getElementById('distribute-quantity-input');
    const quantity = parseInt(qtyInput.value, 10);
    const maxStock = parseInt(qtyInput.max, 10);

    if (!id) {
        showAlert('No item selected for distribution.', 'error');
        return;
    }

    if (!quantity || quantity <= 0) {
        showAlert('Distribution quantity must be greater than 0.', 'error');
        return;
    }

    if (quantity > maxStock) {
        showAlert(`Cannot distribute ${quantity}. Only ${maxStock} items available in stock.`, 'error');
        return;
    }

    try {
        await apiFetch(`/donated-items/${id}/distribute?quantity=${quantity}`, {
            method: 'PUT'
        });
        showAlert(`Successfully distributed ${quantity} unit(s)!`, 'success');
        closeModal('distribute-modal');
        const distForm = document.getElementById('distribute-form');
        if (distForm) distForm.reset();
        document.getElementById('distribute-item-id').value = '';
        await loadDonatedItems();
    } catch (error) {
        console.error('Distribution failed:', error);
        showAlert(error.message || 'Failed to distribute item.', 'error');
    }
}
