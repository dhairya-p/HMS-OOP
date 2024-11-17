package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentSlot {
    private final String slotId;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Doctor doctor;
    private final LocalDate date;
    private boolean available;  // removed '= true' since it's set in constructor
    private Appointment bookedAppointment;
    
    public AppointmentSlot(String slotId, LocalTime startTime, LocalTime endTime,
                          Doctor doctor, LocalDate date) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.doctor = doctor;
        this.date = date;
        this.available = true;
    }

    public synchronized boolean tryBook(Appointment appointment) {
        if (!available) {
            return false;
        }
        available = false;
        bookedAppointment = appointment;
        return true;
    }
    
    public synchronized void release() {
        available = true;
        bookedAppointment = null;
    }
    
    public String getSlotId() {
        return slotId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isAvailable() {
        return available;  // changed from isAvailable to available
    }

    public void setAvailable(boolean available) {  // changed parameter name
        this.available = available;
    }
    
    public Appointment getBookedAppointment() {
        return bookedAppointment;
    }
}