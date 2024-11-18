// Doctor class
package entity;

import java.time.LocalDate;
import java.util.*;

public class Doctor extends User {
    private final String specialization;
    private final List<Patient> patients;
    private final Map<LocalDate, DoctorAvailability> availabilities; // Added this

    public Doctor(String hospitalId, String password, String name, String specialization) {
        super(hospitalId, password, name);
        this.specialization = specialization;
        this.patients = new ArrayList<>();
        this.availabilities = new HashMap<>();
    }

    // Added method
    public DoctorAvailability getAvailability(LocalDate date) {
        return availabilities.get(date);
    }
    
    // Added method
    public void setAvailability(DoctorAvailability availability) {
        availabilities.put(availability.getDate(), availability);
    }

    public void addPatient(Patient patient) {
        if (!patients.contains(patient)) {
            patients.add(patient);
        }
    }
    
    public List<Patient> getPatients() {
        return new ArrayList<>(patients); // Return copy to maintain encapsulation
    }
    
    public String getSpecialization() {
        return specialization;
    }

    public Map<LocalDate, DoctorAvailability> getAvailabilities() {
        return availabilities;
    }
}