package Database;

import Order.OrderEntity;
import Order.OrderStatus;
import Cart.Cart;
import Cart.CartItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection connection;
    
    public OrderDAO() {
        connection = SQLiteConnection.getInstance().getConnection();
    }
    
    public int createOrder(Cart cart, String username, String deliveryAddress, String phoneNumber) {
        String orderSql = """
            INSERT INTO orders (username, total_amount, delivery_address, phone_number) 
            VALUES (?, ?, ?, ?)
            """;
        
        String itemSql = """
            INSERT INTO order_items (order_id, item_id, quantity, price_per_unit) 
            VALUES (?, ?, ?, ?)
            """;
        
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = SQLiteConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Insert order
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, username);
            orderStmt.setDouble(2, cart.getTotal());
            orderStmt.setString(3, deliveryAddress);
            orderStmt.setString(4, phoneNumber);
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            // Get generated order ID
            generatedKeys = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }
            
            // Insert order items
            itemStmt = conn.prepareStatement(itemSql);
            for (CartItem cartItem : cart.getItems()) {
                // Note: This needs food item ID which we don't have in CartItem
                // For now, we'll use a placeholder. You'll need to modify CartItem to store itemId
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, 1); // Placeholder - you need to modify your model
                itemStmt.setInt(3, cartItem.getQuantity());
                itemStmt.setDouble(4, cartItem.getFood().getPrice());
                itemStmt.addBatch();
            }
            
            itemStmt.executeBatch();
            conn.commit();
            
            return orderId;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating order: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Error rolling back: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (orderStmt != null) orderStmt.close();
                if (itemStmt != null) itemStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
    public List<OrderEntity> getUserOrders(String username) {
        List<OrderEntity> orders = new ArrayList<>();
        String sql = """
            SELECT o.*, 
                   GROUP_CONCAT(fi.name || ' (x' || oi.quantity || ')') as items
            FROM orders o
            LEFT JOIN order_items oi ON o.order_id = oi.order_id
            LEFT JOIN food_items fi ON oi.item_id = fi.item_id
            WHERE o.username = ?
            GROUP BY o.order_id
            ORDER BY o.created_at DESC
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                // You'll need to modify OrderEntity to have proper constructor
                // For now, using a simple version
                OrderEntity order = new OrderEntity(
                    rs.getInt("order_id"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getDouble("total_amount"),
                    rs.getString("created_at")
                );
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user orders: " + e.getMessage());
        }
        return orders;
    }
}