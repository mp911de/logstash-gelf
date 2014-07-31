package biz.paluch.logging.gelf.standalone;

public class ShoppingCart {

    private String cartId;
    private double amount;
    private String customerId;

    public String getCartId() {
        return cartId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
