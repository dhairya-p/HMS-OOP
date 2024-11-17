// PharmacistUI.java
package boundary;

import controller.*;
import entity.*;
import entity.enums.*;
import repository.*;
import java.util.*;

public class PharmacistUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final AppointmentController appointmentController;
    private MedicineRepository medicineRepository;
    
    public PharmacistUI(Scanner scanner, AuthenticationController authController, 
                       AppointmentController appointmentController) {
        this.scanner = scanner;
        this.authController = authController;
        this.appointmentController = appointmentController;
        this.medicineRepository = MedicineRepository.getInstance();
    }
    
    public void show(Pharmacist pharmacist) {
        while (true) {
            System.out.println("\nPharmacist Menu");
            System.out.println("1. Change Password");
            System.out.println("2. View Appointment Outcome Records");
            System.out.println("3. Update Prescription Status");
            System.out.println("4. View Medication Inventory");
            System.out.println("5. Submit Replenishment Request");
            System.out.println("6. Logout");
            
            System.out.print("Enter your choice (1-6): ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> changePassword(pharmacist);
                case 2 -> viewAppointmentOutcomes();
                case 3 -> updatePrescriptionStatus();
                case 4 -> viewMedicationInventory();
                case 5 -> submitReplenishmentRequest(pharmacist);
                case 6 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void changePassword(Pharmacist pharmacist) {
        System.out.print("Enter old password: ");
        String oldPassword = scanner.nextLine();
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }
        
        if (newPassword.length() < 8) {
            System.out.println("New password must be at least 8 characters long!");
            return;
        }
        
        if (authController.changePassword(pharmacist, oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Please check your old password.");
        }
    }
    
    private void viewAppointmentOutcomes() {
        System.out.println("\nAppointment Outcomes with Pending Prescriptions:");
        List<Appointment> completedAppointments = appointmentController.getAllAppointments().stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.COMPLETED)
            .filter(apt -> apt.getOutcomeRecord() != null)
            .toList();
            
        if (completedAppointments.isEmpty()) {
            System.out.println("No completed appointments found.");
            return;
        }
    
        for (Appointment apt : completedAppointments) {
            System.out.println("\n=================================");
            System.out.printf("Appointment ID: %s%n", apt.getAppointmentId());
            System.out.printf("Date: %s%n", apt.getDateTime().toLocalDate());
            System.out.printf("Patient: %s%n", apt.getPatient().getName());
            System.out.printf("Doctor: %s%n", apt.getDoctor().getName());
            
            AppointmentOutcomeRecord outcome = apt.getOutcomeRecord();
            System.out.printf("Service: %s%n", outcome.getServiceType());
            System.out.println("\nPrescriptions:");
            
            if (outcome.getPrescriptions().isEmpty()) {
                System.out.println("No prescriptions");
            } else {
                outcome.getPrescriptions().forEach(p -> 
                    System.out.printf("- %s (Quantity: %d) - Status: %s%n",
                        p.getMedicine().getName(),
                        p.getQuantity(),
                        p.getStatus()));
            }
            
            System.out.printf("\nNotes: %s%n", outcome.getConsultationNotes());
        }
    }
    
    
    
    
    private void updatePrescriptionStatus() {
        System.out.print("Enter Appointment ID: ");
        String appointmentId = scanner.nextLine();
        
        Optional<Appointment> optAppointment = appointmentController.getAppointmentById(appointmentId);
        
        if (optAppointment.isEmpty()) {
            System.out.println("Appointment not found!");
            return;
        }
        
        Appointment appointment = optAppointment.get();
        
        if (appointment.getStatus() != AppointmentStatus.COMPLETED || 
            appointment.getOutcomeRecord() == null ||
            appointment.getOutcomeRecord().getPrescriptions().isEmpty()) {
            System.out.println("No prescriptions found for this appointment.");
            return;
        }
        
        AppointmentOutcomeRecord outcome = appointment.getOutcomeRecord();
        List<Prescription> prescriptions = outcome.getPrescriptions();
        
        System.out.println("\nPrescriptions for Appointment " + appointmentId + ":");
        for (int i = 0; i < prescriptions.size(); i++) {
            Prescription p = prescriptions.get(i);
            System.out.printf("%d. %s (Quantity: %d) - Status: %s%n",
                i + 1,
                p.getMedicine().getName(),
                p.getQuantity(),
                p.getStatus());
        }
        
        System.out.print("\nSelect prescription to update (1-" + prescriptions.size() + "): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > prescriptions.size()) {
                System.out.println("Invalid choice!");
                return;
            }
            
            Prescription selectedPrescription = prescriptions.get(choice - 1);
            
            if (selectedPrescription.getStatus() == PrescriptionStatus.DISPENSED) {
                System.out.println("This prescription has already been dispensed.");
                return;
            }
            
            // Check medicine stock
            Medicine medicine = selectedPrescription.getMedicine();
            if (!medicine.canFulfillQuantity(selectedPrescription.getQuantity())) {
                System.out.println("Insufficient stock to dispense this prescription!");
                return;
            }
            
            if (this.dispenseMedication(selectedPrescription)) {
                System.out.println("Prescription status updated to DISPENSED successfully!");
                System.out.printf("Updated stock for %s: %d%n", 
                    medicine.getName(), 
                    medicine.getCurrentStock());
            } else {
                System.out.println("Failed to dispense medication!");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number!");
        }
    }
    
    
    private void viewMedicationInventory() {
        System.out.println("\nCurrent Medication Inventory:");
        List<Medicine> medicines = medicineRepository.findAll();
        medicines.forEach(med -> {
            System.out.println("\n" + med);
            if (med.isLowStock()) {
                System.out.println("*** LOW STOCK ALERT ***");
            }
        });
    }
    
    private void submitReplenishmentRequest(Pharmacist pharmacist) {
        System.out.println("\nLow Stock Medicines:");
        List<Medicine> lowStockMeds = medicineRepository.findAll().stream()
            .filter(Medicine::isLowStock)
            .filter(med -> !med.isReplenishmentRequested())
            .toList();
            
        if (lowStockMeds.isEmpty()) {
            System.out.println("No medicines require replenishment!");
            return;
        }
        
        for (int i = 0; i < lowStockMeds.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, lowStockMeds.get(i));
        }
        
        System.out.print("Enter medicine number to request replenishment (1-" + 
            lowStockMeds.size() + "): ");
        int medicineNum = Integer.parseInt(scanner.nextLine());
        
        if (medicineNum < 1 || medicineNum > lowStockMeds.size()) {
            System.out.println("Invalid medicine number!");
            return;
        }
        
        System.out.print("Enter quantity to request: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        
        Medicine medicine = lowStockMeds.get(medicineNum - 1);
        try {
            ReplenishmentRequest request = pharmacist.createReplenishmentRequest(medicine, quantity);
            System.out.println("Replenishment request submitted successfully!");
            System.out.println("Request details: " + request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error creating request: " + e.getMessage());
        }
    }

    private boolean dispenseMedication(Prescription prescription) {
        try {
            // Get the medicine and required quantity
            Medicine medicine = prescription.getMedicine();
            int requestedQuantity = prescription.getQuantity();
            
            // Verify there's sufficient stock
            if (!medicine.canFulfillQuantity(requestedQuantity)) {
                System.out.println("Error: Insufficient stock available");
                return false;
            }
            
            // Update medicine stock (reduce by using negative quantity)
            if (!medicine.updateStock(-requestedQuantity)) {
                System.out.println("Error: Failed to update medicine stock");
                return false;
            }
            
            // Update prescription status to dispensed
            prescription.setStatus(PrescriptionStatus.DISPENSED);
            
            // Save changes to medicine repository
            medicineRepository.save(medicine);
            
            return true;
        } catch (Exception e) {
            System.out.println("Error occurred while dispensing medication: " + e.getMessage());
            return false;
        }
    }
    
    
}