package Database;

import FoodItem.FoodItemEntity;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodItemDAO {
    private Connection connection;
    
    public FoodItemDAO() {
        connection = SQLiteConnection.getInstance().getConnection();
    }
    
    public static class Category {
        private int categoryId;
        private String name;
        private String description;
        private String imageUrl;
        
        public Category(int categoryId, String name, String description, String imageUrl) {
            this.categoryId = categoryId;
            this.name = name;
            this.description = description;
            this.imageUrl = imageUrl;
        }
        
        public int getCategoryId() { return categoryId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getImageUrl() { return imageUrl; }
    }
    
    public List<FoodItemEntity> getAllAvailableItems() {
        List<FoodItemEntity> items = new ArrayList<>();
        String sql = """
            SELECT fi.*, c.name as category_name 
            FROM food_items fi 
            JOIN categories c ON fi.category_id = c.category_id 
            WHERE fi.available = 1 
            ORDER BY fi.name
            """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FoodItemEntity item = new FoodItemEntity(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getDouble("price"),
                    rs.getBoolean("available"),
                    rs.getBoolean("vegetarian")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting food items: " + e.getMessage());
        }
        return items;
    }
    
    public FoodItemEntity getItemById(int itemId) {
        String sql = """
            SELECT fi.*, c.name as category_name 
            FROM food_items fi 
            JOIN categories c ON fi.category_id = c.category_id 
            WHERE fi.item_id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new FoodItemEntity(
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getDouble("price"),
                    rs.getBoolean("available"),
                    rs.getBoolean("vegetarian")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting item by ID: " + e.getMessage());
        }
        return null;
    }
    
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("category_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("image_url")
                );
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting categories: " + e.getMessage());
        }
        return categories;
    }
}