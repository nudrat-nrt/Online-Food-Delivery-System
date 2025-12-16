package Cart;

public class NormalTotal implements CartTotalStrategy {
    public double calculate(double amount) {
        return amount;
    }
}
