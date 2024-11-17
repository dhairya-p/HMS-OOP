package service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entity.Medicine;
import util.CSVReader;

public class MedicineImportService implements DataImportService<Medicine> {
    @Override
    public List<Medicine> importData(String filename) throws IOException {
        CSVReader reader = new CSVReader(filename);
        List<Medicine> medicines = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (String[] row : reader.getData()) {
            String name = row[0].trim();
            int initialStock = Integer.parseInt(row[1].trim());
            int lowStockAlert = Integer.parseInt(row[2].trim());

            Medicine medicine = new Medicine(name, initialStock, lowStockAlert);
            medicines.add(medicine);
        }

        return medicines;
    }
}