// Main JavaScript file

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('Food Delivery System - Frontend Loaded');
    
    // Check login status
    const currentUser = sessionManager.getCurrentUser();
    if (currentUser) {
        updateUserUI(currentUser);
    }
    
    // Initialize cart
    updateCartCount();
    
    // Set up event listeners
    setupEventListeners();
    
    // Load menu if on menu page
    if (window.location.pathname.includes('menu.html')) {
        loadMenu();
    }
    
    // Load cart if on cart page
    if (window.location.pathname.includes('cart.html')) {
        loadCart();
    }
    
    // Load orders if on profile page
    if (window.location.pathname.includes('profile.html')) {
        loadUserProfile();
        loadOrderHistory();
    }
});

// Setup event listeners
function setupEventListeners() {
    // Login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    
    // Registration form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
    
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }
    
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    // Filter buttons
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            handleFilter(this.dataset.category);
        });
    });
}

// Handle login
async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
    submitBtn.disabled = true;
    
    try {
        const result = await api.login(username, password);
        
        if (result.success) {
            // Store session and user info
            sessionManager.setSessionId(result.sessionId);
            sessionManager.setCurrentUser({
                username: result.username,
                role: result.role,
                email: result.email
            });
            
            showNotification('Login successful!', 'success');
            
            // Redirect based on role
            setTimeout(() => {
                if (result.role === 'ADMIN') {
                    window.location.href = '/admin/dashboard.html';
                } else {
                    window.location.href = '/menu.html';
                }
            }, 1500);
        } else {
            showNotification(result.message || 'Login failed', 'error');
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    } catch (error) {
        console.error('Login error:', error);
        showNotification('Connection error. Please check if server is running.', 'error');
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

// Handle registration
async function handleRegister(e) {
    e.preventDefault();
    
    const formData = {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value,
        email: document.getElementById('email').value,
        fullName: document.getElementById('fullName').value,
        phone: document.getElementById('phone').value
    };
    
    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating account...';
    submitBtn.disabled = true;
    
    try {
        const result = await api.register(formData);
        
        if (result.success) {
            showNotification('Account created successfully! Please login.', 'success');
            
            // Redirect to login after 2 seconds
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showNotification(result.message || 'Registration failed', 'error');
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    } catch (error) {
        console.error('Registration error:', error);
        showNotification('Connection error. Please try again.', 'error');
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

// Handle logout
function handleLogout() {
    sessionManager.clearSession();
    showNotification('Logged out successfully', 'info');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1000);
}

// Load menu from API
async function loadMenu() {
    const container = document.getElementById('menu-container');
    if (!container) return;
    
    container.innerHTML = `
        <div class="loading">
            <i class="fas fa-spinner fa-spin fa-2x"></i>
            <p>Loading delicious menu...</p>
        </div>
    `;
    
    try {
        const menuItems = await api.getMenu();
        displayMenu(menuItems);
    } catch (error) {
        console.error('Failed to load menu:', error);
        container.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Failed to load menu</h3>
                <p>Please check if the server is running</p>
                <button onclick="loadMenu()" class="btn-secondary">
                    <i class="fas fa-redo"></i> Try Again
                </button>
            </div>
        `;
    }
}

// Display menu items
function displayMenu(items) {
    const container = document.getElementById('menu-container');
    
    if (!items || items.length === 0) {
        container.innerHTML = '<p class="empty-message">No menu items available</p>';
        return;
    }
    
    container.innerHTML = items.map(item => `
        <div class="menu-card" data-category="${item.category}" data-id="${item.id}">
            <div class="menu-card-image">
                <img src="${item.imageUrl || 'images/food/default.jpg'}" alt="${item.name}">
                ${item.vegetarian ? '<span class="veg-badge"><i class="fas fa-leaf"></i> Veg</span>' : ''}
                <span class="price-badge">$${item.price.toFixed(2)}</span>
            </div>
            <div class="menu-card-content">
                <h3>${item.name}</h3>
                <p class="description">${item.description}</p>
                <div class="menu-card-footer">
                    <span class="category">${item.category}</span>
                    <button class="btn-primary add-to-cart" 
                            onclick="addToCart(${item.id}, '${item.factoryType}', '${item.name.replace("'", "\\'")}')">
                        <i class="fas fa-plus"></i> Add to Cart
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// Add item to cart
async function addToCart(itemId, foodType, itemName) {
    if (!sessionManager.isLoggedIn()) {
        showNotification('Please login first to add items to cart', 'warning');
        setTimeout(() => {
            window.location.href = 'login.html?redirect=menu.html';
        }, 1500);
        return;
    }
    
    const sessionId = sessionManager.getSessionId();
    
    try {
        const result = await api.addToCart(sessionId, {
            itemId: itemId,
            foodType: foodType,
            quantity: 1
        });
        
        if (result.success) {
            showNotification(`${itemName} added to cart!`, 'success');
            updateCartCount(result.itemCount);
        } else {
            showNotification('Failed to add item to cart', 'error');
        }
    } catch (error) {
        console.error('Add to cart error:', error);
        showNotification('Failed to add item to cart', 'error');
    }
}

// Load cart from API
async function loadCart() {
    const container = document.getElementById('cart-container');
    const summary = document.getElementById('cart-summary');
    
    if (!sessionManager.isLoggedIn()) {
        container.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-shopping-cart fa-3x"></i>
                <h3>Please login to view your cart</h3>
                <a href="login.html?redirect=cart.html" class="btn-primary">Login</a>
            </div>
        `;
        return;
    }
    
    const sessionId = sessionManager.getSessionId();
    
    container.innerHTML = `
        <div class="loading">
            <i class="fas fa-spinner fa-spin fa-2x"></i>
            <p>Loading your cart...</p>
        </div>
    `;
    
    try {
        const cartData = await api.getCart(sessionId);
        displayCart(cartData);
    } catch (error) {
        console.error('Failed to load cart:', error);
        container.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Failed to load cart</h3>
                <p>${error.message}</p>
            </div>
        `;
    }
}

// Display cart items
function displayCart(cartData) {
    const container = document.getElementById('cart-container');
    const summary = document.getElementById('cart-summary');
    
    if (!cartData.items || cartData.items.length === 0) {
        container.innerHTML = `
            <div class="empty-cart">
                <i class="fas fa-shopping-cart fa-3x"></i>
                <h3>Your cart is empty</h3>
                <p>Add some delicious items from our menu!</p>
                <a href="menu.html" class="btn-primary">Browse Menu</a>
            </div>
        `;
        
        summary.innerHTML = '';
        return;
    }
    
    container.innerHTML = cartData.items.map((item, index) => `
        <div class="cart-item">
            <div class="cart-item-info">
                <h4>${item.name}</h4>
                <p>$${item.price.toFixed(2)} each</p>
            </div>
            <div class="cart-item-controls">
                <button class="quantity-btn" onclick="updateQuantity(${index}, -1)">-</button>
                <span class="quantity">${item.quantity}</span>
                <button class="quantity-btn" onclick="updateQuantity(${index}, 1)">+</button>
                <span class="item-total">$${item.total.toFixed(2)}</span>
                <button class="remove-btn" onclick="removeItem(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    summary.innerHTML = `
        <div class="summary-row">
            <span>Subtotal:</span>
            <span>$${cartData.subtotal.toFixed(2)}</span>
        </div>
        <div class="summary-row">
            <span>Delivery Fee:</span>
            <span>$${cartData.deliveryFee.toFixed(2)}</span>
        </div>
        <div class="summary-row">
            <span>Tax:</span>
            <span>$${cartData.tax.toFixed(2)}</span>
        </div>
        <div class="summary-row total">
            <span>Total:</span>
            <span>$${cartData.total.toFixed(2)}</span>
        </div>
        <button class="btn-primary btn-checkout" onclick="checkout()">
            <i class="fas fa-lock"></i> Proceed to Checkout
        </button>
    `;
}

// Update cart item quantity
async function updateQuantity(index, change) {
    // Implementation would update cart in backend
    showNotification('Quantity updated', 'info');
}

// Remove item from cart
async function removeItem(index) {
    // Implementation would remove item from backend cart
    showNotification('Item removed from cart', 'info');
}

// Checkout process
async function checkout() {
    if (!sessionManager.isLoggedIn()) {
        window.location.href = 'login.html?redirect=checkout.html';
        return;
    }
    
    window.location.href = 'checkout.html';
}

// Update cart count in navbar
function updateCartCount(count = null) {
    const cartCountElements = document.querySelectorAll('.cart-count');
    
    if (count !== null) {
        cartCountElements.forEach(el => {
            el.textContent = count;
            el.style.display = count > 0 ? 'flex' : 'none';
        });
    } else {
        // Get cart count from API
        if (sessionManager.isLoggedIn()) {
            const sessionId = sessionManager.getSessionId();
            api.getCart(sessionId)
                .then(cartData => {
                    const itemCount = cartData.itemCount || 0;
                    cartCountElements.forEach(el => {
                        el.textContent = itemCount;
                        el.style.display = itemCount > 0 ? 'flex' : 'none';
                    });
                })
                .catch(() => {
                    cartCountElements.forEach(el => {
                        el.style.display = 'none';
                    });
                });
        }
    }
}

// Update user UI
function updateUserUI(user) {
    const userElements = document.querySelectorAll('.user-display');
    userElements.forEach(el => {
        if (el.tagName === 'SPAN') {
            el.textContent = user.username;
        }
    });
    
    // Show/hide login/logout buttons
    const loginBtn = document.querySelector('.login-btn');
    const logoutBtn = document.querySelector('.logout-btn');
    
    if (loginBtn) loginBtn.style.display = 'none';
    if (logoutBtn) logoutBtn.style.display = 'block';
}

// Show notification
function showNotification(message, type = 'info', duration = 3000) {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        info: 'fa-info-circle',
        warning: 'fa-exclamation-triangle'
    };
    
    notification.innerHTML = `
        <i class="fas ${icons[type] || 'fa-info-circle'}"></i>
        <span>${message}</span>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Remove after duration
    setTimeout(() => {
        notification.classList.add('hide');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 300);
    }, duration);
}

// Load user profile
async function loadUserProfile() {
    const user = sessionManager.getCurrentUser();
    if (!user) {
        window.location.href = 'login.html?redirect=profile.html';
        return;
    }
    
    try {
        const profile = await api.getUserProfile(user.username);
        
        // Update profile display
        const profileEl = document.getElementById('profile-info');
        if (profileEl) {
            profileEl.innerHTML = `
                <div class="profile-field">
                    <label>Username:</label>
                    <span>${profile.username}</span>
                </div>
                <div class="profile-field">
                    <label>Email:</label>
                    <span>${profile.email || 'Not set'}</span>
                </div>
                <div class="profile-field">
                    <label>Phone:</label>
                    <span>${profile.phone || 'Not set'}</span>
                </div>
                <div class="profile-field">
                    <label>Role:</label>
                    <span class="role-badge ${profile.role.toLowerCase()}">${profile.role}</span>
                </div>
            `;
        }
    } catch (error) {
        console.error('Failed to load profile:', error);
    }
}

// Load order history
async function loadOrderHistory() {
    const user = sessionManager.getCurrentUser();
    if (!user) return;
    
    try {
        const orders = await api.getOrders(user.username);
        
        const container = document.getElementById('order-history');
        if (!container) return;
        
        if (!orders || orders.length === 0) {
            container.innerHTML = `
                <div class="empty-orders">
                    <i class="fas fa-shopping-bag fa-3x"></i>
                    <h3>No orders yet</h3>
                    <p>Your order history will appear here</p>
                    <a href="menu.html" class="btn-primary">Start Ordering</a>
                </div>
            `;
            return;
        }
        
        container.innerHTML = orders.map(order => `
            <div class="order-card">
                <div class="order-header">
                    <h4>Order #${order.orderNumber}</h4>
                    <span class="order-status ${order.status.toLowerCase()}">${order.status}</span>
                </div>
                <div class="order-details">
                    <p><strong>Date:</strong> ${new Date(order.createdAt).toLocaleDateString()}</p>
                    <p><strong>Total:</strong> $${order.totalAmount.toFixed(2)}</p>
                    <p><strong>Address:</strong> ${order.deliveryAddress.substring(0, 50)}...</p>
                </div>
                <button class="btn-secondary" onclick="viewOrderDetails(${order.orderId})">
                    View Details
                </button>
            </div>
        `).join('');
    } catch (error) {
        console.error('Failed to load orders:', error);
    }
}

// View order details
function viewOrderDetails(orderId) {
    alert(`Order details for #${orderId} would be shown here`);
}