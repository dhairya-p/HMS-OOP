package entity;
public class Patient extends User {
    private MedicalRecord medicalRecord; // Remove final modifier
    
    public Patient(String hospitalId, String password, String name, MedicalRecord medicalRecord) {
        super(hospitalId, password, name);
        this.medicalRecord = medicalRecord;
    }
    
    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }
    
    public void setMedicalRecord(MedicalRecord medicalRecord) { // Add setter
        this.medicalRecord = medicalRecord;
    }
}
