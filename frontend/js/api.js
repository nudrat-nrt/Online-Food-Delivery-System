// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';

// API Helper Functions
class ApiService {
    constructor() {
        this.baseUrl = API_BASE_URL;
    }

    async request(endpoint, method = 'GET', data = null, authToken = null) {
        const url = `${this.baseUrl}${endpoint}`;
        
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            },
        };
        
        if (authToken) {
            options.headers['Authorization'] = `Bearer ${authToken}`;
        }
        
        if (data) {
            options.body = JSON.stringify(data);
        }
        
        try {
            const response = await fetch(url, options);
            
            // Handle HTTP errors
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw {
                    status: response.status,
                    message: errorData.message || `HTTP ${response.status}`,
                    data: errorData
                };
            }
            
            // Parse response
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            console.error('API Request failed:', error);
            throw error;
        }
    }

    // User Authentication
    async login(username, password) {
        return await this.request('/login', 'POST', { username, password });
    }

    async register(userData) {
        return await this.request('/register', 'POST', userData);
    }

    async getUserProfile(username) {
        return await this.request(`/user/profile?username=${username}`, 'GET');
    }

    // Menu Operations
    async getMenu() {
        return await this.request('/menu', 'GET');
    }

    async getCategories() {
        return await this.request('/categories', 'GET');
    }

    async getMenuItem(id) {
        return await this.request(`/menu/${id}`, 'GET');
    }

    // Cart Operations
    async addToCart(sessionId, foodData) {
        return await this.request('/cart/add', 'POST', {
            sessionId,
            ...foodData
        });
    }

    async getCart(sessionId) {
        return await this.request(`/cart?sessionId=${sessionId}`, 'GET');
    }

    async clearCart(sessionId) {
        return await this.request('/cart/clear', 'POST', { sessionId });
    }

    // Order Operations
    async placeOrder(orderData) {
        return await this.request('/order', 'POST', orderData);
    }

    async getOrders(username) {
        return await this.request(`/order?username=${username}`, 'GET');
    }

    // Test endpoint
    async testConnection() {
        return await this.request('/test', 'GET');
    }
}

// Create global API instance
window.api = new ApiService();

// Session management
window.sessionManager = {
    getSessionId() {
        return localStorage.getItem('sessionId');
    },
    
    setSessionId(sessionId) {
        localStorage.setItem('sessionId', sessionId);
    },
    
    clearSession() {
        localStorage.removeItem('sessionId');
        localStorage.removeItem('currentUser');
    },
    
    getCurrentUser() {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    },
    
    setCurrentUser(user) {
        localStorage.setItem('currentUser', JSON.stringify(user));
    },
    
    isLoggedIn() {
        return !!this.getCurrentUser() && !!this.getSessionId();
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Test API connection
    api.testConnection()
        .then(data => {
            console.log('✅ API Connection successful:', data);
            
            // Update status indicator if exists
            const statusEl = document.getElementById('api-status');
            if (statusEl) {
                statusEl.innerHTML = `
                    <span style="color: #00b894;">●</span>
                    <span>API Connected (Users: ${data.userCount || 0}, Menu: ${data.menuItemCount || 0})</span>
                `;
            }
        })
        .catch(error => {
            console.error('❌ API Connection failed:', error);
            
            // Update status indicator if exists
            const statusEl = document.getElementById('api-status');
            if (statusEl) {
                statusEl.innerHTML = `
                    <span style="color: #ff4757;">●</span>
                    <span>API Connection Failed - Check if server is running on port 8080</span>
                `;
            }
            
            // Show warning
            if (!window.location.pathname.includes('login.html')) {
                showNotification('Backend server is not running. Please start the Java server.', 'error', 10000);
            }
        });
});