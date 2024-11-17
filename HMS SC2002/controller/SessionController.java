package controller;

import entity.*;
import entity.enums.UserRole;
import repository.*;

public class SessionController {
    private static SessionController instance;
    private User currentUser;
    
    // Repositories
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    
    private SessionController() {
        this.patientRepository = PatientRepository.getInstance();
        this.staffRepository = StaffRepository.getInstance();
        this.appointmentRepository = AppointmentRepository.getInstance();
        this.medicineRepository = MedicineRepository.getInstance();
    }
    
    public static SessionController getInstance() {
        if (instance == null) {
            instance = new SessionController();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        this.currentUser = user;
    }
    
    public void clearCurrentUser() {
        this.currentUser = null;
    }
    
    public boolean login(String hospitalId, String password) {
        if (hospitalId == null || password == null) {
            return false;
        }
        
        // First check staff repository
        User user = staffRepository.findById(hospitalId)
            .orElseGet(() -> patientRepository.findById(hospitalId).orElse(null));
            
        if (user != null && user.validatePassword(password)) {
            setCurrentUser(user);
            return true;
        }
        return false;
    }
    
    public void logout() {
        clearCurrentUser();
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public UserRole getCurrentUserRole() {
        if (currentUser == null) {
            return null;
        }
        
        if (currentUser instanceof Doctor) return UserRole.DOCTOR;
        if (currentUser instanceof Patient) return UserRole.PATIENT;
        if (currentUser instanceof Pharmacist) return UserRole.PHARMACIST;
        if (currentUser instanceof Administrator) return UserRole.ADMINISTRATOR;
        return null;
    }
    
    // Additional utility methods
    
    public boolean isUserAuthorized(UserRole requiredRole) {
        UserRole currentRole = getCurrentUserRole();
        return currentRole != null && currentRole == requiredRole;
    }
    
    public boolean validateSession() {
        if (!isLoggedIn()) {
            return false;
        }
        
        // Verify user still exists in repository
        String id = currentUser.getHospitalId();
        if (currentUser instanceof Patient) {
            return patientRepository.exists(id);
        } else {
            return staffRepository.exists(id);
        }
    }
    
    public void refreshCurrentUser() {
        if (currentUser == null) {
            return;
        }
        
        String id = currentUser.getHospitalId();
        User refreshedUser;
        
        if (currentUser instanceof Patient) {
            refreshedUser = patientRepository.findById(id).orElse(null);
        } else {
            refreshedUser = staffRepository.findById(id).orElse(null);
        }
        
        if (refreshedUser != null) {
            setCurrentUser(refreshedUser);
        } else {
            clearCurrentUser();
        }
    }
}
