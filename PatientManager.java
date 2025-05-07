package pt_db_eclipse;

import java.sql.*;
import java.util.Scanner;

public class PatientManager {
    private Connection conn;

    public PatientManager() {
        try {
            conn = DatabaseConfig.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void managePatients(String centerId) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Registered Patients ---");
            displayAllPatients(centerId);

            System.out.println("[0] Back to Dashboard");
            System.out.print("Enter Unique ID of patient to manage (or 0 to go back): ");
            String selectedUniqueId = sc.nextLine().trim();

            if (selectedUniqueId.equals("0")) {
                return; // go back
            }

            if (patientExists(centerId, selectedUniqueId)) {
                manageSinglePatient(sc, selectedUniqueId, centerId);
            } else {
                System.out.println("❌ Patient with Unique ID " + selectedUniqueId + " not found. Try again.");
            }
        }
    }

    private void displayAllPatients(String centerId) {
        try {
            String tableName = "patient_table_" + centerId;
            String sql = "SELECT patient_id, patient_name FROM " + tableName;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("Patient ID: " + rs.getString("patient_id") + " | Name: " + rs.getString("patient_name"));
            }
            if (!found) {
                System.out.println("No patients found.");
            }

        } catch (SQLException e) {
            System.out.println("Error displaying patients: " + e.getMessage());
        }
    }

    private boolean patientExists(String centerId, String uniqueId) {
        try {
            String tableName = "patient_table_" + centerId;
            String sql = "SELECT 1 FROM " + tableName + " WHERE patient_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // returns true if any row found
        } catch (SQLException e) {
            System.out.println("Error checking patient existence: " + e.getMessage());
            return false;
        }
    }

    private void manageSinglePatient(Scanner sc, String uniqueId, String centerId) {
        while (true) {
            System.out.println("\n--- Managing Patient with Unique ID: " + uniqueId + " ---");
            displayPatientDetails(centerId, uniqueId);

            System.out.println("1. Modify Patient Details");
            System.out.println("2. Delete Patient");
            System.out.println("3. Back");
            System.out.print("Enter your choice: ");
            int option = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (option) {
                case 1:
                    modifyPatientDetails(sc, centerId, uniqueId);
                    break;
                case 2:
                    deletePatient(centerId, uniqueId);
                    return; // after deletion, return to patient list
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void displayPatientDetails(String centerId, String uniqueId) {
        try {
            String tableName = "patient_table_" + centerId;
            String sql = "SELECT * FROM " + tableName + " WHERE patient_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Name: " + rs.getString("patient_name"));
                System.out.println("Phone Number: " + rs.getString("phone_number"));
                System.out.println("Email: " + rs.getString("email_id"));
                System.out.println("Number of Donations: " + rs.getInt("no_of_donations"));
                System.out.println("Reward Points: " + rs.getInt("patient_reward"));
            } else {
                System.out.println("Patient not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error displaying patient details: " + e.getMessage());
        }
    }

    private void modifyPatientDetails(Scanner sc, String centerId, String uniqueId) {
        try {
            System.out.print("Enter new phone number: ");
            String newPhone = sc.nextLine();
            System.out.print("Enter new email address: ");
            String newEmail = sc.nextLine();
            System.out.print("Enter number of additional donations: ");
            int additionalDonations = sc.nextInt();
            sc.nextLine(); // consume newline

            // Fetch existing donations and reward points
            String tableName = "patient_table_" + centerId;
            String selectSql = "SELECT no_of_donations, patient_reward FROM " + tableName + " WHERE patient_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, uniqueId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int currentDonations = rs.getInt("no_of_donations");
                int currentRewards = rs.getInt("patient_reward");

                int updatedDonations = currentDonations + additionalDonations;
                int updatedRewards = currentRewards + (additionalDonations * 10); // Example: 10 points per donation

                String updateSql = "UPDATE " + tableName + " SET phone_number = ?, email_id = ?, no_of_donations = ?, patient_reward = ? WHERE patient_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newPhone);
                updateStmt.setString(2, newEmail);
                updateStmt.setInt(3, updatedDonations);
                updateStmt.setInt(4, updatedRewards);
                updateStmt.setString(5, uniqueId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Patient details updated successfully!");
                } else {
                    System.out.println("❌ Failed to update patient details.");
                }
            } else {
                System.out.println("Patient not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error modifying patient details: " + e.getMessage());
        }
    }

    private void deletePatient(String centerId, String uniqueId) {
        try {
            String tableName = "patient_table_" + centerId;
            String sql = "DELETE FROM " + tableName + " WHERE patient_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uniqueId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Patient deleted successfully!");
            } else {
                System.out.println("❌ Patient deletion failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting patient: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // Close connection properly
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
