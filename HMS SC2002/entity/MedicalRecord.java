// Medical Record class 
package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import entity.enums.*;

public class MedicalRecord {
    private final String patientId;
    private final String name;
    private final LocalDate dateOfBirth;
    private final Gender gender;
    private final BloodType bloodType;
    private final ContactInfo contactInfo;
    private final List<Diagnosis> diagnosisHistory;
    
    public MedicalRecord(String patientId, String name, LocalDate dateOfBirth, 
                        Gender gender, BloodType bloodType, ContactInfo contactInfo) {
        this.patientId = patientId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodType = bloodType;
        this.contactInfo = contactInfo;
        this.diagnosisHistory = new ArrayList<>();
    }
    
    public void addDiagnosis(Diagnosis diagnosis) {
        diagnosisHistory.add(diagnosis);
    }
    
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Patient ID: %s\n", patientId));
        sb.append(String.format("Name: %s\n", name));
        sb.append(String.format("Date of Birth: %s\n", dateOfBirth));
        sb.append(String.format("Gender: %s\n", gender));
        sb.append(String.format("Blood Type: %s\n", bloodType));
        sb.append(String.format("Contact Information:\n%s\n", contactInfo));
        sb.append("\nDiagnosis History:\n");
        diagnosisHistory.forEach(d -> sb.append(d.toString()).append("\n"));
        return sb.toString();
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public List<Diagnosis> getDiagnosisHistory() {
        return diagnosisHistory;
    }

    
}