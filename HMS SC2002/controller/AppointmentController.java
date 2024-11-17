package controller;

import java.time.*;
import java.util.*;

import controller.interfaces.*;
import entity.*;
import entity.enums.AppointmentStatus;
import entity.enums.PrescriptionStatus;
import repository.AppointmentRepository;

public class AppointmentController implements AppointmentService {
    private final DoctorAvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepository;
    
    public AppointmentController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
        this.appointmentRepository = AppointmentRepository.getInstance();
    }

    public DoctorAvailabilityService getAvailabilityService() {
        return availabilityService;
    }
    
    @Override
    public List<AppointmentSlot> getAvailableSlots(LocalDate date, Doctor doctor) {
        DoctorAvailability availability = doctor.getAvailability(date);
        List<AppointmentSlot> slots = availabilityService.generateSlots(availability);
        
        // Get all appointments for this doctor on this date
        List<Appointment> existingAppointments = appointmentRepository.findAll().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().toLocalDate().equals(date))
            .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
            .toList();
        
        // Filter out slots that are already booked
        return slots.stream()
            .filter(slot -> {
                LocalDateTime slotDateTime = slot.getDate().atTime(slot.getStartTime());
                return existingAppointments.stream()
                    .noneMatch(apt -> apt.getDateTime().equals(slotDateTime));
            })
            .toList();
    }
    
    @Override
    public Appointment scheduleAppointment(Patient patient, Doctor doctor, AppointmentSlot slot) {
        if (!slot.isAvailable()) {
            return null;
        }
        
        Appointment appointment = new Appointment(null, patient, doctor, 
            slot.getDate().atTime(slot.getStartTime()));
            
        if (!slot.tryBook(appointment)) {
            return null; // Slot was taken before we could book it
        }
        
        return appointmentRepository.save(appointment);
    }
    
    @Override
    public boolean rescheduleAppointment(String appointmentId, AppointmentSlot newSlot) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return false;
        }
        
        Appointment appointment = optionalAppointment.get();
        
        // Validate appointment can be rescheduled
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return false;
        }
        
        // Check if new slot is available
        if (!newSlot.isAvailable()) {
            return false;
        }
        
        // Make the old slot available again
        AppointmentSlot oldSlot = availabilityService.getSlotByDateTime(
            appointment.getDoctor(), 
            appointment.getDateTime().toLocalDate(),
            appointment.getDateTime().toLocalTime()
        );
        if (oldSlot != null) {
            oldSlot.setAvailable(true);
        }
        
        // Update appointment with new datetime and reset status to pending
        appointment.setDateTime(newSlot.getDate().atTime(newSlot.getStartTime()));
        appointment.setStatus(AppointmentStatus.PENDING_APPROVAL);
        newSlot.setAvailable(false);
        
        appointmentRepository.save(appointment);
        return true;
    }
    
    @Override
    public boolean cancelAppointment(String appointmentId) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return false;
        }
        
        Appointment appointment = optionalAppointment.get();
        
        // Validate appointment can be cancelled
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || 
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return false;
        }
        
        // Make the slot available again
        AppointmentSlot slot = availabilityService.getSlotByDateTime(
            appointment.getDoctor(), 
            appointment.getDateTime().toLocalDate(),
            appointment.getDateTime().toLocalTime()
        );
        if (slot != null) {
            slot.setAvailable(true);
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return true;
    }
    
    @Override
    public List<Appointment> getScheduledAppointments(Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        
        return appointmentRepository.findByPatient(patient).stream()
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
    
    @Override
    public List<Appointment> getUpcomingAppointments(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        
        return appointmentRepository.findByDoctor(doctor).stream()
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED || 
                        apt.getStatus() == AppointmentStatus.PENDING_APPROVAL)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
    
    @Override
    public List<Appointment> getPendingAppointments(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        
        return appointmentRepository.findByDoctor(doctor).stream()
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING_APPROVAL)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
    
    @Override
    public boolean updateAppointmentStatus(String appointmentId, AppointmentStatus status) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            return false;
        }
        
        Appointment appointment = optionalAppointment.get();
        AppointmentStatus currentStatus = appointment.getStatus();
        
        // Check valid status transitions
        if (!isValidStatusTransition(currentStatus, status)) {
            return false;
        }
        
        // If cancelling, make the slot available again
        if (status == AppointmentStatus.CANCELLED) {
            AppointmentSlot slot = availabilityService.getSlotByDateTime(
                appointment.getDoctor(), 
                appointment.getDateTime().toLocalDate(),
                appointment.getDateTime().toLocalTime()
            );
            if (slot != null) {
                slot.setAvailable(true);
            }
        }
        
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        return true;
    }
    
    @Override
    public void recordAppointmentOutcome(String appointmentId, String serviceType,
                                    List<Prescription> prescriptions, String notes) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty() || 
            optionalAppointment.get().getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot record outcome for non-confirmed appointment");
        }
        
        Appointment appointment = optionalAppointment.get();
        
        // Initialize prescriptions with PENDING status
        prescriptions.forEach(p -> p.setStatus(PrescriptionStatus.PENDING));
        
        AppointmentOutcomeRecord outcome = new AppointmentOutcomeRecord(
            appointment.getDateTime().toLocalDate(),
            serviceType,
            new ArrayList<>(prescriptions),
            notes
        );
        
        appointment.setOutcomeRecord(outcome);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        
        appointmentRepository.save(appointment);
    }
    
    @Override
    public List<Appointment> getAllAppointments(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor).stream()
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
    
    @Override
    public List<Appointment> getAllAppointments(Patient patient) {
        return appointmentRepository.findByPatient(patient).stream()
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
    
    private boolean isValidStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        return switch (current) {
            case PENDING_APPROVAL -> 
                next == AppointmentStatus.CONFIRMED || 
                next == AppointmentStatus.CANCELLED;
            case CONFIRMED -> 
                next == AppointmentStatus.COMPLETED || 
                next == AppointmentStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll().stream()
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findAll().stream()
            .filter(apt -> apt.getDateTime().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }

    @Override
    public Optional<Appointment> getAppointmentById(String appointmentId) {
        return appointmentRepository.findById(appointmentId);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findAll().stream()
            .filter(apt -> apt.getStatus() == status)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .toList();
    }
}