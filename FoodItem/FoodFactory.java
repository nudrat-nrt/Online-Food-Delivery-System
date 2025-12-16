package FoodItem;

public class FoodFactory {

    public static FoodItem createFood(String type) {
        if (type.equalsIgnoreCase("pizza"))
            return new Pizza();
        if (type.equalsIgnoreCase("burger"))
            return new Burger();
        return null;
    }
}

