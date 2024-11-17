package entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class DoctorAvailability {
    private final String id;
    private final Doctor doctor;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public DoctorAvailability(Doctor doctor, LocalDate date, 
                            LocalTime startTime, LocalTime endTime) {
        this.id = UUID.randomUUID().toString(); // Generate unique ID
        this.doctor = doctor;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() { // Add getId method
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}