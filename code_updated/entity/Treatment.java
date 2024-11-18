package entity;
public class Treatment {
    private final String description;
    
    public Treatment(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return description;
    }

    public String getDescription() {
        return description;
    }
}