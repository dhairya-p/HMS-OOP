package entity;

import java.time.LocalDateTime;

import entity.enums.InventoryActionType;

public class MedicineInventoryAction {
    private final Medicine medicine;
    private final int quantity;
    private final InventoryActionType actionType;
    private final LocalDateTime actionDateTime;
    private final Administrator performedBy;
    private final String reason;

    public MedicineInventoryAction(Medicine medicine, int quantity, 
                                 InventoryActionType actionType,
                                 LocalDateTime actionDateTime, 
                                 Administrator performedBy) {
        this(medicine, quantity, actionType, actionDateTime, performedBy, null);
    }

    public MedicineInventoryAction(Medicine medicine, int quantity, 
                                 InventoryActionType actionType,
                                 LocalDateTime actionDateTime, 
                                 Administrator performedBy,
                                 String reason) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.actionType = actionType;
        this.actionDateTime = actionDateTime;
        this.performedBy = performedBy;
        this.reason = reason;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public int getQuantity() {
        return quantity;
    }

    public InventoryActionType getActionType() {
        return actionType;
    }

    public LocalDateTime getActionDateTime() {
        return actionDateTime;
    }

    public Administrator getPerformedBy() {
        return performedBy;
    }

    public String getReason() {
        return reason;
    }
}
