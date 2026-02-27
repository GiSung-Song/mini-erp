/**
 * Logs a message to the system log panel in the UI.
 * @param {string} message - The message to log.
 * @param {'SUCCESS'|'ERROR'|'INFO'|'WARN'} type - The type of the log, which determines its color.
 */
function logToUI(message, type = 'INFO') {
    const logContainer = document.getElementById('system-log-container');
    if (!logContainer) {
        console.error('System log container not found in the DOM.');
        return;
    }

    const entry = document.createElement('div');
    const timestamp = new Date().toLocaleTimeString();
    
    let colorClass = 'text-gray-700'; // Default for INFO
    let typeLabel = 'INFO';

    switch (type.toUpperCase()) {
        case 'SUCCESS':
            colorClass = 'text-green-600';
            typeLabel = 'SUCCESS';
            break;
        case 'ERROR':
            colorClass = 'text-red-600';
            typeLabel = 'ERROR';
            break;
        case 'WARN':
            colorClass = 'text-yellow-600';
            typeLabel = 'WARN';
            break;
    }

    entry.className = `p-1.5 text-sm font-mono border-b border-gray-200`;
    entry.innerHTML = `
        <span class="font-semibold ${colorClass}">[${typeLabel}]</span>
        <span class="text-gray-500 text-xs mx-1">${timestamp}</span>
        <span class="block text-gray-800">${message}</span>
    `;

    // Add the new log to the top of the container
    logContainer.prepend(entry);
}

// Initial log to confirm the system is ready
document.addEventListener('DOMContentLoaded', () => {
    logToUI('Dashboard initialized. Ready for operations.', 'INFO');
});
