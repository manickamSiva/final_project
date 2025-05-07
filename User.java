package pt_db_eclipse;

import java.sql.*;
import java.util.Scanner;

public class User implements Role {
    private String patientId, centerName;
    private String patientName, emailId;
    private int noOfDonations, patientReward;
    private String centerId;

    private static final Scanner sc = new Scanner(System.in);
    @Override
    public void performAction() {
        System.out.println("User functionality executed.");
        detailRegistration();
    }

    @Override
    public String detailRegistration() {

        System.out.print("Enter Center Name: ");
        centerName = sc.nextLine().trim();
        
        System.out.print("Enter Your Patient ID: ");
        patientId = sc.nextLine().trim();
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Step 1: Get center_id from centers table using center_name
            String centerQuery = "SELECT center_id FROM centers WHERE center_name = ?";
            PreparedStatement centerStmt = conn.prepareStatement(centerQuery);
            centerStmt.setString(1, centerName);
            ResultSet centerRs = centerStmt.executeQuery();

            if (centerRs.next()) {
                centerId = centerRs.getString("center_id");
            } else {
                System.out.println("Center not found!");
                return null;
            }

            // Step 2: Form dynamic patient table
            String patientTable = "patient_table_" + centerId;

            // Step 3: Fetch patient details
            String patientQuery = "SELECT * FROM " + patientTable + " WHERE patient_id = ?";
            PreparedStatement patientStmt = conn.prepareStatement(patientQuery);
            patientStmt.setString(1, patientId);
            ResultSet patientRs = patientStmt.executeQuery();

            if (patientRs.next()) {
                // Assigning fetched data to variables
                patientName = patientRs.getString("patient_name");
                emailId = patientRs.getString("email_id");
                noOfDonations = patientRs.getInt("no_of_donations");
                patientReward = patientRs.getInt("patient_reward");

                // Show the dashboard
                showDashboard(conn, patientTable);
            } else {
                System.out.println("Patient not found with the provided Patient ID!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return centerId;
    }

    private void showDashboard(Connection conn, String patientTable) {
       

        System.out.println("\n--- User Dashboard ---");
        System.out.println("Welcome, " + patientName);
        System.out.println("Email ID: " + emailId);
        System.out.println("Number of Donations: " + noOfDonations);
        System.out.println("Current Reward Points: " + patientReward);

        if (patientReward >= 75) {
            System.out.println("\nYou have enough points to redeem. You have " + patientReward + " points.");
            System.out.print("Do you want to redeem your points for ₹" + (patientReward / 75 * 100) + "? (yes/no): ");
            String redeemChoice = sc.nextLine().trim().toLowerCase();

            if (redeemChoice.equals("yes")) {
                int redeemableTimes = patientReward / 75;
                int amount = redeemableTimes * 100;
                int remainingPoints = patientReward % 75;

                // Update reward points in database
                try {
                    String updateQuery = "UPDATE " + patientTable + " SET patient_reward = ? WHERE patient_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, remainingPoints);
                    updateStmt.setString(2, patientId);
                    int updated = updateStmt.executeUpdate();

                    if (updated > 0) {
                        patientReward = remainingPoints; // update local variable also
                        System.out.println("\nYou have redeemed ₹" + amount + " for medical expenses.");
                        System.out.println("Remaining reward points: " + patientReward);
                    } else {
                        System.out.println("Error updating reward points in database.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("You chose not to redeem your points.");
            }
        } else {
            System.out.println("\nYou need at least 75 points to redeem. Keep donating!");
        }

        // Option to view dashboard again or logout
        System.out.println("\nDo you want to view your details again or log out? (view/logout)");
        String action = sc.nextLine().trim().toLowerCase();

        if (action.equals("view")) {
            showDashboard(conn, patientTable);
        } else if (action.equals("logout")) {
            System.out.println("Logging out...");
        } else {
            System.out.println("Invalid option. Exiting...");
        }
    }
}
