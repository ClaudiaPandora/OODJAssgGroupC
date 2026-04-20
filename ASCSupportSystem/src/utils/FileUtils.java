package utils;

import java.io.*;
import java.util.*;

public class FileUtils {
    
    private static final String DATA_DIR = "src/data/";
    
    public static List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(fileName);
        
        // Ensure directory exists
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // If file doesn't exist, create it with default data
        if (!file.exists()) {
            createDefaultDataFile(fileName);
        }
        
        // Read the file
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            e.printStackTrace();
        }
        
        return lines;
    }
    
    private static void createDefaultDataFile(String fileName) {
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                if (fileName.contains("managers.txt")) {
                    writer.write("M001|John Manager|012-3456789|manager@apu.edu.my|manager1|password");
                } else if (fileName.contains("counter_staff.txt")) {
                    writer.write("C001|Sarah Counter|012-3456788|counter@apu.edu.my|counter1|password");
                    writer.newLine();
                    writer.write("C002|Jane Counter|012-3456781|counter2@apu.edu.my|counter2|password");
                } else if (fileName.contains("technicians.txt")) {
                    writer.write("T001|Mike Technician|012-3456787|tech1@apu.edu.my|tech1|password");
                    writer.newLine();
                    writer.write("T002|Dave Technician|012-3456786|tech2@apu.edu.my|tech2|password");
                    writer.newLine();
                    writer.write("T003|Steve Technician|012-3456782|tech3@apu.edu.my|tech3|password");
                } else if (fileName.contains("customers.txt")) {
                    writer.write("U001|Alice Customer|012-3456785|alice@gmail.com|cust1|password");
                    writer.newLine();
                    writer.write("U002|Bob Customer|012-3456784|bob@gmail.com|cust2|password");
                    writer.newLine();
                    writer.write("U003|Charlie Customer|012-3456783|charlie@gmail.com|cust3|password");
                } else if (fileName.contains("services.txt")) {
                    writer.write("NORMAL|100.0");
                    writer.newLine();
                    writer.write("MAJOR|300.0");
                }
            }
            
            System.out.println("Created default data file: " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error creating default file: " + fileName);
            e.printStackTrace();
        }
    }
    
    public static boolean writeLines(String fileName, List<String> lines) {
        try {
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String line : lines) {
                    if (line != null && !line.trim().isEmpty()) {
                        writer.write(line.trim());
                        writer.newLine();
                    }
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}