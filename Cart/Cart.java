package Cart;

import FoodItem.FoodItem;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private CartTotalStrategy strategy;
    private List<CartItem> items = new ArrayList<>();
    
    public void setStrategy(CartTotalStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void addItem(FoodItem food, int quantity) {
        items.add(new CartItem(food, quantity));
    }
    
    public double getTotal() {
        double subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getTotalPrice();
        }
        return strategy.calculate(subtotal);
    }
    
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public void clear() {
        items.clear();
    }
}