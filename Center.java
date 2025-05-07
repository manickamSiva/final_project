package pt_db_eclipse;

import java.util.*;
import java.sql.*;

public class Center implements Role {
    private String centerId, password, centerName;
    private PatientCreator patientCreator = new PatientCreator();
    private PatientManager patientManager = new PatientManager();
    private Plasmacenter plasmacenter = new Plasmacenter();
    // making scanner static-final
    private static final Scanner sc = new Scanner(System.in);

    @Override
    public void performAction() {
        centerName = detailRegistration();
        if (centerName != null) {
            System.out.println("\n✅ Login successful! Welcome " + centerName);
            showDashboard();
        } else {
            System.out.println("\n❌ Invalid Center ID or Password! Access Denied.");
        }
    }

    @Override
    public String detailRegistration() {


        System.out.print("Enter Center ID: ");
        centerId = sc.nextLine().trim();

        System.out.print("Enter Password: ");
        password = sc.nextLine().trim();

        String centerName = null;

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT center_name FROM centers WHERE center_id = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, centerId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                centerName = rs.getString("center_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return centerName;
    }

    private void showDashboard() {
        
        while (true) {
            System.out.println("\n--- Plasma Center Dashboard ---");
            System.out.println("Welcome, " + centerName);
            System.out.println("1. Patient");
            System.out.println("2. Marketplace");
            System.out.println("3. Plasma_DB");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            sc.nextLine();
            switch (choice) {
                case 1:
                    handlePatient();
                    break;
                case 2:
                    handleMarketplace(centerId);
                    break;
                case 3:
                    handlePlasmaDatabase();
                    break;
                case 4:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void handlePatient() {
     
        while (true) {
            System.out.println("\n--- Patient Module ---");
            System.out.println("1. Create Patient");
            System.out.println("2. Manage Patients");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    patientCreator.createPatient(centerId);
                    break;
                case 2:
                    patientManager.managePatients(centerId);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void handleMarketplace(String centerId) {
  

        while (true) {
            System.out.println("\n--- Marketplace Module ---");
            System.out.println("1. Set Plasma in Marketplace (Sell)");
            System.out.println("2. Display Marketplace Products");
            System.out.println("3. Transaction History (Your Center)");
            System.out.println("4. Full Log (Intake, Listings, Purchases)");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
                continue;
            }

           
            switch (choice) {
                case 1:
                    System.out.println("📦 Available Plasma Stock:");
                    plasmacenter.showAvailablePlasma(centerId);

                    System.out.print("Enter Blood Type (A+, A-, B+, B-, AB+, AB-, O+, O-): ");
                    String bloodType = sc.nextLine().toUpperCase().trim();

                    if (!isValidBloodType(bloodType)) {
                        System.out.println("❌ Invalid blood type.");
                        break;
                    }

                    try {
                    	System.out.print("Enter the Batch ID: ");
                    	String batchId = sc.nextLine().trim().toUpperCase();  

                        System.out.print("Enter Number of Units to List: ");
                        int units = Integer.parseInt(sc.nextLine());

                        System.out.print("Enter Price per Unit: ");
                        double price = Double.parseDouble(sc.nextLine());

                        listPlasmaToMarketplace(centerId, bloodType, batchId, units, price);
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid number format.");
                    }
                    break;

                case 2:
                    displayMarketplaceProducts();
                    break;

                case 3:
                    displayCenterTransactionHistory(centerId);
                    break;

                case 4:
                    displayFullLog(centerId);
                    break;

                case 5:
                    return;

                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }

    private void handlePlasmaDatabase() {

        while (true) {
            System.out.println("\n--- Plasma Database ---");
            System.out.println("1. Store Plasma Details");
            System.out.println("2. Show Availability");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a valid number.");
                continue;
            }

           
            switch (choice) {
                case 1:
                    System.out.print("Enter Blood Type (A+, A-, B+, B-, AB+, AB-, O+, O-): ");
                    String type = sc.nextLine().toUpperCase().trim();

                    if (!isValidBloodType(type)) {
                        System.out.println("❌ Invalid blood type.");
                        break;
                    }

                    System.out.print("Enter Batch Number (e.g., 01): ");
                    String batchNumber = sc.nextLine().trim();

                    if (!batchNumber.matches("\\d{2}")) {
                        System.out.println("❌ Batch number must be two digits (e.g., 01, 05, 10).");
                        break;
                    }

                    System.out.print("Enter Number of Units: ");
                    int units;
                    try {
                        units = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Invalid number of units.");
                        break;
                    }

                   plasmacenter.insertPlasma(centerId, type, units, batchNumber);  // Updated method
                    break;
                case 2:
                    plasmacenter.showAvailablePlasma(centerId);
                    break;

                case 3:
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private boolean isValidBloodType(String type) {
        return type.matches("A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-");
    }

   

  



    private void listPlasmaToMarketplace(String centerId, String bloodType, String batchId, int units, double price) {
        String plasmaTable = "plasma_table_" + centerId;
        String historyTable = "history_" + centerId;

        String checkStockSQL = "SELECT units FROM " + plasmaTable + " WHERE batch_id = ? AND blood_type = ?";
        String checkExistingListingSQL = "SELECT 1 FROM marketplace WHERE center_id = ? AND plasmatype = ? AND batch_id = ? AND status = 'active'";
        String updateStockSQL = "UPDATE " + plasmaTable + " SET units = units - ? WHERE blood_type = ? AND batch_id = ?";
        String insertMarketSQL = "INSERT INTO marketplace (center_id, plasmatype, available_units, price, batch_id, status) VALUES (?, ?, ?, ?, ?, 'active')";
        String insertLogSQL = "INSERT INTO " + historyTable + " (center_id, action, blood_type, batch_id, units) VALUES (?, 'LISTED', ?, ?, ?)";

        createHistoryTableIfNotExists(centerId);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            int availableUnits = 0;

            // ✅ 1. Check stock availability
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSQL)) {
                checkStmt.setString(1, batchId);
                checkStmt.setString(2, bloodType);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    availableUnits = rs.getInt("units");
                } else {
                    System.out.println("❌ Blood type and batch not found in stock.");
                    return;
                }

                if (availableUnits < units) {
                    System.out.println("❌ Not enough plasma units in this batch.");
                    return;
                }
            }

            // 🔁 2. Check for existing active listing for the same batch
            try (PreparedStatement existingStmt = conn.prepareStatement(checkExistingListingSQL)) {
                existingStmt.setString(1, centerId);
                existingStmt.setString(2, bloodType);
                existingStmt.setString(3, batchId);
                ResultSet existingRs = existingStmt.executeQuery();
                if (existingRs.next()) {
                    System.out.println("⚠️ Already listed this batch. Please remove existing listing before adding a new one.");
                    return;
                }
            }

            // ➖ 3. Deduct units
            try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSQL)) {
                updateStmt.setInt(1, units);
                updateStmt.setString(2, bloodType);
                updateStmt.setString(3, batchId);
                updateStmt.executeUpdate();
            }

            // 🛒 4. Insert listing in marketplace
            try (PreparedStatement marketStmt = conn.prepareStatement(insertMarketSQL)) {
                marketStmt.setString(1, centerId);
                marketStmt.setString(2, bloodType);
                marketStmt.setInt(3, units);
                marketStmt.setDouble(4, price);
                marketStmt.setString(5, batchId);
                marketStmt.executeUpdate();
            }

            // 📝 5. Log action in history
            try (PreparedStatement logStmt = conn.prepareStatement(insertLogSQL)) {
                logStmt.setString(1, centerId);
                logStmt.setString(2, bloodType);
                logStmt.setString(3, batchId);
                logStmt.setInt(4, units);
                logStmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Plasma listed from batch " + batchId + " and deducted successfully.");

        } catch (SQLException e) {
            System.out.println("❌ Error during listing operation. Rolling back.");
            e.printStackTrace();
        }
    }


    private void displayMarketplaceProducts() {
        String sql = "SELECT * FROM marketplace WHERE status = 'active'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            boolean hasListings = false; // flag to track if listings exist
            System.out.println("🔷 Marketplace Listings:");

            while (rs.next()) {
                hasListings = true;
                System.out.println("Listing ID: " + rs.getInt("listing_id") +
                                   ", Center: " + rs.getString("center_id") +
                                   ", Type: " + rs.getString("plasmatype") +
                                   ", Units: " + rs.getInt("available_units") +
                                   ", Price: ₹" + rs.getDouble("price"));
            }

            if (!hasListings) {
                System.out.println("⚠️ No active products listed in the marketplace.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void displayCenterTransactionHistory(String centerId) {
        String sql = "SELECT * FROM transactions WHERE center_id = ? ";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, centerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("📜 Purchase History:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("Txn ID: " + rs.getInt("transaction_id") +
                        ", Center ID: " + rs.getString("center_id") +
                        ", Blood Type: " + rs.getString("blood_type") +
                        ", Quantity: " + rs.getInt("quantity") +
                        ", Total: ₹" + rs.getDouble("total_price"));

            }

            if (!hasData) {
                System.out.println("No purchase history found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayFullLog(String centerId) {
        String tableName = "history_" + centerId;
        String sql = "SELECT * FROM " + tableName + " WHERE center_id = ? ORDER BY date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, centerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("📘 Full Log:");
            while (rs.next()) {
                System.out.println("[" + rs.getTimestamp("date") + "] "
                    + rs.getString("action") + " | Type: "
                    + rs.getString("blood_type") + " | Units: "
                    + rs.getInt("units"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createHistoryTableIfNotExists(String centerId) {
        String tableName = "history_" + centerId;
        
        // SQL to create the table if not exists
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                                "id SERIAL PRIMARY KEY, " +
                                "center_id VARCHAR(20), " +
                                "action VARCHAR(20), " +
                                "blood_type VARCHAR(5), " +
                                "batch_id VARCHAR(20), " +  // Added batch_id column
                                "units INT, " +
                                "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (center_id) REFERENCES centers(center_id) ON DELETE CASCADE" +
                                ")";
        
        // SQL to add batch_id column if it doesn't exist
        String alterTableSQL = "ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS batch_id VARCHAR(20)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // First, try to create the table
            stmt.execute(createTableSQL);
            System.out.println("✅ History table checked/created for: " + tableName);

            // Then, ensure the batch_id column exists in case the table already exists
            stmt.execute(alterTableSQL);
            System.out.println("✅ Ensured batch_id column exists in table: " + tableName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
