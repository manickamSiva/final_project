package pt_db_eclipse;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class PatientCreator {
    private static final Scanner sc = new Scanner(System.in);

    public void createPatient(String centerId) {
        try {
            String tableName = "patient_table_" + centerId;

            // Ensure table exists
            ensurePatientTableExists(tableName,centerId);

            // Collect patient information
            System.out.print("Enter Patient Name: ");
            String name = sc.nextLine().trim();

            System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
            String dobInput = sc.nextLine().trim();
            LocalDate dob = parseDate(dobInput);

            if (dob == null) {
                System.out.println("❌ Invalid DOB format. Patient creation cancelled.");
                return;
            }

            int age = calculateAge(dob);

            System.out.print("Enter Phone Number (10 digits): ");
            String phoneNumber = sc.nextLine().trim();
            if (!isValidPhoneNumber(phoneNumber)) {
                System.out.println("❌ Invalid phone number. Patient creation cancelled.");
                return;
            }

            System.out.print("Enter Email ID: ");
            String emailId = sc.nextLine().trim();
            if (!isValidEmail(emailId)) {
                System.out.println("❌ Invalid email address. Patient creation cancelled.");
                return;
            }

            System.out.print("Enter Number of Donations: ");
            int noOfDonations = sc.nextInt();
            sc.nextLine(); // consume newline

            System.out.print("Enter Last Donation Date (YYYY-MM-DD): ");
            String lastDonationStr = sc.nextLine().trim();
            LocalDate lastDonationDate = parseDate(lastDonationStr);

            if (lastDonationDate == null) {
                System.out.println("❌ Invalid Last Donation Date format. Patient creation cancelled.");
                return;
            }

            // Generate unique Patient ID
            String patientId = generateUniqueId(name, phoneNumber);

            // Calculate patient reward
            int patientReward = noOfDonations * 75;

            // Save patient into DB
            savePatientToDatabase(centerId,tableName, patientId, name, age, dob, phoneNumber, emailId, noOfDonations, patientReward, lastDonationDate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensurePatientTableExists(String tableName,String centerId) {
    	 String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
    	            + "center_id VARCHAR(20), " // <-- centerId column with type VARCHAR(20)
    	            + "patient_id VARCHAR(20) PRIMARY KEY, "
    	            + "patient_name VARCHAR(100), "
    	            + "patient_age INT, "
    	            + "dob DATE, "
    	            + "phone_number VARCHAR(15), "
    	            + "email_id VARCHAR(100), "
    	            + "no_of_donations INT, "
    	            + "patient_reward INT, "
    	            + "last_donation_date DATE, "
    	            + "FOREIGN KEY (center_id) REFERENCES centers(center_id)" // <-- adding FOREIGN KEY constraint properly here
    	            + ")";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Patient table ready: " + tableName);
        } catch (SQLException e) {
            System.out.println("❌ Error creating patient table.");
            e.printStackTrace();
        }
    }

    private void savePatientToDatabase(String centerId, String tableName, String patientId, String name, int age, LocalDate dob,
            String phoneNumber, String emailId, int noOfDonations, int patientReward, LocalDate lastDonationDate) {
    	String insertSQL = "INSERT INTO " + tableName
    			+ " (center_id, patient_id, patient_name, patient_age, dob, phone_number, email_id, no_of_donations, patient_reward, last_donation_date) "
    			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    	try (Connection conn = DatabaseConfig.getConnection();
    			PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
    		pstmt.setString(1, centerId);       // ✅ Corrected: centerId first
    		pstmt.setString(2, patientId);
    		pstmt.setString(3, name);
    		pstmt.setInt(4, age);
    		pstmt.setDate(5, Date.valueOf(dob));
    		pstmt.setString(6, phoneNumber);
    		pstmt.setString(7, emailId);
    		pstmt.setInt(8, noOfDonations);
    		pstmt.setInt(9, patientReward);
    		pstmt.setDate(10, Date.valueOf(lastDonationDate));
    		
    		pstmt.executeUpdate();
    		System.out.println("✅ Patient created successfully with ID: " + patientId);
    		
    	} catch (SQLException e) {
    		System.out.println("❌ Error saving patient to database.");
    		e.printStackTrace();
    		}
    	}


    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{10}");
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@[\\w-\\.]+\\.([a-z]{2,4})$";
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    private int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private String generateUniqueId(String name, String phoneNumber) {
        String namePart = name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
        String phonePart = phoneNumber.length() >= 2 ? phoneNumber.substring(phoneNumber.length() - 2) : phoneNumber;
        String randomPart = generateRandomString(2);
        return namePart + randomPart + phonePart;
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt((int) (Math.random() * characters.length())));
        }
        return sb.toString();
    }
}
