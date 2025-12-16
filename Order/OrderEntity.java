package Order;

public class OrderEntity {
    private int orderId;
    private String username;
    private String status;
    private double totalAmount;  // Changed from total
    private String createdAt;    // Changed from orderDate
    private String orderNumber;  // Add this
    private String deliveryAddress;  // Add this
    
    // Updated constructor
    public OrderEntity(int orderId, String username, String status, double totalAmount, String createdAt) {
        this.orderId = orderId;
        this.username = username;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.orderNumber = "ORD-" + String.format("%08d", orderId);
    }
    
    // Getters - add the missing ones
    public int getOrderId() { return orderId; }
    public String getUsername() { return username; }
    public String getStatus() { return status; }
    public double getTotal() { return totalAmount; }  // Keep this for compatibility
    public double getTotalAmount() { return totalAmount; }  // Add this
    public String getOrderDate() { return createdAt; }  // Keep for compatibility
    public String getCreatedAt() { return createdAt; }  // Add this
    public String getOrderNumber() { return orderNumber; }  // Add this
    public String getDeliveryAddress() { return deliveryAddress; }  // Add this
    public void setDeliveryAddress(String address) { this.deliveryAddress = address; }  // Add setter
}