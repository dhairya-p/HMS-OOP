package service;

import entity.*;
import entity.enums.*;
import util.CSVReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class PatientImportService implements DataImportService<Patient> {
    @Override
    public List<Patient> importData(String filename) throws IOException {
        CSVReader reader = new CSVReader(filename);
        List<Patient> patients = new ArrayList<>();

        for (String[] row : reader.getData()) {
            String patientId = row[0].trim();
            String name = row[1].trim();
            LocalDate dob = LocalDate.parse(row[2].trim());
            Gender gender = Gender.valueOf(row[3].trim().toUpperCase());
            BloodType bloodType = parseBloodType(row[4].trim());
            String email = row[5].trim();
            String phone = row.length > 6 ? row[6].trim() : "Not Provided";

            // Create contact info
            ContactInfo contactInfo = new ContactInfo(phone, email);
            
            // Create medical record
            MedicalRecord medicalRecord = new MedicalRecord(
                patientId, name, dob, gender, bloodType, contactInfo);

            // Create patient with default password
            Patient patient = new Patient(patientId, "password", name, medicalRecord);
            patients.add(patient);
        }

        return patients;
    }

    private BloodType parseBloodType(String bloodType) {
        // Remove any spaces and convert to uppercase
        String cleanType = bloodType.replace(" ", "").replace("+", "_POSITIVE").replace("-", "_NEGATIVE").toUpperCase();
        
        return switch (cleanType) {
            case "A_POSITIVE", "APOSITIVE" -> BloodType.A_POSITIVE;
            case "A_NEGATIVE", "ANEGATIVE" -> BloodType.A_NEGATIVE;
            case "B_POSITIVE", "BPOSITIVE" -> BloodType.B_POSITIVE;
            case "B_NEGATIVE", "BNEGATIVE" -> BloodType.B_NEGATIVE;
            case "O_POSITIVE", "OPOSITIVE" -> BloodType.O_POSITIVE;
            case "O_NEGATIVE", "ONEGATIVE" -> BloodType.O_NEGATIVE;
            case "AB_POSITIVE", "ABPOSITIVE" -> BloodType.AB_POSITIVE;
            case "AB_NEGATIVE", "ABNEGATIVE" -> BloodType.AB_NEGATIVE;
            default -> throw new IllegalArgumentException("Invalid blood type format: " + bloodType + 
                ". Expected format: A+, A-, B+, B-, O+, O-, AB+, or AB-");
        };
    }
}
