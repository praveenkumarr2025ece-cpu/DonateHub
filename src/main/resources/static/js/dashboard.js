// DonateHub - Dashboard Script

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        const [donors, recipients, drives, items] = await Promise.all([
            apiFetch('/donors').catch(err => { console.warn('Failed to load donors:', err); return []; }),
            apiFetch('/recipients').catch(err => { console.warn('Failed to load recipients:', err); return []; }),
            apiFetch('/drives').catch(err => { console.warn('Failed to load drives:', err); return []; }),
            apiFetch('/donated-items').catch(err => { console.warn('Failed to load donated items:', err); return []; })
        ]);

        // Update Stat Cards
        document.getElementById('stat-donors').textContent = Array.isArray(donors) ? donors.length : 0;
        document.getElementById('stat-recipients').textContent = Array.isArray(recipients) ? recipients.length : 0;
        document.getElementById('stat-drives').textContent = Array.isArray(drives) ? drives.length : 0;
        document.getElementById('stat-items').textContent = Array.isArray(items) ? items.length : 0;

        const totalQty = Array.isArray(items) ? items.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0) : 0;
        const totalDist = Array.isArray(items) ? items.reduce((sum, item) => sum + (Number(item.distributedQuantity) || 0), 0) : 0;
        
        document.getElementById('stat-quantity').textContent = totalQty;
        document.getElementById('stat-distributed-label').textContent = `${totalDist} distributed / ${totalQty - totalDist} remaining`;

        // Render Recent Drives (up to 5)
        renderRecentDrives(Array.isArray(drives) ? drives.slice(0, 5) : []);

        // Render Recent Items (up to 5)
        renderRecentItems(Array.isArray(items) ? items.slice(0, 5) : []);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showAlert('Could not load dashboard data from backend server.', 'error');
    }
}

function renderRecentDrives(drives) {
    const tbody = document.getElementById('recent-drives-body');
    if (!tbody) return;

    if (drives.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-card">No donation drives found. <a href="drives.html">Create one</a></td></tr>`;
        return;
    }

    tbody.innerHTML = drives.map(drive => `
        <tr>
            <td><span class="badge-id">#${escapeHtml(drive.id)}</span></td>
            <td><strong>${escapeHtml(drive.name)}</strong></td>
            <td>${escapeHtml(drive.startDate || '-')}</td>
            <td>${escapeHtml(drive.endDate || '-')}</td>
            <td>${escapeHtml(drive.description || '-')}</td>
            <td>
                <a href="drives.html" class="btn btn-xs btn-secondary">Manage</a>
            </td>
        </tr>
    `).join('');
}

function renderRecentItems(items) {
    const tbody = document.getElementById('recent-items-body');
    if (!tbody) return;

    if (items.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-card">No donated items cataloged yet. <a href="donated-items.html">Add an item</a></td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(item => {
        const qty = Number(item.quantity) || 0;
        const dist = Number(item.distributedQuantity) || 0;
        const remaining = qty - dist;

        return `
            <tr>
                <td><span class="badge-id">#${escapeHtml(item.id)}</span></td>
                <td><strong>${escapeHtml(item.name)}</strong></td>
                <td><span class="badge badge-category">${escapeHtml(item.category)}</span></td>
                <td><span class="badge badge-condition">${escapeHtml(item.condition)}</span></td>
                <td>${qty}</td>
                <td>${dist}</td>
                <td><span class="badge-stock ${remaining > 0 ? 'stock-in' : 'stock-out'}">${remaining}</span></td>
            </tr>
        `;
    }).join('');
}
