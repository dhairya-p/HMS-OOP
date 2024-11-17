package util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CSVReader {
    private final String filename;
    private final List<String[]> data;
    private final String[] headers;

    public CSVReader(String filename) throws IOException {
        this.filename = filename;
        List<String> lines = Files.readAllLines(Paths.get(filename));
        if (lines.isEmpty()) {
            throw new IOException("Empty CSV file: " + filename);
        }

        // Parse headers
        this.headers = parseCSVLine(lines.get(0));
        
        // Parse data
        this.data = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            data.add(parseCSVLine(lines.get(i)));
        }
    }

    private String[] parseCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public String[] getHeaders() {
        return headers.clone();
    }

    public List<String[]> getData() {
        return new ArrayList<>(data);
    }
}
