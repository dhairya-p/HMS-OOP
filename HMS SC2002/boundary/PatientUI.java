package boundary;

import controller.*;
import controller.interfaces.DoctorAvailabilityService;
import entity.*;
import entity.enums.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PatientUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final AppointmentController appointmentController;
    private final MedicalRecordController medicalRecordController;
    private final PatientController patientController;
    private final DoctorAvailabilityService availabilityService;

    public PatientUI(Scanner scanner, AuthenticationController authController,
                    AppointmentController appointmentController,
                    MedicalRecordController medicalRecordController,
                    PatientController patientController) {
        this.scanner = scanner;
        this.authController = authController;
        this.appointmentController = appointmentController;
        this.medicalRecordController = medicalRecordController;
        this.patientController = patientController;
        this.availabilityService = appointmentController.getAvailabilityService();
    }


    public void show(Patient patient) {
        while (true) {
            try {
                displayMenu();
                System.out.print("Enter your choice (1-10): ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> changePassword(patient);
                    case 2 -> viewMedicalRecord(patient);
                    case 3 -> updatePersonalInformation(patient);
                    case 4 -> viewAvailableAppointmentSlots();
                    case 5 -> scheduleAppointment(patient);
                    case 6 -> rescheduleAppointment(patient);
                    case 7 -> cancelAppointment(patient);
                    case 8 -> viewScheduledAppointments(patient);
                    case 9 -> viewPastAppointmentRecords(patient);
                    case 10 -> {
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
        System.out.println("\nPatient Menu");
        System.out.println("1. Change Password");
        System.out.println("2. View Medical Record");
        System.out.println("3. Update Personal Information");
        System.out.println("4. View Available Appointment Slots");
        System.out.println("5. Schedule Appointment");
        System.out.println("6. Reschedule Appointment");
        System.out.println("7. Cancel Appointment");
        System.out.println("8. View Scheduled Appointments");
        System.out.println("9. View Past Appointment Records");
        System.out.println("10. Logout");
    }

    private void changePassword(Patient patient) {
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

        if (authController.changePassword(patient, oldPassword, newPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. Check your old password.");
        }
    }

    private void viewMedicalRecord(Patient patient) {
        try {
            MedicalRecord record = medicalRecordController.getMedicalRecord(patient.getHospitalId());
            System.out.println("\nYour Medical Record:");
            System.out.println(record);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updatePersonalInformation(Patient patient) {
        try {
            System.out.println("\nCurrent Contact Information:");
            MedicalRecord record = medicalRecordController.getMedicalRecord(patient.getHospitalId());
            System.out.println(record.getContactInfo());

            System.out.print("\nEnter new phone number (or press Enter to keep current): ");
            String phone = scanner.nextLine();

            System.out.print("Enter new email (or press Enter to keep current): ");
            String email = scanner.nextLine();

            if (!phone.isEmpty() || !email.isEmpty()) {
                // If either field is empty, keep the current value
                if (phone.isEmpty()) {
                    phone = record.getContactInfo().getPhoneNumber();
                }
                if (email.isEmpty()) {
                    email = record.getContactInfo().getEmail();
                }

                medicalRecordController.updateContactInfo(patient.getHospitalId(), phone, email);
                System.out.println("Contact information updated successfully!");
            } else {
                System.out.println("No changes made.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAvailableAppointmentSlots() {
        try {
            System.out.print("Enter date to view (YYYY-MM-DD) or press Enter for today: ");
            String dateStr = scanner.nextLine();

            LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);

            List<Doctor> availableDoctors = availabilityService.getAvailableDoctors(date);
            if (availableDoctors.isEmpty()) {
                System.out.println("No doctors available on " + date);
                return;
            }

            System.out.println("\nAvailable Doctors:");
            for (int i = 0; i < availableDoctors.size(); i++) {
                Doctor doctor = availableDoctors.get(i);
                System.out.printf("%d. Dr. %s (%s)%n", 
                    i + 1, 
                    doctor.getName(), 
                    doctor.getSpecialization());
            }

            System.out.print("\nSelect doctor number to view slots (1-" + 
                availableDoctors.size() + "): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice < 1 || choice > availableDoctors.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            Doctor selectedDoctor = availableDoctors.get(choice - 1);
            List<AppointmentSlot> availableSlots = 
                appointmentController.getAvailableSlots(date, selectedDoctor);

            if (availableSlots.isEmpty()) {
                System.out.println("No available slots for selected doctor on " + date);
                return;
            }

            System.out.println("\nAvailable Slots:");
            availableSlots.forEach(slot -> 
                System.out.printf("%s - %s%n", 
                    slot.getStartTime(), 
                    slot.getEndTime()));

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void scheduleAppointment(Patient patient) {
        if (!patientController.canScheduleNewAppointment(patient.getHospitalId())) {
            System.out.println("You have reached the maximum number of allowed appointments.");
            return;
        }

        try {
            System.out.print("Enter appointment date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());

            List<Doctor> availableDoctors = availabilityService.getAvailableDoctors(date);
            if (availableDoctors.isEmpty()) {
                System.out.println("No doctors available on " + date);
                return;
            }

            System.out.println("\nAvailable Doctors:");
            for (int i = 0; i < availableDoctors.size(); i++) {
                Doctor doctor = availableDoctors.get(i);
                System.out.printf("%d. Dr. %s (%s)%n", 
                    i + 1, 
                    doctor.getName(), 
                    doctor.getSpecialization());
            }

            System.out.print("\nSelect doctor number (1-" + availableDoctors.size() + "): ");
            int doctorChoice = Integer.parseInt(scanner.nextLine());

            if (doctorChoice < 1 || doctorChoice > availableDoctors.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            Doctor selectedDoctor = availableDoctors.get(doctorChoice - 1);
            List<AppointmentSlot> availableSlots = 
                appointmentController.getAvailableSlots(date, selectedDoctor);

            if (availableSlots.isEmpty()) {
                System.out.println("No available slots for selected doctor on " + date);
                return;
            }

            System.out.println("\nAvailable Slots:");
            for (int i = 0; i < availableSlots.size(); i++) {
                AppointmentSlot slot = availableSlots.get(i);
                System.out.printf("%d. %s - %s%n", 
                    i + 1, 
                    slot.getStartTime(), 
                    slot.getEndTime());
            }

            System.out.print("\nSelect slot number (1-" + availableSlots.size() + "): ");
            int slotChoice = Integer.parseInt(scanner.nextLine());

            if (slotChoice < 1 || slotChoice > availableSlots.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            AppointmentSlot selectedSlot = availableSlots.get(slotChoice - 1);
            Appointment appointment = appointmentController.scheduleAppointment(
                patient, selectedDoctor, selectedSlot);

            if (appointment != null) {
                System.out.println("\nAppointment scheduled successfully!");
                System.out.println("Appointment details:");
                System.out.println(appointment);
            } else {
                System.out.println("Failed to schedule appointment. Please try again.");
            }

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format! Please use YYYY-MM-DD");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void rescheduleAppointment(Patient patient) {
        try {
            List<Appointment> scheduledAppointments = 
                appointmentController.getScheduledAppointments(patient);

            if (scheduledAppointments.isEmpty()) {
                System.out.println("No appointments to reschedule.");
                return;
            }

            System.out.println("\nYour Scheduled Appointments:");
            for (int i = 0; i < scheduledAppointments.size(); i++) {
                Appointment apt = scheduledAppointments.get(i);
                System.out.printf("%d. %s with Dr. %s (Status: %s)%n",
                    i + 1,
                    apt.getDateTime(),
                    apt.getDoctor().getName(),
                    apt.getStatus());
            }

            System.out.print("\nSelect appointment to reschedule (1-" + 
                scheduledAppointments.size() + "): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice < 1 || choice > scheduledAppointments.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            Appointment oldAppointment = scheduledAppointments.get(choice - 1);
            
            // Choose new slot
            System.out.print("Enter new date (YYYY-MM-DD): ");
            LocalDate newDate = LocalDate.parse(scanner.nextLine());

            List<AppointmentSlot> newSlots = 
                appointmentController.getAvailableSlots(newDate, oldAppointment.getDoctor());

            if (newSlots.isEmpty()) {
                System.out.println("No available slots on selected date.");
                return;
            }

            System.out.println("\nAvailable Slots:");
            for (int i = 0; i < newSlots.size(); i++) {
                AppointmentSlot slot = newSlots.get(i);
                System.out.printf("%d. %s - %s%n",
                    i + 1,
                    slot.getStartTime(),
                    slot.getEndTime());
            }

            System.out.print("\nSelect new slot (1-" + newSlots.size() + "): ");
            int slotChoice = Integer.parseInt(scanner.nextLine());

            if (slotChoice < 1 || slotChoice > newSlots.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            AppointmentSlot newSlot = newSlots.get(slotChoice - 1);
            
            if (appointmentController.rescheduleAppointment(
                oldAppointment.getAppointmentId(), newSlot)) {
                System.out.println("Appointment rescheduled successfully!");
            } else {
                System.out.println("Failed to reschedule appointment.");
            }

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void cancelAppointment(Patient patient) {
        List<Appointment> scheduledAppointments = 
            appointmentController.getScheduledAppointments(patient);

        if (scheduledAppointments.isEmpty()) {
            System.out.println("No appointments to cancel.");
            return;
        }

        System.out.println("\nYour Scheduled Appointments:");
        for (int i = 0; i < scheduledAppointments.size(); i++) {
            Appointment apt = scheduledAppointments.get(i);
            System.out.printf("%d. %s with Dr. %s (Status: %s)%n",
                i + 1,
                apt.getDateTime(),
                apt.getDoctor().getName(),
                apt.getStatus());
        }

        System.out.print("\nSelect appointment to cancel (1-" + 
            scheduledAppointments.size() + "): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > scheduledAppointments.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            Appointment selectedAppointment = scheduledAppointments.get(choice - 1);
            System.out.print("Are you sure you want to cancel this appointment? (y/n): ");
            
            if (scanner.nextLine().toLowerCase().equals("y")) {
                if (appointmentController.cancelAppointment(
                    selectedAppointment.getAppointmentId())) {
                    System.out.println("Appointment cancelled successfully!");
                } else {
                    System.out.println("Failed to cancel appointment.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    private void viewScheduledAppointments(Patient patient) {
        List<Appointment> appointments = 
            appointmentController.getScheduledAppointments(patient);

        if (appointments.isEmpty()) {
            System.out.println("No scheduled appointments.");
            return;
        }

        System.out.println("\nYour Scheduled Appointments:");
        appointments.forEach(apt -> {
            System.out.println("\n--------------------------------");
            System.out.printf("Appointment ID: %s%n", apt.getAppointmentId());
            System.out.printf("Doctor: Dr. %s%n", apt.getDoctor().getName());
            System.out.printf("Date/Time: %s%n", apt.getDateTime());
            System.out.printf("Status: %s%n", apt.getStatus());
            
            // Show additional warning for pending appointments
            if (apt.getStatus() == AppointmentStatus.PENDING_APPROVAL) {
                System.out.println("* Awaiting doctor's approval");
            }
        });
    }

    private void viewPastAppointmentRecords(Patient patient) {
        List<AppointmentOutcomeRecord> records = 
            patientController.getPastAppointmentRecords(patient);

        if (records.isEmpty()) {
            System.out.println("No past appointment records found.");
            return;
        }

        System.out.println("\nPast Appointment Records:");
        records.forEach(record -> {
            System.out.println("\n--------------------------------");
            System.out.printf("Date: %s%n", record.getAppointmentDate());
            System.out.printf("Service: %s%n", record.getServiceType());
            
            if (!record.getPrescriptions().isEmpty()) {
                System.out.println("\nPrescriptions:");
                record.getPrescriptions().forEach(prescription -> {
                    System.out.printf("- %s (Quantity: %d) - Status: %s%n",
                        prescription.getMedicine().getName(),
                        prescription.getQuantity(),
                        prescription.getStatus());
                    
                    if (prescription.getDispensedBy() != null) {
                        System.out.printf("  Dispensed by: %s on %s%n",
                            prescription.getDispensedBy().getName(),
                            prescription.getDispensedDateTime());
                    }
                });
            }
            
            if (record.getConsultationNotes() != null && 
                !record.getConsultationNotes().isEmpty()) {
                System.out.println("\nConsultation Notes:");
                System.out.println(record.getConsultationNotes());
            }
        });
    }

    // Helper method to display date selection with validation
    private LocalDate getValidDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String dateStr = scanner.nextLine();
                LocalDate date = LocalDate.parse(dateStr);
                
                if (date.isBefore(LocalDate.now())) {
                    System.out.println("Please enter a future date.");
                    continue;
                }
                
                if (date.isAfter(LocalDate.now().plusMonths(1))) {
                    System.out.println("Cannot schedule appointments more than a month in advance.");
                    continue;
                }
                
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format! Please use YYYY-MM-DD");
            }
        }
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // Helper method to validate phone number format
    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^[0-9]{8,12}$";
        return phone.matches(phoneRegex);
    }

    // Helper method to format date/time for display
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern(
            "dd-MM-yyyy HH:mm"));
    }

    // Helper method to check if appointment is within cancellation period
    private boolean isWithinCancellationPeriod(Appointment appointment) {
        return appointment.getDateTime().isAfter(
            LocalDateTime.now().plusHours(24));
    }

    // Helper method to confirm action
    private boolean confirmAction(String message) {
        System.out.print(message + " (y/n): ");
        return scanner.nextLine().toLowerCase().equals("y");
    }

    // Helper method to handle number input with range validation
    private int getValidNumberInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int input = Integer.parseInt(scanner.nextLine());
                if (input >= min && input <= max) {
                    return input;
                }
                System.out.printf("Please enter a number between %d and %d%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Helper method to display error messages consistently
    private void displayError(String message) {
        System.out.println("\nError: " + message);
        System.out.println("Please try again or contact support if the problem persists.");
    }

    // Helper method to display success messages consistently
    private void displaySuccess(String message) {
        System.out.println("\nSuccess: " + message);
    }
}

