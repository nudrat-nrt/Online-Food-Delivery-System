package Menu;
import FoodItem.FoodItem;

public class ExtraCheese extends MenuDecorator {

    public ExtraCheese(FoodItem food) {
        super(food);
    }

    public String getDescription() {
        return food.getDescription() + " + Extra Cheese";
    }

    public double getPrice() {
        return food.getPrice() + 50;
    }
}

