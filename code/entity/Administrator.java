package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.enums.InventoryActionType;
import entity.enums.ReplenishmentStatus;

public class Administrator extends User {
    private final Set<User> managedStaff;
    private final List<ReplenishmentRequest> processedRequests;
    private final List<MedicineInventoryAction> inventoryActions;

    public Administrator(String hospitalId, String password, String name) {
        super(hospitalId, password, name);
        this.managedStaff = new HashSet<>();
        this.processedRequests = new ArrayList<>();
        this.inventoryActions = new ArrayList<>();
    }

    public void addStaffMember(User staff) {
        if (staff == null || staff instanceof Administrator) {
            throw new IllegalArgumentException("Invalid staff member");
        }
        managedStaff.add(staff);
    }

    public void removeStaffMember(User staff) {
        if (staff == null || !managedStaff.contains(staff)) {
            throw new IllegalArgumentException("Staff member not found");
        }
        managedStaff.remove(staff);
    }

    public void updateStaffMember(User staff) {
        if (staff == null || !managedStaff.contains(staff)) {
            throw new IllegalArgumentException("Staff member not found");
        }
        managedStaff.remove(staff);
        managedStaff.add(staff);
    }

    public void processReplenishmentRequest(ReplenishmentRequest request, boolean approve) {
        if (request == null || request.getStatus() != ReplenishmentStatus.PENDING) {
            throw new IllegalArgumentException("Invalid or already processed request");
        }

        Medicine medicine = request.getMedicine();
        if (approve) {
            medicine.fulfillReplenishment(request.getRequestedQuantity());
            request.setStatus(ReplenishmentStatus.APPROVED);
            
            MedicineInventoryAction action = new MedicineInventoryAction(
                medicine,
                request.getRequestedQuantity(),
                InventoryActionType.REPLENISHMENT,
                LocalDateTime.now(),
                this
            );
            inventoryActions.add(action);
        } else {
            medicine.cancelReplenishmentRequest();
            request.setStatus(ReplenishmentStatus.REJECTED);
        }

        request.setProcessedBy(this);
        request.setProcessedDateTime(LocalDateTime.now());
        processedRequests.add(request);
    }

    public void updateMedicineStock(Medicine medicine, int quantity, String reason) {
        if (medicine == null || reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        medicine.updateStock(quantity);
        
        MedicineInventoryAction action = new MedicineInventoryAction(
            medicine,
            quantity,
            quantity > 0 ? InventoryActionType.ADDITION : InventoryActionType.REDUCTION,
            LocalDateTime.now(),
            this,
            reason
        );
        inventoryActions.add(action);
    }

    public void updateMedicineLowStockAlert(Medicine medicine, int newAlertLevel) {
        if (medicine == null || newAlertLevel < 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        medicine.setLowStockAlert(newAlertLevel);
    }

    public List<User> getManagedStaff() {
        return new ArrayList<>(managedStaff);
    }

    public List<User> getManagedStaffByRole(Class<? extends User> role) {
        return managedStaff.stream()
            .filter(staff -> role.isInstance(staff))
            .toList();
    }

    public List<ReplenishmentRequest> getProcessedRequests() {
        return new ArrayList<>(processedRequests);
    }

    public List<MedicineInventoryAction> getInventoryActions() {
        return new ArrayList<>(inventoryActions);
    }

    public List<MedicineInventoryAction> getInventoryActionsByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return inventoryActions.stream()
            .filter(action -> {
                LocalDateTime actionDate = action.getActionDateTime();
                return !actionDate.isBefore(startDate) && !actionDate.isAfter(endDate);
            })
            .toList();
    }

    public List<MedicineInventoryAction> getInventoryActionsByMedicine(Medicine medicine) {
        return inventoryActions.stream()
            .filter(action -> action.getMedicine().equals(medicine))
            .toList();
    }
}
