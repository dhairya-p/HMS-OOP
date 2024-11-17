// Staff Import Service
package service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import entity.*;
import util.CSVReader;

public class StaffImportService implements DataImportService<User> {
    @Override
    public List<User> importData(String filename) throws IOException {
        CSVReader reader = new CSVReader(filename);
        List<User> staff = new ArrayList<>();

        for (String[] row : reader.getData()) {
            String staffId = row[0].trim();
            String name = row[1].trim();
            String role = row[2].trim().toUpperCase();
            String gender = row[3].trim();
            int age = Integer.parseInt(row[4].trim());

            User user = switch (role) {
                case "DOCTOR" -> new Doctor(staffId, "password", name, "General Medicine");
                case "PHARMACIST" -> new Pharmacist(staffId, "password", name);
                case "ADMINISTRATOR" -> new Administrator(staffId, "password", name);
                default -> throw new IllegalArgumentException("Invalid role: " + role);
            };

            staff.add(user);
        }

        return staff;
    }
}
