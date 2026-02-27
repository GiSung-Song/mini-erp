/**
 * Common fetch wrapper for API calls.
 * It handles the custom response structure { success, data, error }.
 * @param {string} url - The API endpoint to call.
 * @param {object} [options={}] - Standard fetch options (method, headers, body, etc.).
 * @returns {Promise<any>} - A promise that resolves with the `data` property of the successful API response.
 * @throws {ApiError} - Throws a custom error object if the API returns a failure response.
 */
async function fetchApi(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    const config = {
        ...options,
        headers,
    };

    // Stringify body if it's a JavaScript object
    if (options.body && typeof options.body === 'object') {
        config.body = JSON.stringify(options.body);
    }

    try {
        const response = await fetch(url, config);
        const responseData = await response.json();

        // Check the custom success flag from the backend
        if (responseData.success) {
            logToUI(`${config.method || 'GET'} ${url}`, 'SUCCESS');
            return responseData.data; // This could be the main data or a PageResponse object
        } else {
            // Handle business logic errors returned by the API
            const apiError = new ApiError(responseData.error);
            logToUI(`[${apiError.code}] ${apiError.message}`, 'ERROR');
            throw apiError;
        }

    } catch (error) {
        // Handle network errors or if the fetch/JSON parsing itself fails
        if (!(error instanceof ApiError)) {
            logToUI(`Network or parsing error for ${url}. ${error.message}`, 'ERROR');
        }
        // Re-throw the error to be caught by the calling function
        throw error;
    }
}

/**
 * Custom error class to represent errors returned from the backend API.
 */
class ApiError extends Error {
    constructor(errorData) {
        super(errorData.message || 'An unknown API error occurred.');
        this.name = 'ApiError';
        this.code = errorData.code; // e.g., 'INVALID_REQUEST'
        this.details = errorData.details; // e.g., { fields: { name: "must not be blank" } }
    }
}


// Example logToSystem function (to be implemented in the main dashboard JS)
/*
function logToSystem(message) {
    const logContainer = document.querySelector('.system-log-content'); // A more robust selector
    if (logContainer) {
        const entry = document.createElement('div');
        entry.className = 'text-sm mb-2';
        
        const timestamp = new Date().toLocaleTimeString();
        let color = 'text-gray-600';
        if (message.startsWith('ERROR') || message.startsWith('FATAL')) {
            color = 'text-red-500';
        } else if (message.startsWith('SUCCESS')) {
            color = 'text-green-500';
        }

        entry.innerHTML = `<span class="${color}">[${timestamp}] ${message}</span>`;
        logContainer.prepend(entry);
    }
}
*/
