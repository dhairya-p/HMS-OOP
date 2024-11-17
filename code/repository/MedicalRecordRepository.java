package repository;

import entity.*;
import entity.enums.BloodType;
import entity.enums.Gender;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MedicalRecordRepository implements Repository<MedicalRecord, String> {
    private final Map<String, MedicalRecord> records;
    private static MedicalRecordRepository instance;
    
    private MedicalRecordRepository() {
        this.records = new ConcurrentHashMap<>();
    }
    
    public static MedicalRecordRepository getInstance() {
        if (instance == null) {
            instance = new MedicalRecordRepository();
        }
        return instance;
    }
    
    @Override
    public MedicalRecord save(MedicalRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Medical record cannot be null");
        }
        records.put(record.getPatientId(), record);
        return record;
    }
    
    @Override
    public Optional<MedicalRecord> findById(String patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        return Optional.ofNullable(records.get(patientId));
    }
    
    @Override
    public List<MedicalRecord> findAll() {
        return new ArrayList<>(records.values());
    }
    
    @Override
    public void delete(String patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        records.remove(patientId);
    }
    
    @Override
    public boolean exists(String patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        return records.containsKey(patientId);
    }
    
    /**
     * Finds medical records by gender
     */
    public List<MedicalRecord> findByGender(Gender gender) {
        return records.values().stream()
            .filter(record -> record.getGender() == gender)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records by blood type
     */
    public List<MedicalRecord> findByBloodType(BloodType bloodType) {
        return records.values().stream()
            .filter(record -> record.getBloodType() == bloodType)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records by age range
     */
    public List<MedicalRecord> findByAgeRange(int minAge, int maxAge) {
        if (minAge < 0 || maxAge < minAge) {
            throw new IllegalArgumentException("Invalid age range");
        }
        
        return records.values().stream()
            .filter(record -> {
                int age = Period.between(record.getDateOfBirth(), LocalDate.now()).getYears();
                return age >= minAge && age <= maxAge;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records containing specific diagnosis
     */
    public List<MedicalRecord> findByDiagnosisKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }
        
        String lowercaseKeyword = keyword.toLowerCase().trim();
        return records.values().stream()
            .filter(record -> record.getDiagnosisHistory().stream()
                .anyMatch(diagnosis -> 
                    diagnosis.getDescription().toLowerCase().contains(lowercaseKeyword)))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records by diagnosis count range
     */
    public List<MedicalRecord> findByDiagnosisCountRange(int minCount, int maxCount) {
        if (minCount < 0 || maxCount < minCount) {
            throw new IllegalArgumentException("Invalid count range");
        }
        
        return records.values().stream()
            .filter(record -> {
                int diagnosisCount = record.getDiagnosisHistory().size();
                return diagnosisCount >= minCount && diagnosisCount <= maxCount;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records with diagnoses in date range
     */
    public List<MedicalRecord> findByDiagnosisDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }
        
        return records.values().stream()
            .filter(record -> record.getDiagnosisHistory().stream()
                .anyMatch(diagnosis -> {
                    LocalDate diagnosisDate = diagnosis.getDate();
                    return !diagnosisDate.isBefore(startDate) && !diagnosisDate.isAfter(endDate);
                }))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets recently updated medical records
     */
    public List<MedicalRecord> findRecentlyUpdated(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        
        return records.values().stream()
            .filter(record -> record.getDiagnosisHistory().stream()
                .anyMatch(diagnosis -> !diagnosis.getDate().isBefore(cutoffDate)))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds medical records by contact information
     */
    public List<MedicalRecord> findByContactInfo(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        String term = searchTerm.toLowerCase().trim();
        return records.values().stream()
            .filter(record -> {
                ContactInfo contactInfo = record.getContactInfo();
                return contactInfo.getEmail().toLowerCase().contains(term) ||
                       contactInfo.getPhoneNumber().contains(term);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Saves multiple medical records in batch
     */
    public void saveAll(List<MedicalRecord> medicalRecords) {
        if (medicalRecords == null) {
            throw new IllegalArgumentException("Medical records list cannot be null");
        }
        
        medicalRecords.forEach(this::save);
    }
    
    /**
     * Deletes multiple medical records in batch
     */
    public void deleteAll(List<String> patientIds) {
        if (patientIds == null) {
            throw new IllegalArgumentException("Patient IDs list cannot be null");
        }
        
        patientIds.forEach(this::delete);
    }
    
    /**
     * Gets total count of medical records
     */
    public int getTotalCount() {
        return records.size();
    }
    
    /**
     * Gets medical records statistics
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        // Count by gender
        Map<Gender, Long> genderStats = records.values().stream()
            .collect(Collectors.groupingBy(MedicalRecord::getGender, Collectors.counting()));
        genderStats.forEach((gender, count) -> stats.put("gender_" + gender, count));
        
        // Count by blood type
        Map<BloodType, Long> bloodTypeStats = records.values().stream()
            .collect(Collectors.groupingBy(MedicalRecord::getBloodType, Collectors.counting()));
        bloodTypeStats.forEach((bloodType, count) -> stats.put("bloodType_" + bloodType, count));
        
        // Average age
        double avgAge = records.values().stream()
            .mapToInt(record -> Period.between(record.getDateOfBirth(), LocalDate.now()).getYears())
            .average()
            .orElse(0.0);
        stats.put("average_age", Math.round(avgAge));
        
        return stats;
    }
    @Override
    public void clearAll() {
        records.clear();
    }
}