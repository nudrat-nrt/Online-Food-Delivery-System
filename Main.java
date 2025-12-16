

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import Database.*;
import FoodItem.*;
import Cart.*;
import Login.*;
import Order.*;
import Database.OrderDAO;
import Database.FoodItemDAO;
import Order.OrderEntity;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.sql.*;

public class Main {
    private static Gson gson = new Gson();
    private static UserDAO userDAO = new UserDAO();
    private static FoodItemDAO foodDAO = new FoodItemDAO();
    private static OrderDAO orderDAO = new OrderDAO();
    
    // Store active carts in memory (session-based)
    private static Map<String, Cart> userCarts = new HashMap<>();
    
    public static void main(String[] args) throws IOException {
        System.out.println(getAsciiArt());
        System.out.println("🚀 Starting Food Delivery System Backend Server...");
        
        // Initialize database
        initializeDatabase();
        
        // Create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("🌐 HTTP Server created on port 8080");
        
        // Set up request handler
        server.createContext("/", exchange -> {
            try {
                // Handle preflight requests
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    setCorsHeaders(exchange);
                    exchange.sendResponseHeaders(200, -1);
                    return;
                }
                
                String path = exchange.getRequestURI().getPath();
                
                // API endpoints
                if (path.startsWith("/api/")) {
                    handleApiRequest(exchange);
                } else {
                    // Serve static files from frontend folder
                    serveStaticFile(exchange);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        });
        
        server.setExecutor(null);
        server.start();
        
        displayStartupInfo();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🔴 Shutting down server...");
            SQLiteConnection.getInstance().closeConnection();
            System.out.println("👋 Goodbye!");
        }));
    }
    
    private static String getAsciiArt() {
        return """
               ╔═══════════════════════════════════════════════════════════════════╗
               ║                    🍕 FOOD DELIVERY SYSTEM 🍔                    ║
               ║                      DATABASE EDITION v1.0                       ║
               ╚═══════════════════════════════════════════════════════════════════╝
               """;
    }
    
    private static void initializeDatabase() {
        System.out.println("🗄️  Initializing database...");
        DatabaseInitializer.initializeDatabase();
        
        // Test database connection
        try {
            Connection conn = SQLiteConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful");
                
                // Count users and menu items
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next()) {
                    System.out.println("👤 Users in database: " + rs.getInt(1));
                }
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM food_items");
                if (rs.next()) {
                    System.out.println("🍕 Menu items in database: " + rs.getInt(1));
                }
                
                stmt.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
        }
    }
    
    private static void displayStartupInfo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ BACKEND SERVER STARTED SUCCESSFULLY!");
        System.out.println("=".repeat(60));
        System.out.println("📡 Server URL: http://localhost:8080");
        System.out.println("🔌 API Base: http://localhost:8080/api");
        System.out.println("📁 Frontend: http://localhost:8080/index.html");
        System.out.println("🗄️  Database: database/food_delivery.db");
        System.out.println("\n📋 AVAILABLE ENDPOINTS:");
        System.out.println("   • GET  /api/test          - Health check");
        System.out.println("   • POST /api/login         - User login");
        System.out.println("   • POST /api/register      - User registration");
        System.out.println("   • GET  /api/menu          - Get all menu items");
        System.out.println("   • GET  /api/categories    - Get categories");
        System.out.println("   • POST /api/cart/add      - Add item to cart");
        System.out.println("   • GET  /api/cart          - Get cart items");
        System.out.println("   • POST /api/cart/clear    - Clear cart");
        System.out.println("   • POST /api/order         - Place order");
        System.out.println("   • GET  /api/orders        - Get user orders");
        System.out.println("   • GET  /api/user/profile  - Get user profile");
        System.out.println("\n👤 DEFAULT USERS:");
        System.out.println("   • Username: admin");
        System.out.println("   • Password: 1234");
        System.out.println("   • Role: ADMIN");
        System.out.println("\n🛑 Press Ctrl+C to stop the server");
        System.out.println("=".repeat(60) + "\n");
    }
    
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }
    
    private static void handleApiRequest(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        System.out.println("📨 " + method + " " + path);
        
        try {
            switch (path) {
                case "/api/test":
                    handleTestEndpoint(exchange);
                    break;
                case "/api/login":
                    if ("POST".equals(method)) handleLogin(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/register":
                    if ("POST".equals(method)) handleRegister(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/menu":
                    if ("GET".equals(method)) handleGetMenu(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/categories":
                    if ("GET".equals(method)) handleGetCategories(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/cart/add":
                    if ("POST".equals(method)) handleAddToCart(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/cart":
                    if ("GET".equals(method)) handleGetCart(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/cart/clear":
                    if ("POST".equals(method)) handleClearCart(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/order":
                    if ("POST".equals(method)) handlePlaceOrder(exchange);
                    else if ("GET".equals(method)) handleGetOrders(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                case "/api/user/profile":
                    if ("GET".equals(method)) handleGetUserProfile(exchange);
                    else sendErrorResponse(exchange, 405, "Method not allowed");
                    break;
                default:
                    // Check for dynamic routes like /api/menu/{id}
                    if (path.startsWith("/api/menu/")) {
                        if ("GET".equals(method)) handleGetMenuItem(exchange, path);
                        else sendErrorResponse(exchange, 405, "Method not allowed");
                    } else {
                        sendErrorResponse(exchange, 404, "API endpoint not found");
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }
    
    private static void handleTestEndpoint(HttpExchange exchange) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("status", "online");
        response.addProperty("service", "Food Delivery Backend");
        response.addProperty("database", "SQLite");
        response.addProperty("timestamp", System.currentTimeMillis());
        response.addProperty("version", "1.0.0");
        
        try {
            Connection conn = SQLiteConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                response.addProperty("databaseStatus", "connected");
                
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next()) response.addProperty("userCount", rs.getInt(1));
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM food_items");
                if (rs.next()) response.addProperty("menuItemCount", rs.getInt(1));
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
                if (rs.next()) response.addProperty("orderCount", rs.getInt(1));
                
                stmt.close();
            }
        } catch (SQLException e) {
            response.addProperty("databaseStatus", "error: " + e.getMessage());
        }
        
        sendJsonResponse(exchange, 200, response.toString());
    }
    
    private static void handleLogin(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        
        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();
        
        System.out.println("🔐 Login attempt for: " + username);
        
        // Authenticate user from database
        User user = userDAO.authenticate(username, password);
        
        JsonObject response = new JsonObject();
        if (user != null) {
            // Generate session ID
            String sessionId = generateSessionId(username);
            
            // Initialize cart for this session
            userCarts.put(sessionId, new Cart());
            userCarts.get(sessionId).setStrategy(new NormalTotal());
            
            response.addProperty("success", true);
            response.addProperty("message", "Login successful");
            response.addProperty("sessionId", sessionId);
            response.addProperty("username", user.getUsername());
            response.addProperty("role", user.getRole());
            response.addProperty("email", user.getEmail() != null ? user.getEmail() : "");
            
            System.out.println("✅ Login successful: " + username + " (Role: " + user.getRole() + ")");
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid username or password");
            System.out.println("❌ Login failed: " + username);
        }
        
        sendJsonResponse(exchange, 200, response.toString());
    }
    
    private static void handleRegister(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        
        String username = json.get("username").getAsString();
        String password = json.get("password").getAsString();
        String email = json.get("email").getAsString();
        String fullName = json.has("fullName") ? json.get("fullName").getAsString() : username;
        String phone = json.has("phone") ? json.get("phone").getAsString() : "";
        
        System.out.println("📝 Registration attempt: " + username);
        
        JsonObject response = new JsonObject();
        
        // Validate input
        if (username.length() < 3) {
            response.addProperty("success", false);
            response.addProperty("message", "Username must be at least 3 characters");
        } else if (password.length() < 4) {
            response.addProperty("success", false);
            response.addProperty("message", "Password must be at least 4 characters");
        } else if (!email.contains("@")) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid email address");
        } else if (userDAO.usernameExists(username)) {
            response.addProperty("success", false);
            response.addProperty("message", "Username already exists");
        } else if (userDAO.emailExists(email)) {
            response.addProperty("success", false);
            response.addProperty("message", "Email already registered");
        } else {
            // Create new user
            User newUser = new User(username, password, "CUSTOMER");
            newUser.setEmail(email);
            
            if (userDAO.registerUser(newUser, email, fullName, phone)) {
                response.addProperty("success", true);
                response.addProperty("message", "Registration successful");
                response.addProperty("username", username);
                response.addProperty("email", email);
                response.addProperty("role", "CUSTOMER");
                System.out.println("✅ New user registered: " + username);
            } else {
                response.addProperty("success", false);
                response.addProperty("message", "Registration failed - database error");
            }
        }
        
        sendJsonResponse(exchange, 200, response.toString());
    }
    
    private static void handleGetMenu(HttpExchange exchange) throws IOException {
        // Get menu items from database
        List<FoodItemEntity> menuItems = foodDAO.getAllAvailableItems();
        
        // Convert to JSON
        List<Map<String, Object>> responseItems = new ArrayList<>();
        for (FoodItemEntity item : menuItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getItemId());
            itemMap.put("name", item.getName());
            itemMap.put("description", item.getDescription());
            itemMap.put("price", item.getPrice());
            itemMap.put("category", item.getCategoryName());
            itemMap.put("available", item.isAvailable());
            itemMap.put("vegetarian", item.isVegetarian());
            
            // Map to factory types for cart operations
            String factoryType = getFactoryType(item.getCategoryName());
            itemMap.put("factoryType", factoryType);
            
            // Add image URL (placeholder for now)
            itemMap.put("imageUrl", "/images/food/" + factoryType + ".jpg");
            
            responseItems.add(itemMap);
        }
        
        sendJsonResponse(exchange, 200, gson.toJson(responseItems));
        System.out.println("📋 Sent menu: " + menuItems.size() + " items");
    }
    
    private static void handleGetMenuItem(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length < 4) {
            sendErrorResponse(exchange, 400, "Invalid menu item ID");
            return;
        }
        
        try {
            int itemId = Integer.parseInt(parts[3]);
            FoodItemEntity item = foodDAO.getItemById(itemId);
            
            if (item == null) {
                sendErrorResponse(exchange, 404, "Menu item not found");
                return;
            }
            
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getItemId());
            itemMap.put("name", item.getName());
            itemMap.put("description", item.getDescription());
            itemMap.put("price", item.getPrice());
            itemMap.put("category", item.getCategoryName());
            itemMap.put("available", item.isAvailable());
            itemMap.put("vegetarian", item.isVegetarian());
            itemMap.put("factoryType", getFactoryType(item.getCategoryName()));
            itemMap.put("imageUrl", "/images/food/" + getFactoryType(item.getCategoryName()) + ".jpg");
            
            sendJsonResponse(exchange, 200, gson.toJson(itemMap));
            System.out.println("📋 Sent menu item: " + item.getName());
            
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid menu item ID format");
        }
    }
    
    private static void handleGetCategories(HttpExchange exchange) throws IOException {
        List<FoodItemDAO.Category> categories = foodDAO.getAllCategories();
        
        List<Map<String, Object>> responseCategories = new ArrayList<>();
        for (FoodItemDAO.Category category : categories) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getCategoryId());
            categoryMap.put("name", category.getName());
            categoryMap.put("description", category.getDescription());
            categoryMap.put("imageUrl", category.getImageUrl());
            responseCategories.add(categoryMap);
        }
        
        sendJsonResponse(exchange, 200, gson.toJson(responseCategories));
        System.out.println("📋 Sent categories: " + categories.size() + " categories");
    }
    
    private static void handleAddToCart(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        
        String sessionId = json.get("sessionId").getAsString();
        String foodType = json.get("foodType").getAsString();
        int quantity = json.get("quantity").getAsInt();
        int itemId = json.has("itemId") ? json.get("itemId").getAsInt() : -1;
        
        // Get cart for session
        Cart cart = userCarts.get(sessionId);
        if (cart == null) {
            sendErrorResponse(exchange, 401, "Session expired. Please login again.");
            return;
        }
        
        // Get item details from database if itemId is provided
        double price = 0.0;
        String itemName = foodType;
        
        if (itemId > 0) {
            FoodItemEntity item = foodDAO.getItemById(itemId);
            if (item != null) {
                price = item.getPrice();
                itemName = item.getName();
            }
        }
        
        // Create food item using factory
        FoodItem foodItem = FoodFactory.createFood(foodType);
        if (foodItem == null) {
            sendErrorResponse(exchange, 400, "Invalid food type: " + foodType);
            return;
        }
        
        // Override price if we have it from database
        if (price > 0) {
            // We need to modify FoodItem interface or create a new class
            // For now, use the factory price
        }
        
        // Add to cart
        cart.addItem(foodItem, quantity);
        
        // Prepare response
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", itemName + " added to cart");
        response.addProperty("cartTotal", cart.getTotal());
        response.addProperty("itemCount", cart.getItems().size());
        
        sendJsonResponse(exchange, 200, response.toString());
        System.out.println("🛒 Added to cart: " + quantity + "x " + foodType + " for session " + sessionId);
    }
    
    private static void handleGetCart(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String sessionId = getQueryParam(query, "sessionId");
        
        if (sessionId == null || sessionId.isEmpty()) {
            sendErrorResponse(exchange, 400, "sessionId parameter required");
            return;
        }
        
        Cart cart = userCarts.get(sessionId);
        if (cart == null) {
            // Return empty cart
            Map<String, Object> emptyCart = new HashMap<>();
            emptyCart.put("items", new ArrayList<>());
            emptyCart.put("subtotal", 0.0);
            emptyCart.put("deliveryFee", 2.99);
            emptyCart.put("tax", 0.0);
            emptyCart.put("total", 2.99);
            emptyCart.put("itemCount", 0);
            
            sendJsonResponse(exchange, 200, gson.toJson(emptyCart));
            return;
        }
        
        // Calculate totals
        cart.setStrategy(new NormalTotal());
        double subtotal = cart.getTotal();
        double deliveryFee = 2.99;
        double tax = subtotal * 0.10;
        double total = subtotal + deliveryFee + tax;
        
        // Prepare cart items
        List<Map<String, Object>> cartItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", cartItem.getFood().getDescription());
            item.put("price", cartItem.getFood().getPrice());
            item.put("quantity", cartItem.getQuantity());
            item.put("total", cartItem.getTotalPrice());
            cartItems.add(item);
        }
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("items", cartItems);
        response.put("subtotal", subtotal);
        response.put("deliveryFee", deliveryFee);
        response.put("tax", tax);
        response.put("total", total);
        response.put("itemCount", cartItems.size());
        
        sendJsonResponse(exchange, 200, gson.toJson(response));
        System.out.println("🛒 Sent cart: " + cartItems.size() + " items for session " + sessionId);
    }
    
    private static void handleClearCart(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        String sessionId = json.get("sessionId").getAsString();
        
        Cart cart = userCarts.get(sessionId);
        if (cart != null) {
            int itemCount = cart.getItems().size();
            cart.clear();
            System.out.println("🗑️  Cart cleared: " + itemCount + " items removed from session " + sessionId);
        }
        
        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Cart cleared");
        
        sendJsonResponse(exchange, 200, response.toString());
    }
    
    private static void handlePlaceOrder(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(requestBody).getAsJsonObject();
        
        String sessionId = json.get("sessionId").getAsString();
        String username = json.get("username").getAsString();
        String deliveryAddress = json.get("address").getAsString();
        String phoneNumber = json.has("phone") ? json.get("phone").getAsString() : "Not provided";
        String paymentMethod = json.has("paymentMethod") ? json.get("paymentMethod").getAsString() : "Cash on Delivery";
        
        Cart cart = userCarts.get(sessionId);
        if (cart == null || cart.getItems().isEmpty()) {
            sendErrorResponse(exchange, 400, "Cart is empty");
            return;
        }
        
        // Place order using OrderDAO
        int orderId = orderDAO.createOrder(cart, username, deliveryAddress, phoneNumber);
        
        JsonObject response = new JsonObject();
        if (orderId > 0) {
            // Clear cart after successful order
            cart.clear();
            
            response.addProperty("success", true);
            response.addProperty("orderId", orderId);
            response.addProperty("message", "Order placed successfully!");
            response.addProperty("total", cart.getTotal());
            
            sendJsonResponse(exchange, 200, response.toString());
            
            System.out.println("✅ Order #" + orderId + " placed for user: " + username);
            System.out.println("   Address: " + deliveryAddress);
            System.out.println("   Payment: " + paymentMethod);
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Failed to place order");
            sendJsonResponse(exchange, 500, response.toString());
            System.out.println("❌ Failed to place order for user: " + username);
        }
    }
    
    private static void handleGetOrders(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String username = getQueryParam(query, "username");
        
        if (username == null || username.isEmpty()) {
            sendErrorResponse(exchange, 400, "username parameter required");
            return;
        }
        
        // Get orders from OrderDAO
        List<OrderEntity> orders = orderDAO.getUserOrders(username);
        
        // Convert to JSON
        List<Map<String, Object>> responseOrders = new ArrayList<>();
        for (OrderEntity order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", order.getOrderId());
            orderMap.put("orderNumber", order.getOrderNumber());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus().toString());
            orderMap.put("deliveryAddress", order.getDeliveryAddress());
            orderMap.put("createdAt", order.getCreatedAt());
            responseOrders.add(orderMap);
        }
        
        sendJsonResponse(exchange, 200, gson.toJson(responseOrders));
        System.out.println("📋 Sent orders: " + orders.size() + " orders for user " + username);
    }
    
    private static void handleGetUserProfile(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String username = getQueryParam(query, "username");
        
        if (username == null || username.isEmpty()) {
            sendErrorResponse(exchange, 400, "username parameter required");
            return;
        }
        
        User user = userDAO.getUserByUsername(username);
        
        if (user == null) {
            sendErrorResponse(exchange, 404, "User not found");
            return;
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("role", user.getRole());
        
        sendJsonResponse(exchange, 200, gson.toJson(profile));
        System.out.println("👤 Sent profile for user: " + username);
    }
    
    private static void serveStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Default to index.html
        if (path.equals("/") || path.equals("")) {
            path = "/index.html";
        }
        
        // Map common routes to HTML files
        Map<String, String> routeMap = new HashMap<>();
        routeMap.put("/login", "/login.html");
        routeMap.put("/register", "/register.html");
        routeMap.put("/menu", "/menu.html");
        routeMap.put("/cart", "/cart.html");
        routeMap.put("/checkout", "/checkout.html");
        routeMap.put("/order-confirmation", "/order-confirmation.html");
        routeMap.put("/profile", "/profile.html");
        
        if (routeMap.containsKey(path)) {
            path = routeMap.get(path);
        }
        
        File file = new File("frontend" + path);
        
        if (file.exists() && !file.isDirectory()) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String contentType = getContentType(path);
            
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            
            System.out.println("📄 Served: " + path + " (" + contentType + ")");
        } else {
            // Try with .html extension
            File altFile = new File("frontend" + path + ".html");
            if (altFile.exists()) {
                byte[] bytes = Files.readAllBytes(altFile.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } else {
                // File not found
                String notFoundPage = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>404 - Food Not Found</title>
                        <style>
                            body { 
                                font-family: Arial, sans-serif; 
                                text-align: center; 
                                padding: 50px; 
                                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                color: white;
                                min-height: 100vh;
                                display: flex;
                                flex-direction: column;
                                justify-content: center;
                                align-items: center;
                            }
                            h1 { 
                                font-size: 4rem; 
                                margin-bottom: 20px;
                                color: #ff6b6b;
                            }
                            p { 
                                font-size: 1.2rem; 
                                margin-bottom: 30px; 
                                max-width: 600px;
                            }
                            a { 
                                color: #4ecdc4; 
                                text-decoration: none; 
                                font-weight: bold;
                                font-size: 1.1rem;
                                padding: 10px 30px;
                                background: rgba(255,255,255,0.1);
                                border-radius: 50px;
                                transition: all 0.3s;
                            }
                            a:hover {
                                background: rgba(255,255,255,0.2);
                                transform: translateY(-2px);
                            }
                        </style>
                    </head>
                    <body>
                        <h1>🍕 404 - Food Not Found!</h1>
                        <p>The delicious page you're looking for doesn't exist. Maybe it was eaten?</p>
                        <a href="/">🏠 Back to Home</a>
                    </body>
                    </html>
                    """;
                
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(404, notFoundPage.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundPage.getBytes());
                }
                
                System.out.println("❌ File not found: " + path);
            }
        }
    }
    
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }
    
    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("error", true);
        error.addProperty("message", message);
        error.addProperty("statusCode", statusCode);
        
        sendJsonResponse(exchange, statusCode, error.toString());
        System.out.println("❌ Error " + statusCode + ": " + message);
    }
    
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }
    
    private static String getQueryParam(String query, String paramName) {
        if (query == null) return null;
        
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith(paramName + "=")) {
                return param.substring(paramName.length() + 1);
            }
        }
        return null;
    }
    
    private static String getContentType(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".json")) return "application/json";
        return "text/html";
    }
    
    private static String generateSessionId(String username) {
        return "sess_" + System.currentTimeMillis() + "_" + username.hashCode() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private static String getFactoryType(String categoryName) {
        if (categoryName == null) return "pizza";
        
        String category = categoryName.toLowerCase();
        if (category.contains("pizza")) return "pizza";
        if (category.contains("burger")) return "burger";
        if (category.contains("pasta")) return "pizza";
        if (category.contains("sushi")) return "pizza";
        if (category.contains("salad")) return "pizza";
        if (category.contains("drink")) return "pizza";
        return "pizza";
    }
}