package boundary;

import controller.*;
import entity.*;
import entity.enums.*;
import repository.MedicineRepository;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

public class DoctorUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final AppointmentController appointmentController;
    private final MedicalRecordController medicalRecordController;
    private final DoctorAvailabilityController availabilityController;

    public DoctorUI(Scanner scanner, AuthenticationController authController,
                   AppointmentController appointmentController,
                   MedicalRecordController medicalRecordController,
                   DoctorAvailabilityController availabilityController) {
        this.scanner = scanner;
        this.authController = authController;
        this.appointmentController = appointmentController;
        this.medicalRecordController = medicalRecordController;
        this.availabilityController = availabilityController;
    }

    public void show(Doctor doctor) {
        while (true) {
            try {
                displayMenu();
                System.out.print("Enter your choice (1-9): ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> changePassword(doctor);
                    case 2 -> viewPatientMedicalRecords(doctor);
                    case 3 -> updatePatientMedicalRecords(doctor);
                    case 4 -> viewPersonalSchedule(doctor);
                    case 5 -> setAvailability(doctor);
                    case 6 -> handleAppointmentRequests(doctor);
                    case 7 -> viewUpcomingAppointments(doctor);
                    case 8 -> recordAppointmentOutcome(doctor);
                    case 9 -> {
                        System.out.println("Logging out...");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void displayMenu() {
        System.out.println("\nDoctor Menu");
        System.out.println("1. Change Password");
        System.out.println("2. View Patient Medical Records");
        System.out.println("3. Update Patient Medical Records");
        System.out.println("4. View Personal Schedule");
        System.out.println("5. Set Availability for Appointments");
        System.out.println("6. Accept/Decline Appointment Requests");
        System.out.println("7. View Upcoming Appointments");
        System.out.println("8. Record Appointment Outcome");
        System.out.println("9. Logout");
    }

    private void changePassword(Doctor doctor) {
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

        if (authController.changePassword(doctor, oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Check your old password.");
        }
    }

    private void viewPatientMedicalRecords(Doctor doctor) {
        List<Appointment> allAppointments = appointmentController.getAllAppointments(doctor);
        Set<Patient> patients = new HashSet<>();
        
        // Get unique patients from all appointments
        allAppointments.forEach(apt -> patients.add(apt.getPatient()));
        
        if (patients.isEmpty()) {
            System.out.println("No patients found under your care.");
            return;
        }

        System.out.println("\nYour Patients:");
        List<Patient> sortedPatients = new ArrayList<>(patients);
        sortedPatients.sort(Comparator.comparing(Patient::getName));

        for (int i = 0; i < sortedPatients.size(); i++) {
            System.out.printf("%d. %s (ID: %s)%n", 
                i + 1, 
                sortedPatients.get(i).getName(),
                sortedPatients.get(i).getHospitalId());
        }

        System.out.print("\nEnter patient number to view (1-" + patients.size() + "): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > patients.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            Patient selectedPatient = sortedPatients.get(choice - 1);
            MedicalRecord record = medicalRecordController.getMedicalRecord(selectedPatient.getHospitalId());
            System.out.println("\nMedical Record:");
            System.out.println(record);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    private void updatePatientMedicalRecords(Doctor doctor) {
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();

        try {
            // Verify that this patient has had appointments with this doctor
            boolean isAuthorized = appointmentController.getAllAppointments(doctor).stream()
                .anyMatch(apt -> apt.getPatient().getHospitalId().equals(patientId));

            if (!isAuthorized) {
                System.out.println("You are not authorized to update this patient's records.");
                return;
            }

            System.out.print("Enter diagnosis: ");
            String diagnosis = scanner.nextLine();

            System.out.print("Enter treatment plan: ");
            String treatment = scanner.nextLine();

            medicalRecordController.addDiagnosis(patientId, diagnosis, treatment);
            System.out.println("Medical record updated successfully!");

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewPersonalSchedule(Doctor doctor) {
        System.out.print("Enter date to view (YYYY-MM-DD) or press Enter for today: ");
        String dateStr = scanner.nextLine();
    
        LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
    
        // Get availability for the date
        DoctorAvailability availability = doctor.getAvailability(date);
        if (availability != null) {
            System.out.printf("\nAvailability for %s:%n", date);
            System.out.printf("Available from %s to %s%n", 
                availability.getStartTime(), 
                availability.getEndTime());
        } else {
            System.out.println("No availability set for this date.");
        }
    
        // Get all appointments for the date
        List<Appointment> appointments = appointmentController.getAllAppointments(doctor).stream()
            .filter(apt -> apt.getDateTime().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    
        if (!appointments.isEmpty()) {
            System.out.println("\nAppointments:");
            appointments.forEach(apt -> {
                System.out.println("\n--------------------------------");
                System.out.printf("Appointment ID: %s%n", apt.getAppointmentId());
                System.out.printf("Patient: %s%n", apt.getPatient().getName());
                System.out.printf("Time: %s%n", apt.getDateTime().toLocalTime());
                System.out.printf("Status: %s%n", apt.getStatus());
            });
        } else {
            System.out.println("No appointments scheduled for this date.");
        }
    }
    
    

    private void setAvailability(Doctor doctor) {
        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());

            if (date.isBefore(LocalDate.now())) {
                System.out.println("Cannot set availability for past dates!");
                return;
            }

            System.out.print("Enter start time (HH:mm): ");
            LocalTime startTime = LocalTime.parse(scanner.nextLine());

            System.out.print("Enter end time (HH:mm): ");
            LocalTime endTime = LocalTime.parse(scanner.nextLine());

            availabilityController.setAvailability(doctor, date, startTime, endTime);
            System.out.println("Availability set successfully!");

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format!");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleAppointmentRequests(Doctor doctor) {
        List<Appointment> pendingAppointments = appointmentController.getPendingAppointments(doctor);
        
        if (pendingAppointments.isEmpty()) {
            System.out.println("No pending appointment requests.");
            return;
        }
    
        System.out.println("\nPending Appointment Requests:");
        for (int i = 0; i < pendingAppointments.size(); i++) {
            Appointment apt = pendingAppointments.get(i);
            System.out.printf("%d. Patient: %s, Date/Time: %s%n",
                i + 1,
                apt.getPatient().getName(),
                apt.getDateTime());
        }
    
        System.out.print("\nEnter appointment number to process (1-" + 
            pendingAppointments.size() + "): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > pendingAppointments.size()) {
                System.out.println("Invalid choice!");
                return;
            }
    
            Appointment selectedAppointment = pendingAppointments.get(choice - 1);
            
            // Add response validation loop
            String response;
            while (true) {
                System.out.print("Accept appointment? (y/n): ");
                response = scanner.nextLine().toLowerCase().trim();
                
                if (response.equals("y") || response.equals("n")) {
                    break;
                }
                System.out.println("Please enter 'y' for yes or 'n' for no.");
            }
    
            AppointmentStatus newStatus = response.equals("y") ? 
                AppointmentStatus.CONFIRMED : AppointmentStatus.CANCELLED;
    
            if (appointmentController.updateAppointmentStatus(
                selectedAppointment.getAppointmentId(), newStatus)) {
                System.out.println("Appointment " + 
                    (response.equals("y") ? "accepted!" : "declined!"));
            } else {
                System.out.println("Failed to update appointment status.");
            }
    
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
    

    private void viewUpcomingAppointments(Doctor doctor) {
        List<Appointment> upcomingAppointments = 
            appointmentController.getUpcomingAppointments(doctor);
    
        if (upcomingAppointments.isEmpty()) {
            System.out.println("No upcoming appointments.");
            return;
        }
    
        System.out.println("\nUpcoming Appointments:");
        upcomingAppointments.forEach(apt -> {
            System.out.println("\n--------------------------------");
            System.out.printf("Appointment ID: %s%n", apt.getAppointmentId());
            System.out.printf("Patient: %s%n", apt.getPatient().getName());
            System.out.printf("Date/Time: %s%n", apt.getDateTime());
            System.out.printf("Status: %s%n", apt.getStatus());
        });
    }
    

    private void recordAppointmentOutcome(Doctor doctor) {
        try {
            List<Appointment> confirmedAppointments = appointmentController.getUpcomingAppointments(doctor);
            
            if (confirmedAppointments.isEmpty()) {
                System.out.println("No confirmed appointments to record outcome for.");
                return;
            }
    
            System.out.println("\nConfirmed Appointments:");
            for (int i = 0; i < confirmedAppointments.size(); i++) {
                Appointment apt = confirmedAppointments.get(i);
                System.out.printf("%d. Patient: %s, Date/Time: %s%n",
                    i + 1,
                    apt.getPatient().getName(),
                    apt.getDateTime());
            }
    
            System.out.print("\nEnter appointment number to record outcome (1-" + 
                confirmedAppointments.size() + "): ");
                
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > confirmedAppointments.size()) {
                System.out.println("Invalid choice!");
                return;
            }
    
            Appointment selectedAppointment = confirmedAppointments.get(choice - 1);
    
            System.out.print("Enter service type: ");
            String serviceType = scanner.nextLine();
    
            System.out.print("Enter number of prescriptions to add: ");
            int numPrescriptions = Integer.parseInt(scanner.nextLine());
    
            List<Prescription> prescriptions = new ArrayList<>();
            MedicineRepository medicineRepo = MedicineRepository.getInstance();
    
            for (int i = 0; i < numPrescriptions; i++) {
                System.out.printf("\nPrescription %d:%n", i + 1);
                
                // Show available medicines
                System.out.println("Available medicines:");
                List<Medicine> availableMedicines = medicineRepo.findAll();
                for (int j = 0; j < availableMedicines.size(); j++) {
                    Medicine med = availableMedicines.get(j);
                    System.out.printf("%d. %s (Stock: %d)%n", 
                        j + 1, 
                        med.getName(), 
                        med.getCurrentStock());
                }
                
                System.out.print("Select medicine (1-" + availableMedicines.size() + "): ");
                int medChoice = Integer.parseInt(scanner.nextLine());
                
                if (medChoice < 1 || medChoice > availableMedicines.size()) {
                    System.out.println("Invalid medicine choice!");
                    return;
                }
                
                Medicine selectedMedicine = availableMedicines.get(medChoice - 1);
                
                System.out.print("Enter quantity: ");
                int quantity = Integer.parseInt(scanner.nextLine());
                
                if (quantity <= 0) {
                    System.out.println("Quantity must be positive!");
                    return;
                }
                
                if (!selectedMedicine.canFulfillQuantity(quantity)) {
                    System.out.printf("Insufficient stock! Available: %d%n", 
                        selectedMedicine.getCurrentStock());
                    return;
                }
    
                prescriptions.add(new Prescription(selectedMedicine, quantity));
            }
    
            System.out.print("Enter consultation notes: ");
            String notes = scanner.nextLine();
    
            appointmentController.recordAppointmentOutcome(
                selectedAppointment.getAppointmentId(),
                serviceType,
                prescriptions,
                notes
            );
    
            System.out.println("Appointment outcome recorded successfully!");
    
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}