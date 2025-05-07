package pt_db_eclipse;

import java.util.*;
import java.sql.*;

public class Buyers implements Role {
    private String buyerId, password, buyerCenterName;
    private static final Scanner sc = new Scanner(System.in);

    @Override
    public void performAction() {
        buyerCenterName = detailRegistration();

        if (buyerCenterName != null) {
            System.out.println("\n✅ Login successful! Welcome " + buyerCenterName);
            showDashboard();
        } else {
            System.out.println("\n❌ Invalid Center ID or Password! Access Denied.");
        }
    }

    @Override
    public String detailRegistration() {
        
        System.out.print("Enter Center ID: ");
        buyerId = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        password = sc.nextLine();

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT buyer_center_name FROM buyers WHERE buyer_id = ? AND buyer_password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, buyerId);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("buyer_center_name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showDashboard() {
        
        while (true) {
            System.out.println("\n--- Buyers Center Dashboard ---");
            System.out.println("1. Marketplace");
            System.out.println("2. Transaction History");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> handleMarketplace();
                case 2 -> handleTransactionHistory();
                case 3 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void handleMarketplace() {
        while (true) {
            System.out.println("\n--- Marketplace ---");
            System.out.println("1. View Available Plasma Products");
            System.out.println("2. Buy Plasma Product");
            System.out.println("3. Back");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> buyProduct();
                case 3 -> {
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewProducts() {
        String sql = "SELECT * FROM marketplace WHERE status = 'active'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (!rs.isBeforeFirst()) {
                System.out.println("⚠ No plasma products available in the marketplace.");
                return;
            }

            System.out.println("\n🩸 Available Plasma Products:");
            while (rs.next()) {
                System.out.println("Listing ID: " + rs.getInt("listing_id") +
                        ", Center: " + rs.getString("center_id") +
                        ", Type: " + rs.getString("plasmatype") +
                        ", Units: " + rs.getInt("available_units") +
                        ", Price: ₹" + rs.getDouble("price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buyProduct() {

        System.out.print("Enter Center ID to buy from: ");
        String centerId = sc.nextLine();

        System.out.print("Enter the blood type: ");
        String bloodType = sc.nextLine();

        System.out.print("Enter number of units to buy: ");
        int qty = sc.nextInt();
        sc.nextLine(); // consume leftover newline

        String sql = "SELECT * FROM marketplace WHERE center_id = ? AND plasmatype = ? AND status = 'active'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, centerId);
            pstmt.setString(2, bloodType);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int listingId = rs.getInt("listing_id");
                int availableUnits = rs.getInt("available_units");
                double pricePerUnit = rs.getDouble("price");

                if (qty > availableUnits) {
                    System.out.println("❌ Not enough units available. Try again.");
                    return;
                }

                double totalPrice = qty * pricePerUnit;
                int newUnits = availableUnits - qty;
                String status = newUnits == 0 ? "sold_out" : "active";

                // Update marketplace row using listing_id for precision
                updateMarketplaceStatus(listingId, newUnits, status);

                // Replace 'buyerCenterName' with your actual buyer's identifier
                saveTransactionToDatabase(buyerCenterName, centerId, bloodType, qty, totalPrice);

                System.out.println("✅ Purchase successful! Total Price: ₹" + totalPrice);
            } else {
                System.out.println("❌ Product not found or already sold out.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMarketplaceStatus(int listingId, int newUnits, String status) {
        String sql = "UPDATE marketplace SET available_units = ?, status = ? WHERE listing_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newUnits);
            pstmt.setString(2, status);
            pstmt.setInt(3, listingId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void saveTransactionToDatabase(String buyerName, String centerId, String bloodType, int qty, double totalPrice) {
        String sql = "INSERT INTO transactions (buyer_name, center_id, blood_type, quantity, total_price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, buyerName);
            pstmt.setString(2, centerId);
            pstmt.setString(3, bloodType);
            pstmt.setInt(4, qty);
            pstmt.setDouble(5, totalPrice);
            pstmt.executeUpdate();
            System.out.println("📝 Transaction saved.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        String sql = "SELECT * FROM transactions WHERE buyer_name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, buyerCenterName);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No transactions found.");
                return;
            }

            while (rs.next()) {
                System.out.println("Txn ID: " + rs.getInt("transaction_id") +
                        ", Center ID: " + rs.getString("center_id") +
                        ", Blood Type: " + rs.getString("blood_type") +
                        ", Quantity: " + rs.getInt("quantity") +
                        ", Total: ₹" + rs.getDouble("total_price"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
