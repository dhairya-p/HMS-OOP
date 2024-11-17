package entity;

import java.util.*;
import java.time.LocalDateTime;
import entity.enums.*;

public class Pharmacist extends User {
    private List<Medicine> dispensedMedications;
    private List<ReplenishmentRequest> replenishmentRequests;

    public Pharmacist(String hospitalId, String password, String name) {
        super(hospitalId, password, name);
        this.dispensedMedications = new ArrayList<>();
        this.replenishmentRequests = new ArrayList<>();
    }

    public boolean dispenseMedication(Prescription prescription) {
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            return false;
        }

        Medicine medicine = prescription.getMedicine();
        if (!medicine.canFulfillQuantity(prescription.getQuantity())) {
            return false;
        }

        medicine.updateStock(-prescription.getQuantity());
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        prescription.setDispensedBy(this);
        prescription.setDispensedDateTime(LocalDateTime.now());
        
        dispensedMedications.add(medicine);
        return true;
    }

    public ReplenishmentRequest createReplenishmentRequest(Medicine medicine, int requestedQuantity) {
        if (medicine == null || requestedQuantity <= 0) {
            throw new IllegalArgumentException("Invalid medicine or quantity");
        }

        if (!medicine.isLowStock() && medicine.getCurrentStock() + requestedQuantity > medicine.getMaxStock()) {
            throw new IllegalArgumentException("Medicine is not low on stock or request exceeds max stock");
        }

        if (medicine.isReplenishmentRequested()) {
            throw new IllegalStateException("Replenishment already requested for this medicine");
        }

        ReplenishmentRequest request = new ReplenishmentRequest(
            medicine,
            requestedQuantity,
            this,
            LocalDateTime.now()
        );

        medicine.requestReplenishment();
        replenishmentRequests.add(request);
        return request;
    }

    public List<Medicine> getDispensedMedications() {
        return new ArrayList<>(dispensedMedications);
    }

    public List<ReplenishmentRequest> getReplenishmentRequests() {
        return new ArrayList<>(replenishmentRequests);
    }

    public List<ReplenishmentRequest> getPendingReplenishmentRequests() {
        return replenishmentRequests.stream()
            .filter(request -> request.getStatus() == ReplenishmentStatus.PENDING)
            .toList();
    }
}
