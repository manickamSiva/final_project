package pt_db_eclipse;
import java.util.*;

public class

Register {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Role role = null;
        while (true) {
            // Ask user to select role
            System.out.println("Enter your role: user / centers / buyers (or 'exit' to quit)");
            String roleInput = sc.nextLine().trim().toLowerCase();

            if (roleInput.equals("exit")) {
                System.out.println("Exiting the program...");
                break;  // Exit the loop if 'exit' is entered
            }

            // Create the appropriate role
            switch (roleInput) {
                case "user":
                    role = new User();
                    break;
                // You can add cases for other roles like center and buyers
                case "centers":
                    role = new Center();
                    break;
                case "buyers":
                    role = new Buyers();
                    break;
                default:
                    System.out.println("Invalid role entered.");
                    
            }

            // If role is valid, perform the action
            if (role != null) {
                role.performAction();
            }
        }

        sc.close();
    }
}
