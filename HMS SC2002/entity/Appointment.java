// Appointment class
package entity;

import java.time.LocalDateTime;
import entity.enums.AppointmentStatus;

public class Appointment {
    private final String appointmentId;
    private final Patient patient;
    private final Doctor doctor;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private AppointmentOutcomeRecord outcomeRecord;
    
    public Appointment(String appointmentId, Patient patient, Doctor doctor, 
                      LocalDateTime dateTime) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = AppointmentStatus.PENDING_APPROVAL;
    }
    
    // Added method
    public void setDateTime(LocalDateTime newDateTime) {
        this.dateTime = newDateTime;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("Appointment ID: %s\nPatient: %s\nDoctor: %s\nDate/Time: %s\nStatus: %s",
            appointmentId, patient.getName(), doctor.getName(), dateTime, status);
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public AppointmentOutcomeRecord getOutcomeRecord() {
        return outcomeRecord;
    }

    public void setOutcomeRecord(AppointmentOutcomeRecord record) {
        this.outcomeRecord = record;
    }
    
    // Add a method to validate the outcome record
    public boolean hasValidOutcomeRecord() {
        return this.outcomeRecord != null && 
               this.status == AppointmentStatus.COMPLETED;
    }

}