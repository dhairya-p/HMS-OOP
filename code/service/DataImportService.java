package service;

import entity.*;
import util.CSVReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public interface DataImportService<T> {
    List<T> importData(String filename) throws IOException;
}