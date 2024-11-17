# HMS-OOP

Use system/HospitalManagementSystem as entry point to system

# Low Coupling Analysis

## 1. Dependency Injection
- Primary example: DoctorUI class
```java
public class DoctorUI {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final AppointmentController appointmentController;
    private final MedicalRecordController medicalRecordController;
    private final DoctorAvailabilityController availabilityController;

    public DoctorUI(Scanner scanner, AuthenticationController authController,
                    AppointmentController appointmentController,
                    MedicalRecordController medicalRecordController,
                    DoctorAvailabilityController availabilityController) {
        this.scanner = scanner;
        this.authController = authController;
        this.appointmentController = appointmentController;
        this.medicalRecordController = medicalRecordController;
        this.availabilityController = availabilityController;
    }
}
```
- Dependencies are injected through constructor rather than created inside the class
- Makes the class testable and allows for different implementations
- Reduces direct dependencies between components

## 2. Interface Segregation
- Example: Controller interfaces in controller.interfaces package
```java
interface AppointmentService {
    List<AppointmentSlot> getAvailableSlots(LocalDate date, Doctor doctor);
    Appointment scheduleAppointment(Patient patient, Doctor doctor, AppointmentSlot slot);
    boolean rescheduleAppointment(String appointmentId, AppointmentSlot newSlot);
    // Other appointment-specific methods
}

interface MedicalRecordService {
    MedicalRecord getMedicalRecord(String patientId);
    void updateContactInfo(String patientId, String phone, String email);
    void addDiagnosis(String patientId, String diagnosis, String treatment);
    // Other medical record-specific methods
}
```
- Services are broken down into focused interfaces
- Controllers implement only relevant interfaces
- Clients depend only on the interfaces they need

# High Cohesion Analysis

## 1. Single Responsibility Principle (SRP)
- Example: Medicine class handles only medicine-related operations
```java
public class Medicine {
    private final String name;
    private int currentStock;
    private int lowStockAlert;
    private boolean replenishmentRequested;
    private final int maxStock;

    public boolean updateStock(int quantity) {
        int newStock = this.currentStock + quantity;
        if (newStock < 0) return false;
        this.currentStock = newStock;
        return true;
    }

    public boolean isLowStock() {
        return currentStock <= lowStockAlert;
    }

    public boolean requestReplenishment() {
        if (replenishmentRequested) return false;
        replenishmentRequested = true;
        return true;
    }
}
```
- Focuses solely on medicine inventory management
- Each method serves a specific purpose related to medicine state

## 2. Separation of Concerns
- Example: Appointment management is distributed across multiple focused classes
```java
// Entity class handles data and basic validation
public class Appointment {
    private final String appointmentId;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private AppointmentOutcomeRecord outcomeRecord;
    // Basic getters/setters
}

// Controller handles business logic
public class AppointmentController implements AppointmentService {
    private final DoctorAvailabilityService availabilityService;
    private final AppointmentRepository appointmentRepository;
    
    public List<AppointmentSlot> getAvailableSlots(LocalDate date, Doctor doctor) {
        // Complex business logic for slot availability
    }
    
    public boolean rescheduleAppointment(String appointmentId, AppointmentSlot newSlot) {
        // Complex rescheduling logic
    }
}

// Repository handles persistence
public class AppointmentRepository implements Repository<Appointment, String> {
    private final Map<String, Appointment> appointments;
    
    public Optional<Appointment> findById(String id) {
        return Optional.ofNullable(appointments.get(id));
    }
    
    public List<Appointment> findByDoctor(Doctor doctor) {
        // Database query logic
    }
}
```

# SOLID Principles Implementation

## 1. Open/Closed Principle (OCP)
- Example: User class hierarchy
```java
public abstract class User {
    private final String hospitalId;
    private String password;
    private final String name;
    
    protected abstract UserRole getUserRole();
}

public class Doctor extends User {
    private final String specialization;
    protected UserRole getUserRole() {
        return UserRole.DOCTOR;
    }
}

public class Pharmacist extends User {
    private List<Medicine> dispensedMedications;
    protected UserRole getUserRole() {
        return UserRole.PHARMACIST;
    }
}
```
- New user types can be added without modifying existing code
- Each user type implements its specific behavior

## 2. Liskov Substitution Principle (LSP)
- Example: Repository interface implementation
```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(ID id);
}

public class MedicineRepository implements Repository<Medicine, String> {
    public Medicine save(Medicine medicine) {
        // Implementation maintains contract
    }
    
    public Optional<Medicine> findById(String id) {
        // Implementation maintains contract
    }
}
```
- All repository implementations can be used interchangeably
- Derived classes maintain the base contract

## 3. Interface Segregation Principle (ISP)
- Already discussed in Low Coupling section
- Clients are not forced to depend on methods they don't use

## 4. Dependency Inversion Principle (DIP)
- Example: Controllers depending on interfaces rather than concrete implementations
```java
public class AppointmentController implements AppointmentService {
    private final DoctorAvailabilityService availabilityService;
    // Depends on interface, not concrete class
}
```

# Design Patterns Used

## 1. Singleton Pattern
- Used for repositories to ensure single instance of data access
```java
public class MedicineRepository implements Repository<Medicine, String> {
    private static MedicineRepository instance;
    private final Map<String, Medicine> medicines;
    
    private MedicineRepository() {
        this.medicines = new HashMap<>();
    }
    
    public static MedicineRepository getInstance() {
        if (instance == null) {
            instance = new MedicineRepository();
        }
        return instance;
    }
}
```

## 2. MVC Pattern
- Model: Entity classes (Appointment, Medicine, etc.)
- View: UI classes (DoctorUI, PharmacistUI, etc.)
- Controller: Controller classes implementing service interfaces

## 3. Repository Pattern
- Abstracts data persistence
- Provides collection-like interface to access domain objects

## 4. Factory Pattern
- Used in DataImportManager for creating different types of entities

These patterns and principles together create a maintainable, extensible system where components are loosely coupled but highly cohesive, making the system robust and easier to modify or extend.
