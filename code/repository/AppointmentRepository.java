package repository;

import entity.*;
import entity.enums.AppointmentStatus;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AppointmentRepository implements Repository<Appointment, String> {
    private final Map<String, Appointment> appointments;
    private static AppointmentRepository instance;
    private int nextAppointmentNumber = 1;
    
    private AppointmentRepository() {
        this.appointments = new ConcurrentHashMap<>();
    }
    
    public static AppointmentRepository getInstance() {
        if (instance == null) {
            instance = new AppointmentRepository();
        }
        return instance;
    }
    
    /**
     * Generates a unique appointment ID
     */
    private String generateAppointmentId() {
        String appointmentId;
        do {
            appointmentId = "A" + String.format("%05d", nextAppointmentNumber++);
        } while (appointments.containsKey(appointmentId));
        return appointmentId;
    }
    
    /**
     * Saves an appointment. If the appointment doesn't have an ID, generates one.
     */
    @Override
    public Appointment save(Appointment appointment) {
        if (appointment.getAppointmentId() == null) {
            String newId = generateAppointmentId();
            // Create new appointment with generated ID
            appointment = new Appointment(
                newId,
                appointment.getPatient(),
                appointment.getDoctor(),
                appointment.getDateTime()
            );
        }
        appointments.put(appointment.getAppointmentId(), appointment);
        return appointment;
    }
    
    @Override
    public Optional<Appointment> findById(String id) {
        return Optional.ofNullable(appointments.get(id));
    }
    
    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(appointments.values());
    }
    
    @Override
    public void delete(String id) {
        appointments.remove(id);
    }
    
    @Override
    public boolean exists(String id) {
        return appointments.containsKey(id);
    }
    
    /**
     * Finds all appointments for a specific doctor
     */
    public List<Appointment> findByDoctor(Doctor doctor) {
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all appointments for a specific patient
     */
    public List<Appointment> findByPatient(Patient patient) {
        return appointments.values().stream()
            .filter(apt -> apt.getPatient().equals(patient))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all appointments with a specific status
     */
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointments.values().stream()
            .filter(apt -> apt.getStatus() == status)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all future appointments for a doctor
     */
    public List<Appointment> findFutureAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all future appointments for a patient
     */
    public List<Appointment> findFutureAppointmentsByPatient(Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getPatient().equals(patient))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all past appointments for a doctor
     */
    public List<Appointment> findPastAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().isBefore(now))
            .sorted(Comparator.comparing(Appointment::getDateTime).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all past appointments for a patient
     */
    public List<Appointment> findPastAppointmentsByPatient(Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getPatient().equals(patient))
            .filter(apt -> apt.getDateTime().isBefore(now))
            .sorted(Comparator.comparing(Appointment::getDateTime).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all appointments for a specific date
     */
    public List<Appointment> findByDate(LocalDate date) {
        return appointments.values().stream()
            .filter(apt -> apt.getDateTime().toLocalDate().equals(date))
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds appointments in a date range
     */
    public List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointments.values().stream()
            .filter(apt -> {
                LocalDate aptDate = apt.getDateTime().toLocalDate();
                return !aptDate.isBefore(startDate) && !aptDate.isAfter(endDate);
            })
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all pending appointments for a doctor
     */
    public List<Appointment> findPendingAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.PENDING_APPROVAL)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds all confirmed appointments for a doctor
     */
    public List<Appointment> findConfirmedAppointmentsByDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
            .sorted(Comparator.comparing(Appointment::getDateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the next appointment for a patient
     */
    public Optional<Appointment> findNextAppointmentForPatient(Patient patient) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getPatient().equals(patient))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
            .min(Comparator.comparing(Appointment::getDateTime));
    }
    
    /**
     * Gets the next appointment for a doctor
     */
    public Optional<Appointment> findNextAppointmentForDoctor(Doctor doctor) {
        LocalDateTime now = LocalDateTime.now();
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getDateTime().isAfter(now))
            .filter(apt -> apt.getStatus() == AppointmentStatus.CONFIRMED)
            .min(Comparator.comparing(Appointment::getDateTime));
    }
    
    /**
     * Counts appointments by status for a doctor
     */
    public Map<AppointmentStatus, Long> getAppointmentCountsByStatusForDoctor(Doctor doctor) {
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .collect(Collectors.groupingBy(
                Appointment::getStatus,
                Collectors.counting()
            ));
    }
    
    /**
     * Deletes all cancelled appointments older than the specified date
     */
    public void cleanupOldCancelledAppointments(LocalDate before) {
        appointments.values().removeIf(apt -> 
            apt.getStatus() == AppointmentStatus.CANCELLED &&
            apt.getDateTime().toLocalDate().isBefore(before)
        );
    }
    
    /**
     * Checks if a doctor has any appointments at a specific date and time
     */
    public boolean isDoctorAvailable(Doctor doctor, LocalDateTime dateTime) {
        return appointments.values().stream()
            .filter(apt -> apt.getDoctor().equals(doctor))
            .filter(apt -> apt.getStatus() != AppointmentStatus.CANCELLED)
            .noneMatch(apt -> apt.getDateTime().equals(dateTime));
    }

    @Override
    public void clearAll() {
        appointments.clear();
    }
}