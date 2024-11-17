package controller;

import controller.interfaces.AuthenticationService;
import entity.*;
import repository.*;
import java.util.Optional;

public class AuthenticationController implements AuthenticationService {
    private final StaffRepository staffRepository;
    private final PatientRepository patientRepository;
    private User currentUser;
    
    public AuthenticationController() {
        this.staffRepository = StaffRepository.getInstance();
        this.patientRepository = PatientRepository.getInstance();
    }

    
    public void clearAllUsers() {
        staffRepository.clearAll();
        patientRepository.clearAll();
        currentUser = null;
    }

    
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Add or update in appropriate repository
        if (user instanceof Patient) {
            patientRepository.save((Patient) user);
        } else {
            staffRepository.save(user);
        }
    }
    @Override
    public User login(String hospitalId, String password) {
        // First check staff repository
        Optional<User> staffUser = staffRepository.findById(hospitalId);
        if (staffUser.isPresent()) {
            User user = staffUser.get();
            if (user.validatePassword(password)) {
                currentUser = user;
                return user;
            }
            return null;
        }
        
        // Then check patient repository
        Optional<Patient> patientUser = patientRepository.findById(hospitalId);
        if (patientUser.isPresent()) {
            Patient patient = patientUser.get();
            if (patient.validatePassword(password)) {
                currentUser = patient;
                return patient;
            }
        }
        
        return null;
    }
    
    @Override
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        // Validate input
        if (user == null || oldPassword == null || newPassword == null) {
            return false;
        }
        
        // Attempt password change
        boolean changed = user.changePassword(oldPassword, newPassword);
        
        // If successful, update in appropriate repository
        if (changed) {
            if (user instanceof Patient) {
                patientRepository.save((Patient) user);
            } else {
                staffRepository.save(user);
            }
        }
        
        return changed;
    }
    
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        currentUser = null;
    }
    
    /**
     * Checks if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public String getUserType() {
        if (currentUser == null) {
            return null;
        }
        
        if (currentUser instanceof Doctor) return "Doctor";
        if (currentUser instanceof Patient) return "Patient";
        if (currentUser instanceof Pharmacist) return "Pharmacist";
        if (currentUser instanceof Administrator) return "Administrator";
        
        return "Unknown";
    }
    
    public boolean userExists(String hospitalId) {
        return staffRepository.exists(hospitalId) || patientRepository.exists(hospitalId);
    }
   
    public boolean removeUser(String hospitalId) {
        if (staffRepository.exists(hospitalId)) {
            staffRepository.delete(hospitalId);
            return true;
        }
        if (patientRepository.exists(hospitalId)) {
            patientRepository.delete(hospitalId);
            return true;
        }
        return false;
    }
    
   
    public Optional<User> getUser(String hospitalId) {
        Optional<User> staffUser = staffRepository.findById(hospitalId);
        if (staffUser.isPresent()) {
            return staffUser;
        }
        return patientRepository.findById(hospitalId).map(patient -> (User) patient);
    }
    
    
    public boolean resetPassword(String hospitalId) {
        Optional<User> user = getUser(hospitalId);
        if (user.isPresent()) {
            User u = user.get();
            u.setPassword("password"); // Default password
            
            if (u instanceof Patient) {
                patientRepository.save((Patient) u);
            } else {
                staffRepository.save(u);
            }
            return true;
        }
        return false;
    }

}