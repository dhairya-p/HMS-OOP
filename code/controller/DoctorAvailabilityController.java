package controller;

import java.time.*;
import java.util.*;

import controller.interfaces.DoctorAvailabilityService;
import entity.*;
import repository.DoctorAvailabilityRepository;
import repository.StaffRepository;

public class DoctorAvailabilityController implements DoctorAvailabilityService {
    private final DoctorAvailabilityRepository availabilityRepository;
    private final StaffRepository staffRepository;
    private final int SLOT_DURATION_MINUTES = 30;
    
    public DoctorAvailabilityController() {
        this.availabilityRepository = DoctorAvailabilityRepository.getInstance();
        this.staffRepository = StaffRepository.getInstance();
    }
    
    @Override
    public List<Doctor> getAvailableDoctors(LocalDate date) {
        // Get all doctors first
        List<Doctor> allDoctors = staffRepository.findAllDoctors();
        
        // Filter doctors who have availability on the given date
        return allDoctors.stream()
            .filter(doctor -> {
                Optional<DoctorAvailability> availability = 
                    availabilityRepository.findByDoctorAndDate(doctor, date);
                    
                return availability.map(avail -> 
                    // For current date, check if end time hasn't passed
                    date.equals(LocalDate.now()) ? 
                        avail.getEndTime().isAfter(LocalTime.now()) : true
                ).orElse(false);
            })
            .sorted(Comparator.comparing(Doctor::getName))
            .toList();
    }

    @Override
    public void setAvailability(Doctor doctor, LocalDate date, 
                              LocalTime startTime, LocalTime endTime) {
        // Validate inputs
        if (doctor == null || date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot set availability for past dates");
        }
        
        // If setting availability for today, validate times
        if (date.equals(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Cannot set availability starting in the past");
        }
        
        // Create and save availability
        DoctorAvailability availability = new DoctorAvailability(doctor, date, startTime, endTime);
        availabilityRepository.save(availability);
        
        // Update doctor's own record
        doctor.setAvailability(availability);
        staffRepository.save(doctor);
    }
    
    @Override
    public List<AppointmentSlot> generateSlots(DoctorAvailability availability) {
        // Return empty list if availability is null
        if (availability == null) {
            return new ArrayList<>();
        }
        
        List<AppointmentSlot> slots = new ArrayList<>();
        LocalTime currentTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        
        // For current date, don't generate slots for times that have already passed
        if (availability.getDate().equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            if (currentTime.isBefore(now)) {
                // Round up to next slot
                currentTime = now.plusMinutes(SLOT_DURATION_MINUTES - 
                    (now.getMinute() % SLOT_DURATION_MINUTES));
            }
        }
        
        // Generate slots
        while (currentTime.plusMinutes(SLOT_DURATION_MINUTES).isBefore(endTime) || 
               currentTime.plusMinutes(SLOT_DURATION_MINUTES).equals(endTime)) {
            
            AppointmentSlot slot = new AppointmentSlot(
                UUID.randomUUID().toString(),
                currentTime,
                currentTime.plusMinutes(SLOT_DURATION_MINUTES),
                availability.getDoctor(),
                availability.getDate()
            );
            
            slots.add(slot);
            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
        }
        
        return slots;
    }
    
    @Override
    public AppointmentSlot getSlotByDateTime(Doctor doctor, LocalDate date, LocalTime time) {
        // Get doctor's availability for the date
        Optional<DoctorAvailability> availability = 
            availabilityRepository.findByDoctorAndDate(doctor, date);
            
        if (availability.isEmpty()) {
            return null;
        }
        
        DoctorAvailability avail = availability.get();
        
        // Check if time falls within availability
        if (time.isBefore(avail.getStartTime()) || 
            time.isAfter(avail.getEndTime().minusMinutes(SLOT_DURATION_MINUTES))) {
            return null;
        }
        
        // Generate and find matching slot
        return generateSlots(avail).stream()
            .filter(slot -> slot.getStartTime().equals(time))
            .findFirst()
            .orElse(null);
    }
    
    // Additional utility methods
    
    /**
     * Gets all availabilities for a doctor
     */
    public List<DoctorAvailability> getDoctorAvailabilities(Doctor doctor) {
        return availabilityRepository.findByDoctor(doctor);
    }
    
    /**
     * Gets all availabilities for a date
     */
    public List<DoctorAvailability> getAvailabilitiesByDate(LocalDate date) {
        return availabilityRepository.findByDate(date);
    }
    
    /**
     * Removes availability for a doctor on a specific date
     */
    public boolean removeAvailability(Doctor doctor, LocalDate date) {
        Optional<DoctorAvailability> availability = 
            availabilityRepository.findByDoctorAndDate(doctor, date);
            
        if (availability.isPresent()) {
            availabilityRepository.delete(availability.get().getId());
            return true;
        }
        return false;
    }
    
    /**
     * Updates existing availability
     */
    public boolean updateAvailability(Doctor doctor, LocalDate date, 
                                    LocalTime newStartTime, LocalTime newEndTime) {
        Optional<DoctorAvailability> existing = 
            availabilityRepository.findByDoctorAndDate(doctor, date);
            
        if (existing.isEmpty()) {
            return false;
        }
        
        try {
            setAvailability(doctor, date, newStartTime, newEndTime);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Checks if a doctor is available at a specific date and time
     */
    public boolean isDoctorAvailable(Doctor doctor, LocalDate date, LocalTime time) {
        Optional<DoctorAvailability> availability = 
            availabilityRepository.findByDoctorAndDate(doctor, date);
            
        if (availability.isEmpty()) {
            return false;
        }
        
        DoctorAvailability avail = availability.get();
        return !time.isBefore(avail.getStartTime()) && 
               !time.isAfter(avail.getEndTime().minusMinutes(SLOT_DURATION_MINUTES));
    }
    
    /**
     * Gets the next available slot for a doctor
     */
    public Optional<AppointmentSlot> getNextAvailableSlot(Doctor doctor) {
        LocalDate currentDate = LocalDate.now();
        
        // Check next 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentDate.plusDays(i);
            Optional<DoctorAvailability> availability = 
                availabilityRepository.findByDoctorAndDate(doctor, date);
                
            if (availability.isPresent()) {
                List<AppointmentSlot> slots = generateSlots(availability.get());
                Optional<AppointmentSlot> availableSlot = slots.stream()
                    .filter(AppointmentSlot::isAvailable)
                    .findFirst();
                    
                if (availableSlot.isPresent()) {
                    return availableSlot;
                }
            }
        }
        
        return Optional.empty();
    }
}