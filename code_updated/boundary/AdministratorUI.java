package boundary;

import controller.*;
import entity.*;
import entity.enums.*;
import repository.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

public class AdministratorUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final AppointmentController appointmentController;
    private final MedicineRepository medicineRepository;
    private final StaffRepository staffRepository;
    
    public AdministratorUI(Scanner scanner, AuthenticationController authController,
                          AppointmentController appointmentController) {
        this.scanner = scanner;
        this.authController = authController;
        this.appointmentController = appointmentController;
        this.medicineRepository = MedicineRepository.getInstance();
        this.staffRepository = StaffRepository.getInstance();
    }
    
    private void changePassword(Administrator admin) {
        System.out.print("Enter old password: ");
        String oldPassword = scanner.nextLine();
        
        System.out.print("Enter new password (min 8 characters): ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }
        
        if (newPassword.length() < 8) {
            System.out.println("New password must be at least 8 characters!");
            return;
        }
        
        if (authController.changePassword(admin, oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Check your old password.");
        }
    }
    
    private void viewAllStaff(Administrator admin) {
        System.out.println("\nHospital Staff List:");
        System.out.println("\nDoctors:");
        staffRepository.findAllDoctors().forEach(doctor -> 
            System.out.printf("ID: %s, Name: %s, Specialization: %s%n",
                doctor.getHospitalId(), doctor.getName(), doctor.getSpecialization()));
        
        System.out.println("\nPharmacists:");
        staffRepository.findAll().stream()
            .filter(staff -> staff instanceof Pharmacist)
            .forEach(pharmacist -> 
                System.out.printf("ID: %s, Name: %s%n",
                    pharmacist.getHospitalId(), pharmacist.getName()));
        
        System.out.println("\nAdministrators:");
        staffRepository.findAll().stream()
            .filter(staff -> staff instanceof Administrator)
            .forEach(admin2 -> 
                System.out.printf("ID: %s, Name: %s%n",
                    admin2.getHospitalId(), admin2.getName()));
    }
    
    private void addNewStaff(Administrator admin) {
        System.out.println("\nAdd New Staff Member");
        System.out.println("1. Doctor");
        System.out.println("2. Pharmacist");
        System.out.println("3. Administrator");
        System.out.print("Enter staff type (1-3): ");
        
        try {
            int type = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter Hospital ID: ");
            String hospitalId = scanner.nextLine();
            
            if (staffRepository.exists(hospitalId)) {
                System.out.println("Staff ID already exists!");
                return;
            }
            
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            
            User newStaff;
            switch (type) {
                case 1 -> {
                    System.out.print("Enter Specialization: ");
                    String specialization = scanner.nextLine();
                    newStaff = new Doctor(hospitalId, "password", name, specialization);
                }
                case 2 -> newStaff = new Pharmacist(hospitalId, "password", name);
                case 3 -> newStaff = new Administrator(hospitalId, "password", name);
                default -> {
                    System.out.println("Invalid staff type!");
                    return;
                }
            }
            
            // First save to repository to ensure persistence
            staffRepository.save(newStaff);
            // Then add to admin's managed staff
            admin.addStaffMember(newStaff);
            
            System.out.println("Staff member added successfully!");
            System.out.println("Default password is 'password'. Please ask staff to change it on first login.");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void updateStaff(Administrator admin) {
        try {
            System.out.print("Enter Hospital ID of staff to update: ");
            String hospitalId = scanner.nextLine();
            
            Optional<User> staffOpt = staffRepository.findById(hospitalId);
            if (staffOpt.isEmpty()) {
                System.out.println("Staff member not found!");
                return;
            }
            
            User staff = staffOpt.get();
            System.out.print("Enter new name (or press Enter to keep current): ");
            String newName = scanner.nextLine();
            
            User updatedStaff;
            if (staff instanceof Doctor doctor) {
                // Use current name if no new name provided
                String finalName = newName.isEmpty() ? doctor.getName() : newName;
                
                System.out.print("Enter new specialization (or press Enter to keep current): ");
                String newSpec = scanner.nextLine();
                String finalSpec = newSpec.isEmpty() ? doctor.getSpecialization() : newSpec;
                
                // Create updated doctor with either new or current values
                updatedStaff = new Doctor(hospitalId, staff.getPassword(), finalName, finalSpec);
                
                // Show what was updated
                boolean nameChanged = !newName.isEmpty();
                boolean specChanged = !newSpec.isEmpty();
                if (nameChanged || specChanged) {
                    System.out.println("\nUpdated fields:");
                    if (nameChanged) System.out.println("Name: " + finalName);
                    if (specChanged) System.out.println("Specialization: " + finalSpec);
                } else {
                    System.out.println("No changes made.");
                    return;
                }
            } else {
                // For non-doctor staff, only update if new name provided
                if (newName.isEmpty()) {
                    System.out.println("No changes made.");
                    return;
                }
                
                updatedStaff = switch (staff) {
                    case Pharmacist p -> new Pharmacist(hospitalId, p.getPassword(), newName);
                    case Administrator a -> new Administrator(hospitalId, a.getPassword(), newName);
                    default -> staff;
                };
            }
            
            // Update in repository and admin's managed staff
            staffRepository.save(updatedStaff);
            admin.updateStaffMember(updatedStaff);
            
            System.out.println("Staff member updated successfully!");
            
        } catch (IllegalArgumentException e) {
            System.out.println("Error updating staff: " + e.getMessage());
        }
    }
    
    private void removeStaff(Administrator admin) {
        try {
            System.out.print("Enter Hospital ID of staff to remove: ");
            String hospitalId = scanner.nextLine();
            
            Optional<User> staffOpt = staffRepository.findById(hospitalId);
            if (staffOpt.isEmpty()) {
                System.out.println("Staff member not found!");
                return;
            }
            
            User staff = staffOpt.get();
            if (staff.getHospitalId().equals(admin.getHospitalId())) {
                System.out.println("Cannot remove yourself!");
                return;
            }
            
            System.out.print("Are you sure you want to remove " + staff.getName() + "? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                // First remove from admin's managed staff
                admin.removeStaffMember(staff);
                // Then remove from repository
                staffRepository.delete(hospitalId);
                System.out.println("Staff member removed successfully!");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error removing staff: " + e.getMessage());
        }
    }
    
    private void manageHospitalStaff(Administrator admin) {
    while (true) {
        System.out.println("\nStaff Management");
        System.out.println("1. View All Staff");
        System.out.println("2. Add New Staff");
        System.out.println("3. Update Staff");
        System.out.println("4. Remove Staff");
        System.out.println("5. Back to Main Menu");
        
        System.out.print("Enter choice (1-5): ");
        int choice = Integer.parseInt(scanner.nextLine());
        
        switch (choice) {
            case 1 -> viewAllStaff(admin);
            case 2 -> addNewStaff(admin);
            case 3 -> updateStaff(admin);
            case 4 -> removeStaff(admin);
            case 5 -> {
                return;
            }
            default -> System.out.println("Invalid choice!");
        }
    }
}

    
    private void displayAppointmentDetails(Appointment appointment) {
        System.out.println("\n================================");
        System.out.printf("Appointment ID: %s%n", appointment.getAppointmentId());
        System.out.printf("Patient: %s (ID: %s)%n", 
            appointment.getPatient().getName(), 
            appointment.getPatient().getHospitalId());
        System.out.printf("Doctor: %s (ID: %s)%n", 
            appointment.getDoctor().getName(), 
            appointment.getDoctor().getHospitalId());
        System.out.printf("Date/Time: %s%n", appointment.getDateTime());
        System.out.printf("Status: %s%n", appointment.getStatus());
        
        if (appointment.getOutcomeRecord() != null) {
            System.out.println("\nOutcome Record:");
            System.out.println(appointment.getOutcomeRecord());
        }
    }
    
    private void manageMedicationInventory(Administrator admin) {
        while (true) {
            System.out.println("\nMedication Inventory Management");
            System.out.println("1. View Inventory");
            System.out.println("2. Update Stock Level");
            System.out.println("3. Update Low Stock Alert Level");
            System.out.println("4. View Inventory Actions");
            System.out.println("5. Back to Main Menu");
            
            System.out.print("Enter choice (1-5): ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1 -> viewInventory();
                case 2 -> updateStockLevel(admin);
                case 3 -> updateAlertLevel(admin);
                case 4 -> viewInventoryActions(admin);
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }
    
    private void viewInventory() {
        System.out.println("\nCurrent Inventory:");
        medicineRepository.findAll().forEach(medicine -> {
            System.out.println("\n" + medicine);
            if (medicine.isLowStock()) {
                System.out.println("*** LOW STOCK ALERT ***");
            }
        });
    }
    
    private void updateStockLevel(Administrator admin) {
        List<Medicine> medicines = medicineRepository.findAll();
        System.out.println("\nSelect Medicine:");
        for (int i = 0; i < medicines.size(); i++) {
            System.out.printf("%d. %s (Current Stock: %d)%n", 
                i + 1, 
                medicines.get(i).getName(), 
                medicines.get(i).getCurrentStock());
        }
        
        System.out.print("Enter medicine number (1-" + medicines.size() + "): ");
        int medicineNum = Integer.parseInt(scanner.nextLine());
        
        if (medicineNum < 1 || medicineNum > medicines.size()) {
            System.out.println("Invalid selection!");
            return;
        }
        
        Medicine medicine = medicines.get(medicineNum - 1);
        System.out.print("Enter quantity change (positive to add, negative to remove): ");
        int quantity = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Enter reason for adjustment: ");
        String reason = scanner.nextLine();
        
        try {
            admin.updateMedicineStock(medicine, quantity, reason);
            System.out.println("Stock updated successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void updateAlertLevel(Administrator admin) {
        List<Medicine> medicines = medicineRepository.findAll();
        System.out.println("\nSelect Medicine:");
        for (int i = 0; i < medicines.size(); i++) {
            System.out.printf("%d. %s (Current Alert Level: %d)%n", 
                i + 1, 
                medicines.get(i).getName(), 
                medicines.get(i).getLowStockAlert());
        }
        
        System.out.print("Enter medicine number (1-" + medicines.size() + "): ");
        int medicineNum = Integer.parseInt(scanner.nextLine());
        
        if (medicineNum < 1 || medicineNum > medicines.size()) {
            System.out.println("Invalid selection!");
            return;
        }
        
        Medicine medicine = medicines.get(medicineNum - 1);
        System.out.print("Enter new alert level: ");
        int newLevel = Integer.parseInt(scanner.nextLine());
        
        try {
            admin.updateMedicineLowStockAlert(medicine, newLevel);
            System.out.println("Alert level updated successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void viewInventoryActions(Administrator admin) {
        System.out.println("\nInventory Actions History:");
        admin.getInventoryActions().forEach(action -> {
            System.out.println("\n--------------------------------");
            System.out.printf("Medicine: %s%n", action.getMedicine().getName());
            System.out.printf("Action: %s%n", action.getActionType());
            System.out.printf("Quantity: %d%n", action.getQuantity());
            System.out.printf("Date/Time: %s%n", action.getActionDateTime());
            System.out.printf("Performed By: %s%n", action.getPerformedBy().getName());
            if (action.getReason() != null) {
                System.out.printf("Reason: %s%n", action.getReason());
            }
        });
    }

    private void approveReplenishmentRequests(Administrator admin) {
        List<ReplenishmentRequest> pendingRequests = staffRepository.findAll().stream()
            .filter(staff -> staff instanceof Pharmacist)
            .map(staff -> (Pharmacist) staff)
            .flatMap(pharmacist -> pharmacist.getReplenishmentRequests().stream())
            .filter(request -> request.getStatus() == ReplenishmentStatus.PENDING)
            .toList();
            
        if (pendingRequests.isEmpty()) {
            System.out.println("No pending replenishment requests!");
            return;
        }
        
        System.out.println("\nPending Replenishment Requests:");
        for (int i = 0; i < pendingRequests.size(); i++) {
            ReplenishmentRequest request = pendingRequests.get(i);
            System.out.printf("%d. Medicine: %s%n", i + 1, request.getMedicine().getName());
            System.out.printf("   Requested By: %s%n", request.getRequestedBy().getName());
            System.out.printf("   Quantity: %d%n", request.getRequestedQuantity());
            System.out.printf("   Current Stock: %d%n", request.getMedicine().getCurrentStock());
            System.out.printf("   Request Date: %s%n", request.getRequestDateTime());
            System.out.println();
        }
        
        try {
            System.out.print("Enter request number to process (1-" + pendingRequests.size() + "): ");
            int requestNum = Integer.parseInt(scanner.nextLine());
            
            if (requestNum < 1 || requestNum > pendingRequests.size()) {
                System.out.println("Invalid request number!");
                return;
            }
            
            ReplenishmentRequest selectedRequest = pendingRequests.get(requestNum - 1);
            
            // Add response validation loop
            String response;
            while (true) {
                System.out.print("Approve request? (y/n): ");
                response = scanner.nextLine().toLowerCase().trim();
                
                if (response.equals("y") || response.equals("n")) {
                    break;
                }
                System.out.println("Please enter 'y' for yes or 'n' for no.");
            }
            
            admin.processReplenishmentRequest(selectedRequest, response.equals("y"));
            System.out.println("Request " + 
                (response.equals("y") ? "approved" : "rejected") + 
                " successfully!");
                
            if (response.equals("y")) {
                Medicine medicine = selectedRequest.getMedicine();
                System.out.printf("New stock level for %s: %d%n", 
                    medicine.getName(), 
                    medicine.getCurrentStock());
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error processing request: " + e.getMessage());
        }
    }
    
    
    public void show(Administrator admin) {
        while (true) {
            try {
                System.out.println("\nAdministrator Menu");
                System.out.println("1. Change Password");
                System.out.println("2. View and Manage Hospital Staff");
                System.out.println("3. View Appointment Details");
                System.out.println("4. View and Manage Medication Inventory");
                System.out.println("5. Approve Replenishment Requests");
                System.out.println("6. Logout");
                
                System.out.print("Enter your choice (1-6): ");
                int choice = Integer.parseInt(scanner.nextLine());
                
                switch (choice) {
                    case 1 -> changePassword(admin);
                    case 2 -> manageHospitalStaff(admin); // Use the correct method name
                    case 3 -> viewAppointmentDetails(admin); // Add admin parameter
                    case 4 -> manageMedicationInventory(admin);
                    case 5 -> approveReplenishmentRequests(admin);
                    case 6 -> {
                        System.out.println("Logging out...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private void viewAppointmentDetails(Administrator admin) {
    while (true) {
        System.out.println("\nView Appointments");
        System.out.println("1. View All Appointments");
        System.out.println("2. View Appointments by Date");
        System.out.println("3. View Appointments by Doctor");
        System.out.println("4. View Appointments by Status");
        System.out.println("5. Back to Main Menu");
        
        System.out.print("Enter choice (1-5): ");
        int choice = Integer.parseInt(scanner.nextLine());
        
        List<Appointment> appointments = new ArrayList<>();
        
        try {
            switch (choice) {
                case 1 -> appointments = appointmentController.getAllAppointments();
                case 2 -> {
                    System.out.print("Enter date (YYYY-MM-DD): ");
                    LocalDate date = LocalDate.parse(scanner.nextLine());
                    appointments = appointmentController.getAppointmentsByDate(date);
                }
                case 3 -> {
                    System.out.print("Enter Doctor ID: ");
                    String doctorId = scanner.nextLine();
                    Optional<User> doctor = staffRepository.findById(doctorId);
                    if (doctor.isPresent() && doctor.get() instanceof Doctor) {
                        appointments = appointmentController.getAllAppointments((Doctor) doctor.get());
                    } else {
                        System.out.println("Invalid doctor ID!");
                    }
                }
                case 4 -> {
                    System.out.println("Select status:");
                    System.out.println("1. Pending Approval");
                    System.out.println("2. Confirmed");
                    System.out.println("3. Cancelled");
                    System.out.println("4. Completed");
                    System.out.print("Enter choice (1-4): ");
                    int statusChoice = Integer.parseInt(scanner.nextLine());
                    AppointmentStatus status = switch (statusChoice) {
                        case 1 -> AppointmentStatus.PENDING_APPROVAL;
                        case 2 -> AppointmentStatus.CONFIRMED;
                        case 3 -> AppointmentStatus.CANCELLED;
                        case 4 -> AppointmentStatus.COMPLETED;
                        default -> null;
                    };
                    if (status != null) {
                        appointments = appointmentController.getAppointmentsByStatus(status);
                    } else {
                        System.out.println("Invalid status choice!");
                    }
                }
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }

            if (!appointments.isEmpty()) {
                appointments.forEach(this::displayAppointmentDetails);
            } else {
                System.out.println("No appointments found!");
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}


    
}
