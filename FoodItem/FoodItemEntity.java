package FoodItem;

public class FoodItemEntity {
    private int itemId;
    private String name;
    private String description;
    private int categoryId;
    private String categoryName;
    private double price;
    private boolean available;
    private boolean vegetarian;
    
    // 8-parameter constructor
    public FoodItemEntity(int itemId, String name, String description, 
                         int categoryId, String categoryName, double price, 
                         boolean available, boolean vegetarian) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.available = available;
        this.vegetarian = vegetarian;
    }
    
    // Getters
    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public boolean isVegetarian() { return vegetarian; }
}