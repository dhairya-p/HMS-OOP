package controller;

import entity.*;
import entity.enums.AppointmentStatus;
import repository.PatientRepository;
import java.util.*;
import controller.interfaces.*;

public class PatientController implements PatientService {
    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    
    public PatientController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
        this.patientRepository = PatientRepository.getInstance();
    }

    // Update these methods to use patientRepository instead of patients Map
    @Override
    public void addPatient(Patient patient) {
        if (patientRepository.exists(patient.getHospitalId())) {
            throw new IllegalArgumentException("Patient with ID " + 
                patient.getHospitalId() + " already exists");
        }
        patientRepository.save(patient);
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public boolean existsPatient(String patientId) {
        return patientRepository.exists(patientId);
    }

    // Rest of the methods remain the same since they already use patientRepository
    @Override
    public Patient getPatient(String patientId) {
        return patientRepository.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));
    }

    @Override
    public void validatePatient(String patientId) {
        if (!patientRepository.exists(patientId)) {
            throw new IllegalArgumentException("Patient not found with ID: " + patientId);
        }
    }

    @Override
    public List<Patient> getPatientsUnderCare(Doctor doctor) {
        // Get all confirmed and upcoming appointments for the doctor
        List<Appointment> doctorAppointments = appointmentService.getUpcomingAppointments(doctor);
        
        // Add all completed appointments to get full patient history
        List<Appointment> allAppointments = appointmentService.getAllAppointments(doctor);
        
        // Create a set of unique patients from all appointments
        Set<Patient> uniquePatients = new HashSet<>();
        
        // Add patients from upcoming appointments
        for (Appointment apt : doctorAppointments) {
            uniquePatients.add(apt.getPatient());
        }
        
        // Add patients from historical appointments
        for (Appointment apt : allAppointments) {
            if (apt.getStatus() == AppointmentStatus.COMPLETED) {
                uniquePatients.add(apt.getPatient());
            }
        }
        
        // Convert to list and sort by patient name
        List<Patient> patientList = new ArrayList<>(uniquePatients);
        patientList.sort(Comparator.comparing(Patient::getName));
        
        return patientList;
    }

    @Override
    public List<AppointmentOutcomeRecord> getPastAppointmentRecords(Patient patient) {
        List<AppointmentOutcomeRecord> records = new ArrayList<>();
        
        List<Appointment> allAppointments = appointmentService.getAllAppointments(patient);
        
        for (Appointment apt : allAppointments) {
            if (apt.getStatus() == AppointmentStatus.COMPLETED && 
                apt.getOutcomeRecord() != null) {
                records.add(apt.getOutcomeRecord());
            }
        }
        
        records.sort((r1, r2) -> r2.getAppointmentDate().compareTo(r1.getAppointmentDate()));
        return records;
    }

    public List<Appointment> getUpcomingAppointments(String patientId) {
        validatePatient(patientId);
        Patient patient = getPatient(patientId);
        return appointmentService.getScheduledAppointments(patient);
    }

    public List<Appointment> getAllAppointments(String patientId) {
        validatePatient(patientId);
        Patient patient = getPatient(patientId);
        return appointmentService.getAllAppointments(patient);
    }

    public boolean hasAnyPendingAppointments(String patientId) {
        validatePatient(patientId);
        return getUpcomingAppointments(patientId).stream()
            .anyMatch(apt -> apt.getStatus() == AppointmentStatus.PENDING_APPROVAL);
    }

    public boolean hasAnyConfirmedAppointments(String patientId) {
        validatePatient(patientId);
        return getUpcomingAppointments(patientId).stream()
            .anyMatch(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED);
    }

    public Optional<Appointment> getNextAppointment(String patientId) {
        validatePatient(patientId);
        return getUpcomingAppointments(patientId).stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
            .min(Comparator.comparing(Appointment::getDateTime));
    }

    @Override
    public boolean canScheduleNewAppointment(String patientId) {
        try {
            validatePatient(patientId);
            List<Appointment> upcomingAppointments = getUpcomingAppointments(patientId);
            
            long pendingAndConfirmed = upcomingAppointments.stream()
                .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING_APPROVAL || 
                             apt.getStatus() == AppointmentStatus.CONFIRMED)
                .count();
            
            return pendingAndConfirmed < 3;
        } catch (IllegalArgumentException e) {
            System.out.println("Error checking appointment eligibility: " + e.getMessage());
            return false;
        }
    }

    public List<Appointment> getAppointmentsWithDoctor(String patientId, Doctor doctor) {
        validatePatient(patientId);
        return getAllAppointments(patientId).stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }

    public void cancelAllPendingAppointments(String patientId) {
        validatePatient(patientId);
        getUpcomingAppointments(patientId).stream()
            .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING_APPROVAL)
            .forEach(apt -> appointmentService.cancelAppointment(apt.getAppointmentId()));
    }
}
