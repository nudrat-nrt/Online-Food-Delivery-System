package Database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = SQLiteConnection.getInstance().getConnection();
            stmt = conn.createStatement();
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    email TEXT UNIQUE,
                    full_name TEXT,
                    phone TEXT,
                    role TEXT DEFAULT 'CUSTOMER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT 1
                )
            """;
            stmt.execute(createUsersTable);
            
            // Create categories table
            String createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS categories (
                    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    description TEXT,
                    image_url TEXT
                )
            """;
            stmt.execute(createCategoriesTable);
            
            // Create food_items table
            String createFoodItemsTable = """
                CREATE TABLE IF NOT EXISTS food_items (
                    item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    category_id INTEGER,
                    price REAL NOT NULL,
                    available BOOLEAN DEFAULT 1,
                    vegetarian BOOLEAN DEFAULT 0,
                    image_url TEXT,
                    FOREIGN KEY (category_id) REFERENCES categories(category_id)
                )
            """;
            stmt.execute(createFoodItemsTable);
            
            // Create orders table
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    order_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    total_amount REAL NOT NULL,
                    status TEXT DEFAULT 'PENDING',
                    delivery_address TEXT NOT NULL,
                    phone_number TEXT,
                    payment_method TEXT DEFAULT 'Cash on Delivery',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (username) REFERENCES users(username)
                )
            """;
            stmt.execute(createOrdersTable);
            
            // Create order_items table
            String createOrderItemsTable = """
                CREATE TABLE IF NOT EXISTS order_items (
                    order_item_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    price_per_unit REAL NOT NULL,
                    FOREIGN KEY (order_id) REFERENCES orders(order_id),
                    FOREIGN KEY (item_id) REFERENCES food_items(item_id)
                )
            """;
            stmt.execute(createOrderItemsTable);
            
            // Insert default categories
            String insertCategories = """
                INSERT OR IGNORE INTO categories (category_id, name, description, image_url) VALUES
                (1, 'Pizza', 'Delicious pizzas with various toppings', '/images/categories/pizza.jpg'),
                (2, 'Burger', 'Juicy burgers with fresh ingredients', '/images/categories/burger.jpg'),
                (3, 'Pasta', 'Creamy and delicious pasta dishes', '/images/categories/pasta.jpg'),
                (4, 'Sushi', 'Fresh and authentic Japanese sushi', '/images/categories/sushi.jpg'),
                (5, 'Salad', 'Healthy and fresh salads', '/images/categories/salad.jpg'),
                (6, 'Drinks', 'Refreshing beverages', '/images/categories/drinks.jpg')
            """;
            stmt.execute(insertCategories);
            
            // Insert default food items
            String insertFoodItems = """
                INSERT OR IGNORE INTO food_items (item_id, name, description, category_id, price, vegetarian) VALUES
                (1, 'Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and fresh basil', 1, 12.99, 1),
                (2, 'Pepperoni Pizza', 'Loaded with spicy pepperoni and extra cheese', 1, 14.99, 0),
                (3, 'Classic Burger', 'Beef patty with lettuce, tomato, and special sauce', 2, 8.99, 0),
                (4, 'Chicken Burger', 'Grilled chicken breast with avocado and mayo', 2, 9.99, 0),
                (5, 'Spaghetti Carbonara', 'Creamy pasta with bacon and parmesan', 3, 11.99, 0),
                (6, 'California Roll', 'Crab stick, avocado, and cucumber roll', 4, 15.99, 0),
                (7, 'Fresh Salad', 'Mixed greens with cherry tomatoes and vinaigrette', 5, 7.99, 1),
                (8, 'Soft Drinks', 'Coke, Pepsi, Sprite, Fanta', 6, 2.99, 1)
            """;
            stmt.execute(insertFoodItems);
            
            // Insert default admin user
            String insertAdmin = """
                INSERT OR IGNORE INTO users (username, password_hash, email, full_name, phone, role) VALUES
                ('admin', '1234', 'admin@foodexpress.com', 'System Administrator', '1234567890', 'ADMIN')
            """;
            stmt.execute(insertAdmin);
            
            System.out.println("✅ Database tables created and initialized");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                System.err.println("❌ Error closing statement: " + e.getMessage());
            }
        }
    }
}