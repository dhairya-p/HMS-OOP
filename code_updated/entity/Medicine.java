package entity;

public class Medicine {
    private final String name;
    private int currentStock;
    private int lowStockAlert; // Remove final modifier
    private boolean replenishmentRequested;
    private final int maxStock; // Add maxStock field
    
    public Medicine(String name, int initialStock, int lowStockAlert) {
        // Existing constructor validation
        this.name = name;
        this.currentStock = initialStock;
        this.lowStockAlert = lowStockAlert;
        this.maxStock = initialStock * 2; // Set max stock as double initial stock
        this.replenishmentRequested = false;
    }
    
    public void setLowStockAlert(int newAlertLevel) { // Add setter
        if (newAlertLevel < 0) {
            throw new IllegalArgumentException("Alert level cannot be negative");
        }
        this.lowStockAlert = newAlertLevel;
    }
    
    public int getMaxStock() {
        return maxStock;
    }

    public boolean updateStock(int quantity) {
        int newStock = this.currentStock + quantity;
        if (newStock < 0) {
            return false;
        }
        this.currentStock = newStock;
        return true;
    }
    
    public boolean isLowStock() {
        return currentStock <= lowStockAlert;
    }
    
    public boolean requestReplenishment() {
        if (replenishmentRequested) {
            return false;
        }
        replenishmentRequested = true;
        return true;
    }
    
    public boolean fulfillReplenishment(int quantity) {
        if (!replenishmentRequested || quantity <= 0) {
            return false;
        }
        currentStock += quantity;
        replenishmentRequested = false;
        return true;
    }
    
    public boolean cancelReplenishmentRequest() {
        if (!replenishmentRequested) {
            return false;
        }
        replenishmentRequested = false;
        return true;
    }
    
    public boolean canFulfillQuantity(int quantity) {
        return quantity > 0 && quantity <= currentStock;
    }
    
    @Override
    public String toString() {
        return String.format("Medicine: %s%nCurrent Stock: %d%nLow Stock Alert Level: %d%n" +
                           "Replenishment Requested: %s%nStock Status: %s",
            name, currentStock, lowStockAlert, 
            replenishmentRequested ? "Yes" : "No",
            isLowStock() ? "LOW STOCK" : "Normal");
    }
    
    public String getName() {
        return name;
    }
    
    public int getCurrentStock() {
        return currentStock;
    }
    
    public int getLowStockAlert() {
        return lowStockAlert;
    }
    
    public boolean isReplenishmentRequested() {
        return replenishmentRequested;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public void setReplenishmentRequested(boolean replenishmentRequested) {
        this.replenishmentRequested = replenishmentRequested;
    }
}