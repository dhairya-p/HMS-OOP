package system;

import boundary.*;
import controller.*;
import entity.Administrator;
import entity.Doctor;
import entity.Patient;
import entity.Pharmacist;
import entity.User;
import repository.DataImportManager;

import java.nio.file.*;
import java.util.Scanner;

public class HospitalManagementSystem {
    private final Scanner scanner;
    private final LoginUI loginUI;
    private final PatientUI patientUI;
    private final DoctorUI doctorUI;
    private final PharmacistUI pharmacistUI;
    private final AdministratorUI administratorUI;
    private final DataImportManager dataImportManager;
    private final AuthenticationController authController;
    
    public HospitalManagementSystem() {
        this.scanner = new Scanner(System.in);
        
        // Initialize AuthenticationController first
        this.authController = new AuthenticationController();
        
        // Initialize other controllers
        DoctorAvailabilityController availabilityController = new DoctorAvailabilityController();
        AppointmentController appointmentController = new AppointmentController(availabilityController);
        MedicalRecordController medicalRecordController = new MedicalRecordController();
        PatientController patientController = new PatientController(appointmentController);
        
        // Initialize UIs with the same AuthenticationController instance
        this.loginUI = new LoginUI(scanner, authController);
        this.patientUI = new PatientUI(scanner, authController, appointmentController, 
            medicalRecordController, patientController);
        this.doctorUI = new DoctorUI(scanner, authController, appointmentController, 
            medicalRecordController, availabilityController);
        this.pharmacistUI = new PharmacistUI(scanner, authController, appointmentController);
        this.administratorUI = new AdministratorUI(scanner, authController, appointmentController);
        
        // Initialize DataImportManager with the same AuthenticationController instance
        this.dataImportManager = new DataImportManager(authController);
    }
    
    private void loadInitialData() {
        try {
            // Create data directory if it doesn't exist
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            System.out.println("Loading initial data...");
            dataImportManager.importAllData(
                "data/Medicine_List.csv",
                "data/Patient_List.csv",
                "data/Staff_List.csv"
            );
            
        } catch (Exception e) {
            System.err.println("Error loading initial data: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Starting with empty system...");
        }
    }
    
    public void start() {
        System.out.println("Initializing Hospital Management System...");
        loadInitialData();
        
        while (true) {
            User user = loginUI.show();
            if (user == null) {
                break;
            }
            
            SessionController.getInstance().setCurrentUser(user);
            
            switch (user) {
                case Patient patient -> patientUI.show(patient);
                case Doctor doctor -> doctorUI.show(doctor);
                case Pharmacist pharmacist -> pharmacistUI.show(pharmacist);
                case Administrator admin -> administratorUI.show(admin);
                default -> System.out.println("Unknown user type!");
            }
            
            SessionController.getInstance().clearCurrentUser();
        }
        
        scanner.close();
        System.out.println("System shutdown complete. Goodbye!");
    }
    
    public static void main(String[] args) {
        HospitalManagementSystem hms = new HospitalManagementSystem();
        hms.start();
    }
}