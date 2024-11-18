// LoginUI.java
package boundary;

import controller.AuthenticationController;
import entity.User;
import java.util.Scanner;

public class LoginUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    
    public LoginUI(Scanner scanner, AuthenticationController authController) {
        this.scanner = scanner;
        this.authController = authController;
    }
    
    public User show() {
        while (true) {
            displayHeader();
            
            try {
                System.out.print("Please select your domain (1-5): ");
                String choice = scanner.nextLine();
                
                if (choice.equals("5")) {
                    return null; // Exit system
                }
                
                System.out.print("Please enter your hospitalID: ");
                String hospitalId = scanner.nextLine();
                
                System.out.print("Please enter your password: ");
                String password = scanner.nextLine();
                
                User user = authController.login(hospitalId, password);
                
                if (user != null) {
                    if (password.equals("password")) {
                        handleFirstTimeLogin(user);
                    }
                    return user;
                } else {
                    System.out.println("Invalid credentials. Please try again.");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }
    
    private void displayHeader() {
        System.out.println("\nWelcome to the Hospital Management System!");
        System.out.println("====================================================");
        System.out.println("1. Patient");
        System.out.println("2. Doctor");
        System.out.println("3. Pharmacist");
        System.out.println("4. Administrator");
        System.out.println("5. Exit System");
        System.out.println("=====================================================");
    }
    
    private void handleFirstTimeLogin(User user) {
        System.out.println("\nFirst time login detected. You must change your password.");
        while (true) {
            System.out.print("Enter new password (min 8 characters): ");
            String newPassword = scanner.nextLine();
            
            System.out.print("Confirm new password: ");
            String confirmPassword = scanner.nextLine();
            
            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match!");
                continue;
            }
            
            if (newPassword.length() < 8) {
                System.out.println("Password must be at least 8 characters!");
                continue;
            }
            
            if (authController.changePassword(user, "password", newPassword)) {
                System.out.println("Password changed successfully!");
                break;
            } else {
                System.out.println("Failed to change password. Please try again.");
            }
        }
    }
}