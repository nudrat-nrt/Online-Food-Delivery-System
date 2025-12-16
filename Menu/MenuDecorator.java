package Menu;
import FoodItem.FoodItem;

public abstract class MenuDecorator implements FoodItem {
    protected FoodItem food;

    public MenuDecorator(FoodItem food) {
        this.food = food;
    }
}
