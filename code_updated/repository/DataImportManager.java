package repository;

import service.*;
import entity.*;
import controller.AuthenticationController;
import java.io.IOException;
import java.util.List;

public class DataImportManager {
    private final MedicineImportService medicineImportService;
    private final PatientImportService patientImportService;
    private final StaffImportService staffImportService;
    private final AuthenticationController authController;
    
    public DataImportManager(AuthenticationController authController) {
        this.medicineImportService = new MedicineImportService();
        this.patientImportService = new PatientImportService();
        this.staffImportService = new StaffImportService();
        this.authController = authController;
    }
    
    public void clearAllData() {
        System.out.println("Clearing existing data...");
        MedicineRepository.getInstance().clearAll();
        PatientRepository.getInstance().clearAll();
        StaffRepository.getInstance().clearAll();
        MedicalRecordRepository.getInstance().clearAll();
        authController.clearAllUsers();
        System.out.println("All data cleared successfully.");
    }
    
    public void importAllData(String medicineFile, String patientFile, String staffFile) 
            throws IOException {
        // Clear existing data first
        clearAllData();
        
        System.out.println("Starting data import...");
        
        // Import medicines first
        try {
            List<Medicine> medicines = medicineImportService.importData(medicineFile);
            MedicineRepository medicineRepo = MedicineRepository.getInstance();
            for (Medicine medicine : medicines) {
                medicineRepo.save(medicine);
            }
            System.out.println("Imported " + medicines.size() + " medicines");
        } catch (Exception e) {
            System.err.println("Error importing medicines: " + e.getMessage());
        }
        
        // Import staff next
        try {
            List<User> staff = staffImportService.importData(staffFile);
            StaffRepository staffRepo = StaffRepository.getInstance();
            for (User staffMember : staff) {
                staffRepo.save(staffMember);
                authController.addUser(staffMember);
            }
            System.out.println("Imported " + staff.size() + " staff members");
        } catch (Exception e) {
            System.err.println("Error importing staff: " + e.getMessage());
        }
        
        // Import patients last
        try {
            List<Patient> patients = patientImportService.importData(patientFile);
            PatientRepository patientRepo = PatientRepository.getInstance();
            MedicalRecordRepository medicalRecordRepo = MedicalRecordRepository.getInstance();
            for (Patient patient : patients) {
                patientRepo.save(patient);
                medicalRecordRepo.save(patient.getMedicalRecord());
                authController.addUser(patient);
            }
            System.out.println("Imported " + patients.size() + " patients");
        } catch (Exception e) {
            System.err.println("Error importing patients: " + e.getMessage());
        }
        
        System.out.println("Data import completed.");
    }
}