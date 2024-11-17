package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import entity.Doctor;
import entity.Pharmacist;
import entity.User;

public class StaffRepository implements Repository<User, String> {
    private final Map<String, User> staff = new ConcurrentHashMap<>();
    
    private static StaffRepository instance;
    
    private StaffRepository() {}
    
    public static StaffRepository getInstance() {
        if (instance == null) {
            instance = new StaffRepository();
        }
        return instance;
    }
    
    @Override
    public User save(User user) {
        staff.put(user.getHospitalId(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(staff.get(id));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(staff.values());
    }
    
    @Override
    public void delete(String id) {
        staff.remove(id);
    }
    
    @Override
    public boolean exists(String id) {
        return staff.containsKey(id);
    }
    
    // Additional methods specific to staff
    public List<Doctor> findAllDoctors() {
        return staff.values().stream()
            .filter(user -> user instanceof Doctor)
            .map(user -> (Doctor) user)
            .toList();
    }
    
    public List<Pharmacist> findAllPharmacists() {
        return staff.values().stream()
            .filter(user -> user instanceof Pharmacist)
            .map(user -> (Pharmacist) user)
            .toList();
    }
    @Override
    public void clearAll() {
        staff.clear();
    }
}
