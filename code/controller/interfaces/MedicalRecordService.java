package controller.interfaces;

import java.util.List;

import entity.Diagnosis;
import entity.MedicalRecord;

public interface MedicalRecordService {
    MedicalRecord getMedicalRecord(String patientId);
    void updateContactInfo(String patientId, String phone, String email);
    void addDiagnosis(String patientId, String diagnosis, String treatment);
    List<Diagnosis> getDiagnosisHistory(String patientId);
}