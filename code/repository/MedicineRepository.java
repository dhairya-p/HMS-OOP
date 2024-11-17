package repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import entity.Medicine;

public class MedicineRepository implements Repository<Medicine, String> {
    private final Map<String, Medicine> medicines = new ConcurrentHashMap<>();
    
    private static MedicineRepository instance;
    
    private MedicineRepository() {}
    
    public static MedicineRepository getInstance() {
        if (instance == null) {
            instance = new MedicineRepository();
        }
        return instance;
    }
    
    @Override
    public Medicine save(Medicine medicine) {
        medicines.put(medicine.getName(), medicine);
        return medicine;
    }
    
    @Override
    public Optional<Medicine> findById(String name) {
        return Optional.ofNullable(medicines.get(name));
    }
    
    @Override
    public List<Medicine> findAll() {
        return new ArrayList<>(medicines.values());
    }
    
    @Override
    public void delete(String name) {
        medicines.remove(name);
    }
    
    @Override
    public boolean exists(String name) {
        return medicines.containsKey(name);
    }

    public Optional<Medicine> findByName(String name) {
        return findAll().stream()
            .filter(medicine -> medicine.getName().equalsIgnoreCase(name))
            .findFirst();
    }
    
    // Additional methods specific to medicine
    public List<Medicine> findLowStock() {
        return medicines.values().stream()
            .filter(medicine -> medicine.getCurrentStock() <= medicine.getLowStockAlert())
            .toList();
    }
    @Override
    public void clearAll() {
        medicines.clear();
    }
}
