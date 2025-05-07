package pt_db_eclipse;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Plasmacenter {

    // Validate if the given blood type is valid
    private boolean isValidBloodType(String bloodType) {
        return bloodType.matches("A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-");
    }

    // Insert plasma record into the database
    public void insertPlasma(String centerId, String bloodType, int units, String userBatchNumber) {
        if (!isValidBloodType(bloodType)) {
            System.out.println("❌ Invalid blood type entered.");
            return;
        }

        // Generate batch ID: B + userBatchNumber + lastTwoDigitsOfYear + currentDay (e.g., B012504)
        Calendar calendar = Calendar.getInstance();
        String year = new SimpleDateFormat("yy").format(calendar.getTime());  // e.g., 25
        String day = new SimpleDateFormat("dd").format(calendar.getTime());   // e.g., 04
        String batchId = "B" + userBatchNumber + year + day;

        String tableName = "plasma_table_" + centerId;
        createPlasmaTableIfNotExists(tableName);

        String insertSQL = "INSERT INTO " + tableName + " (batch_id, blood_type, units) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, batchId);
            stmt.setString(2, bloodType);
            stmt.setInt(3, units);

            stmt.executeUpdate();
            System.out.println("✅ Plasma stored in database with Batch ID: " + batchId);

        } catch (SQLException e) {
            System.err.println("❌ Error inserting plasma data: " + e.getMessage());
        }
    }

    // Create plasma table if it doesn't exist
    private void createPlasmaTableIfNotExists(String tableName) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "batch_id VARCHAR(50) PRIMARY KEY, " +
                "blood_type VARCHAR(5), " +
                "units INT, " +
                "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("❌ Error creating table: " + e.getMessage());
        }
    }

    // Show available plasma stock for a center
    public void showAvailablePlasma(String centerId) {
        String tableName = "plasma_table_" + centerId;
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            boolean hasData = false;
            System.out.println("\n📦 Plasma Availability:");
            while (rs.next()) {
                hasData = true;
                System.out.println("Batch ID: " + rs.getString("batch_id") +
                                   ", Type: " + rs.getString("blood_type") +
                                   ", Units: " + rs.getInt("units") +
                                   ", Date: " + rs.getTimestamp("date"));
            }

            if (!hasData) {
                System.out.println("❌ No plasma stock available.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching plasma stock: " + e.getMessage());
        }
    }
}
