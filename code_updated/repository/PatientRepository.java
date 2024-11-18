package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import entity.Patient;

public class PatientRepository implements Repository<Patient, String> {
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    
    private static PatientRepository instance;
    
    private PatientRepository() {}
    
    public static PatientRepository getInstance() {
        if (instance == null) {
            instance = new PatientRepository();
        }
        return instance;
    }
    
    @Override
    public Patient save(Patient patient) {
        patients.put(patient.getHospitalId(), patient);
        return patient;
    }
    
    @Override
    public Optional<Patient> findById(String id) {
        return Optional.ofNullable(patients.get(id));
    }
    
    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(patients.values());
    }
    
    @Override
    public void delete(String id) {
        patients.remove(id);
    }
    
    @Override
    public boolean exists(String id) {
        return patients.containsKey(id);
    }

    @Override
    public void clearAll() {
        patients.clear();
    }
}
