package controller;

import java.util.*;

import controller.interfaces.MedicalRecordService;
import entity.*;
import entity.enums.BloodType;
import repository.MedicalRecordRepository;
import repository.PatientRepository;

public class MedicalRecordController implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    
    public MedicalRecordController() {
        this.medicalRecordRepository = MedicalRecordRepository.getInstance();
        this.patientRepository = PatientRepository.getInstance();
    }
    
    @Override
    public MedicalRecord getMedicalRecord(String patientId) {
        return medicalRecordRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Medical record not found for patient: " + patientId));
    }
    
    @Override
    public void updateContactInfo(String patientId, String phone, String email) {
        // Validate input
        if (phone == null || email == null) {
            throw new IllegalArgumentException("Phone and email cannot be null");
        }
        
        if (phone.trim().isEmpty() || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone and email cannot be empty");
        }
        
        // Get and update medical record
        MedicalRecord record = getMedicalRecord(patientId);
        record.getContactInfo().updateContactInfo(phone, email);
        medicalRecordRepository.save(record);
        
        // Update patient's medical record reference as well
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            Patient updatedPatient = patient.get();
            updatedPatient.setMedicalRecord(record);
            patientRepository.save(updatedPatient);
        }
    }

    @Override
    public void addDiagnosis(String patientId, String diagnosis, String treatment) {
        // Validate input
        if (diagnosis == null || treatment == null) {
            throw new IllegalArgumentException("Diagnosis and treatment cannot be null");
        }
        
        if (diagnosis.trim().isEmpty() || treatment.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis and treatment cannot be empty");
        }
        
        MedicalRecord record = getMedicalRecord(patientId);
        
        // Create new Treatment and Diagnosis objects
        Treatment newTreatment = new Treatment(treatment);
        Diagnosis newDiagnosis = new Diagnosis(diagnosis, newTreatment);
        
        // Add to patient's medical record
        record.addDiagnosis(newDiagnosis);
        
        // Save updated record
        medicalRecordRepository.save(record);
        
        // Update patient's medical record reference
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            Patient updatedPatient = patient.get();
            updatedPatient.setMedicalRecord(record);
            patientRepository.save(updatedPatient);
        }
    }
    
    @Override
    public List<Diagnosis> getDiagnosisHistory(String patientId) {
        MedicalRecord record = getMedicalRecord(patientId);
        return new ArrayList<>(record.getDiagnosisHistory());
    }
    
    /**
     * Adds a new medical record to the system
     * @param patientId The ID of the patient
     * @param record The medical record to add
     * @throws IllegalArgumentException if record already exists
     */
    public void addMedicalRecord(String patientId, MedicalRecord record) {
        if (medicalRecordRepository.exists(patientId)) {
            throw new IllegalArgumentException("Medical record already exists for patient: " + patientId);
        }
        
        medicalRecordRepository.save(record);
        
        // Update patient's medical record reference
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            Patient updatedPatient = patient.get();
            updatedPatient.setMedicalRecord(record);
            patientRepository.save(updatedPatient);
        }
    }
    
    /**
     * Gets all medical records in the system
     * @return List of all medical records
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }
    
    /**
     * Checks if a medical record exists for a patient
     * @param patientId The patient ID to check
     * @return true if medical record exists, false otherwise
     */
    public boolean hasMedicalRecord(String patientId) {
        return medicalRecordRepository.exists(patientId);
    }
    
    /**
     * Gets medical records by blood type
     * @param bloodType The blood type to search for
     * @return List of medical records matching the blood type
     */
    public List<MedicalRecord> getMedicalRecordsByBloodType(BloodType bloodType) {
        return medicalRecordRepository.findAll().stream()
            .filter(record -> record.getBloodType() == bloodType)
            .toList();
    }
    
    /**
     * Searches medical records for specific diagnosis
     * @param diagnosisKeyword Keyword to search for in diagnoses
     * @return List of medical records containing the diagnosis keyword
     */
    public List<MedicalRecord> searchMedicalRecordsByDiagnosis(String diagnosisKeyword) {
        if (diagnosisKeyword == null || diagnosisKeyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }
        
        String keyword = diagnosisKeyword.toLowerCase().trim();
        
        return medicalRecordRepository.findAll().stream()
            .filter(record -> record.getDiagnosisHistory().stream()
                .anyMatch(diagnosis -> 
                    diagnosis.getDescription().toLowerCase().contains(keyword)))
            .toList();
    }
    
    
    
    /**
     * Removes a medical record from the system
     * @param patientId The ID of the patient whose record to remove
     * @return true if record was removed, false if it didn't exist
     */
    public boolean removeMedicalRecord(String patientId) {
        if (!medicalRecordRepository.exists(patientId)) {
            return false;
        }
        
        medicalRecordRepository.delete(patientId);
        
        // Update patient reference
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            Patient updatedPatient = patient.get();
            updatedPatient.setMedicalRecord(null);
            patientRepository.save(updatedPatient);
        }
        
        return true;
    }
}