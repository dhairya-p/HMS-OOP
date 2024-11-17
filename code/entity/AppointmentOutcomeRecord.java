// AppointmentOutcomeRecord class
package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentOutcomeRecord {
    private final LocalDate appointmentDate;
    private final String serviceType;
    private final List<Prescription> prescriptions;
    private final String consultationNotes;
    
    public AppointmentOutcomeRecord(LocalDate appointmentDate, String serviceType, 
                                  List<Prescription> prescriptions, String consultationNotes) {
        this.appointmentDate = appointmentDate;
        this.serviceType = serviceType;
        this.prescriptions = prescriptions;
        this.consultationNotes = consultationNotes;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Date: %s\n", appointmentDate));
        sb.append(String.format("Service: %s\n", serviceType));
        sb.append("Prescriptions:\n");
        prescriptions.forEach(p -> sb.append(p.toString()).append("\n"));
        sb.append(String.format("Notes: %s", consultationNotes));
        return sb.toString();
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public String getServiceType() {
        return serviceType;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public String getConsultationNotes() {
        return consultationNotes;
    }
}