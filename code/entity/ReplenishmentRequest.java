package entity;

import java.time.LocalDateTime;

import entity.enums.ReplenishmentStatus;

public class ReplenishmentRequest {
    private final Medicine medicine;
    private final int requestedQuantity;
    private final Pharmacist requestedBy;
    private final LocalDateTime requestDateTime;
    private Administrator processedBy;
    private LocalDateTime processedDateTime;
    private ReplenishmentStatus status;

    public ReplenishmentRequest(Medicine medicine, int requestedQuantity, 
                              Pharmacist requestedBy, LocalDateTime requestDateTime) {
        this.medicine = medicine;
        this.requestedQuantity = requestedQuantity;
        this.requestedBy = requestedBy;
        this.requestDateTime = requestDateTime;
        this.status = ReplenishmentStatus.PENDING;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public Pharmacist getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public Administrator getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Administrator processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(LocalDateTime processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    public ReplenishmentStatus getStatus() {
        return status;
    }

    public void setStatus(ReplenishmentStatus status) {
        this.status = status;
    }
}

