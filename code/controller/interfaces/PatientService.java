package controller.interfaces;

import entity.*;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    List<Patient> getPatientsUnderCare(Doctor doctor);
    List<AppointmentOutcomeRecord> getPastAppointmentRecords(Patient patient);
    void addPatient(Patient patient);
    Patient getPatient(String patientId);
    List<Patient> getAllPatients();
    boolean existsPatient(String patientId);
    List<Appointment> getUpcomingAppointments(String patientId);
    List<Appointment> getAllAppointments(String patientId);
    boolean hasAnyPendingAppointments(String patientId);
    boolean hasAnyConfirmedAppointments(String patientId);
    Optional<Appointment> getNextAppointment(String patientId);
    boolean canScheduleNewAppointment(String patientId);
    List<Appointment> getAppointmentsWithDoctor(String patientId, Doctor doctor);
    void cancelAllPendingAppointments(String patientId);
    void validatePatient(String patientId);
}