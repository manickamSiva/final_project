package pt_db_eclipse;
public class Transaction {
    private String buyerName;
    private String bloodType;
    private int quantity; // Changed from 'units' to 'quantity' for clarity
    private double totalPrice;

    public Transaction(String buyerName, String bloodType, int quantity, double totalPrice) {
        this.buyerName = buyerName;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    // Getters
    public String getBuyerName() {
        return buyerName;
    }

    public String getBloodType() {
        return bloodType;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return "Buyer: " + buyerName +
                ", Blood Type: " + bloodType +
                ", Quantity: " + quantity +
                ", Total Price: ₹" + totalPrice;
    }
}