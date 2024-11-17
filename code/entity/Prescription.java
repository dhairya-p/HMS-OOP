// Prescription class
package entity;

import java.time.LocalDateTime;

import entity.enums.*;

public class Prescription {
    private final Medicine medicine; // Change from medicationName to Medicine
    private final int quantity;
    private PrescriptionStatus status;
    private Pharmacist dispensedBy;
    private LocalDateTime dispensedDateTime;
    
    public Prescription(Medicine medicine, int quantity) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.status = PrescriptionStatus.PENDING;
    }
    
    public Medicine getMedicine() {
        return medicine;
    }
    
    public void setDispensedBy(Pharmacist pharmacist) {
        this.dispensedBy = pharmacist;
    }
    
    public void setDispensedDateTime(LocalDateTime dateTime) {
        this.dispensedDateTime = dateTime;
    }
    @Override
    public String toString() {
        return String.format("Medication: %s, Quantity: %d, Status: %s, %s",
            medicine.getName(), quantity, status,
            dispensedBy != null ? "Dispensed by: " + dispensedBy.getName() + 
            " at " + dispensedDateTime : "Not yet dispensed");
    }
    
    public String getMedicineName() { // Changed from getMedicationName
        return medicine.getName();
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public PrescriptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }
    
    // Add getters for new fields
    public Pharmacist getDispensedBy() {
        return dispensedBy;
    }
    
    public LocalDateTime getDispensedDateTime() {
        return dispensedDateTime;
    }
}