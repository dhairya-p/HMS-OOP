package controller.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import entity.*;
import entity.enums.*;

public interface AppointmentService {
    List<AppointmentSlot> getAvailableSlots(LocalDate date, Doctor doctor);
    Appointment scheduleAppointment(Patient patient, Doctor doctor, AppointmentSlot slot);
    boolean rescheduleAppointment(String appointmentId, AppointmentSlot newSlot);
    boolean cancelAppointment(String appointmentId);
    List<Appointment> getScheduledAppointments(Patient patient);
    List<Appointment> getUpcomingAppointments(Doctor doctor);
    List<Appointment> getPendingAppointments(Doctor doctor);
    boolean updateAppointmentStatus(String appointmentId, AppointmentStatus status);
    // Add to AppointmentService interface
    List<Appointment> getAllAppointments(Doctor doctor);
    List<Appointment> getAllAppointments(Patient patient);
    List<Appointment> getAllAppointments();
    List<Appointment> getAppointmentsByDate(LocalDate date);
    Optional<Appointment> getAppointmentById(String appointmentId);
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);
    void recordAppointmentOutcome(String appointmentId, String serviceType,
                                List<Prescription> prescriptions, String notes);
}