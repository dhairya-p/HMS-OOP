package entity;

import java.time.LocalDate;

public class Diagnosis {
    private final LocalDate date;
    private final String description;
    private final Treatment treatment;
    
    public Diagnosis(String description, Treatment treatment) {
        this.date = LocalDate.now();
        this.description = description;
        this.treatment = treatment;
    }
    
    @Override
    public String toString() {
        return String.format("Date: %s\nDiagnosis: %s\nTreatment: %s", 
            date, description, treatment);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Treatment getTreatment() {
        return treatment;
    }
}