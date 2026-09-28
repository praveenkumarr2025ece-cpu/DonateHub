// DonateHub - Common Utilities & API Fetch Wrapper
// Relative API URLs - communicates directly with Spring Boot REST Controllers on the same origin

const API_BASE_URL = '';

/**
 * Universal fetch wrapper for DonateHub backend REST APIs
 * @param {string} endpoint - API path, e.g. '/donors' or '/drives/1'
 * @param {object} options - Fetch options (method, body, headers)
 * @returns {Promise<any>}
 */
async function apiFetch(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const config = {
        method: options.method || 'GET',
        headers: {
            'Accept': 'application/json',
            ...(options.headers || {})
        }
    };

    if (options.body) {
        if (typeof options.body === 'object') {
            config.headers['Content-Type'] = 'application/json';
            config.body = JSON.stringify(options.body);
        } else {
            config.body = options.body;
        }
    }

    try {
        const response = await fetch(url, config);

        // Handle 204 No Content
        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get('content-type');
        let data = null;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            let errorMessage = `Error ${response.status}: `;
            if (typeof data === 'object' && data !== null) {
                errorMessage += data.message || data.error || JSON.stringify(data);
            } else if (typeof data === 'string' && data.length > 0) {
                errorMessage += data;
            } else {
                errorMessage += response.statusText || 'Request failed';
            }
            throw new Error(errorMessage);
        }

        return data;
    } catch (err) {
        console.error(`API Call Failed: [${config.method}] ${url}`, err);
        throw err;
    }
}

/**
 * Displays user-friendly alert message
 * @param {string} message 
 * @param {'success'|'error'} type 
 */
function showAlert(message, type = 'success') {
    const container = document.getElementById('alert-container');
    if (!container) return;

    const alertClass = type === 'success' ? 'alert-success' : 'alert-error';
    const alertEl = document.createElement('div');
    alertEl.className = `alert ${alertClass}`;
    alertEl.innerHTML = `
        <span>${escapeHtml(message)}</span>
        <button type="button" class="alert-close" onclick="this.parentElement.remove()">&times;</button>
    `;

    container.appendChild(alertEl);

    // Auto dismiss after 4 seconds
    setTimeout(() => {
        if (alertEl.parentElement) {
            alertEl.remove();
        }
    }, 4000);
}

/**
 * Simple HTML escaping to prevent XSS in table displays
 */
function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/**
 * Open Modal by ID
 */
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.style.display = 'flex';
    }
}

/**
 * Close Modal by ID
 */
function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Close modal when clicking outside modal-card
window.addEventListener('click', (e) => {
    if (e.target && e.target.classList.contains('modal-backdrop')) {
        e.target.style.display = 'none';
    }
});
