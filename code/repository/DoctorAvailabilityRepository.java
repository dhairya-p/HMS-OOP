package repository;

import entity.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DoctorAvailabilityRepository implements Repository<DoctorAvailability, String> {
    private static DoctorAvailabilityRepository instance;
    private final Map<String, DoctorAvailability> availabilities;
    
    private DoctorAvailabilityRepository() {
        this.availabilities = new ConcurrentHashMap<>();
    }
    
    public static DoctorAvailabilityRepository getInstance() {
        if (instance == null) {
            instance = new DoctorAvailabilityRepository();
        }
        return instance;
    }
    
    @Override
    public DoctorAvailability save(DoctorAvailability availability) {
        availabilities.put(availability.getId(), availability);
        return availability;
    }
    
    @Override
    public Optional<DoctorAvailability> findById(String id) {
        return Optional.ofNullable(availabilities.get(id));
    }
    
    @Override
    public List<DoctorAvailability> findAll() {
        return new ArrayList<>(availabilities.values());
    }
    
    @Override
    public void delete(String id) {
        availabilities.remove(id);
    }
    
    @Override
    public boolean exists(String id) {
        return availabilities.containsKey(id);
    }
    
    public Optional<DoctorAvailability> findByDoctorAndDate(Doctor doctor, LocalDate date) {
        return availabilities.values().stream()
            .filter(a -> a.getDoctor().equals(doctor) && a.getDate().equals(date))
            .findFirst();
    }
    
    public List<DoctorAvailability> findByDoctor(Doctor doctor) {
        return availabilities.values().stream()
            .filter(a -> a.getDoctor().equals(doctor))
            .sorted(Comparator.comparing(DoctorAvailability::getDate))
            .toList();
    }
    
    public List<DoctorAvailability> findByDate(LocalDate date) {
        return availabilities.values().stream()
            .filter(a -> a.getDate().equals(date))
            .sorted(Comparator.comparing(a -> a.getDoctor().getName()))
            .toList();
    }

    public void clearAll() {
        availabilities.clear();
    }
}