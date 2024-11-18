package controller.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import entity.AppointmentSlot;
import entity.Doctor;
import entity.DoctorAvailability;

public interface DoctorAvailabilityService {
    void setAvailability(Doctor doctor, LocalDate date, 
                        LocalTime startTime, LocalTime endTime);
    List<AppointmentSlot> generateSlots(DoctorAvailability availability);
    List<Doctor> getAvailableDoctors(LocalDate date);
    AppointmentSlot getSlotByDateTime(Doctor doctor, LocalDate date, LocalTime time);
}