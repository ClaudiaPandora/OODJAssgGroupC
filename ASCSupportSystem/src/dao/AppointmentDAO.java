package dao;

import enums.AppointmentStatus;
import enums.ServiceType;
import models.Appointment;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class AppointmentDAO implements FileDAO<Appointment> {
    
    private static final String FILE_NAME = "src/data/appointments.txt";
    
    public AppointmentDAO() {
        // No mock data
    }
    
    @Override
    public List<Appointment> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Appointment> appointments = new ArrayList<>();
        
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                // Use -1 parameter to keep trailing empty strings
                String[] parts = line.split("\\|", -1);
                
                // We need at least 10 fields (including amount)
                if (parts.length >= 10) {
                    try {
                        Appointment a = new Appointment();
                        a.setId(parts[0].trim());
                        a.setCustomerId(parts[1].trim());
                        a.setServiceType(ServiceType.valueOf(parts[2].trim()));
                        a.setDate(parts[3].trim());
                        a.setStartTime(parts[4].trim());
                        a.setDuration(Integer.parseInt(parts[5].trim()));
                        a.setStatus(AppointmentStatus.valueOf(parts[6].trim()));
                        
                        // Handle empty technician ID (empty string becomes null)
                        String techId = parts[7].trim();
                        a.setTechnicianId(techId.isEmpty() ? null : techId);
                        
                        // Handle empty counter staff ID (empty string becomes null)
                        String staffId = parts[8].trim();
                        a.setCounterStaffId(staffId.isEmpty() ? null : staffId);
                        
                        // Read amount from the 10th field (index 9)
                        String amountStr = parts[9].trim();
                        if (!amountStr.isEmpty()) {
                            a.setAmount(Double.parseDouble(amountStr));
                        } else {
                            // Default amount based on service type
                            a.setAmount(a.getServiceType() == ServiceType.MAJOR ? 300.0 : 100.0);
                        }
                        
                        appointments.add(a);
                    } catch (Exception e) {
                        System.err.println("Error parsing appointment: " + line + " - " + e.getMessage());
                    }
                } else if (parts.length == 9) {
                    // Handle old format (no amount) - add default amount
                    try {
                        Appointment a = new Appointment();
                        a.setId(parts[0].trim());
                        a.setCustomerId(parts[1].trim());
                        a.setServiceType(ServiceType.valueOf(parts[2].trim()));
                        a.setDate(parts[3].trim());
                        a.setStartTime(parts[4].trim());
                        a.setDuration(Integer.parseInt(parts[5].trim()));
                        a.setStatus(AppointmentStatus.valueOf(parts[6].trim()));
                        
                        String techId = parts[7].trim();
                        a.setTechnicianId(techId.isEmpty() ? null : techId);
                        
                        String staffId = parts[8].trim();
                        a.setCounterStaffId(staffId.isEmpty() ? null : staffId);
                        
                        // Default amount based on service type
                        a.setAmount(a.getServiceType() == ServiceType.MAJOR ? 300.0 : 100.0);
                        
                        appointments.add(a);
                    } catch (Exception e) {
                        System.err.println("Error parsing old format appointment: " + line);
                    }
                } else {
                    System.err.println("Invalid appointment format (expected at least 9 fields, got " + parts.length + "): " + line);
                }
            }
        }
        
        return appointments;
    }
    
    @Override
    public Appointment findById(String id) {
        if (id == null) return null;
        return readAll().stream()
            .filter(a -> a.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Appointment> findByCustomerId(String customerId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : readAll()) {
            if (a.getCustomerId().equals(customerId)) {
                result.add(a);
            }
        }
        return result;
    }
    
    public List<Appointment> findByTechnicianId(String technicianId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : readAll()) {
            if (technicianId != null && technicianId.equals(a.getTechnicianId())) {
                result.add(a);
            }
        }
        return result;
    }
    
    public List<Appointment> findByStatus(AppointmentStatus status) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : readAll()) {
            if (a.getStatus() == status) {
                result.add(a);
            }
        }
        return result;
    }
    
    public List<Appointment> findByDate(String date) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : readAll()) {
            if (a.getDate().equals(date)) {
                result.add(a);
            }
        }
        return result;
    }
    
    @Override
    public boolean save(Appointment appointment) {
        List<Appointment> all = readAll();
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            // Generate new ID based on existing appointments
            int maxId = 0;
            for (Appointment a : all) {
                try {
                    int num = Integer.parseInt(a.getId().substring(1));
                    if (num > maxId) maxId = num;
                } catch (Exception e) {}
            }
            appointment.setId(String.format("A%04d", maxId + 1));
        }
        all.add(appointment);
        return saveAll(all);
    }
    
    @Override
    public boolean update(Appointment appointment) {
        List<Appointment> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(appointment.getId())) {
                all.set(i, appointment);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<Appointment> all = readAll();
        boolean removed = all.removeIf(a -> a.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<Appointment> appointments) {
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) {
            StringBuilder sb = new StringBuilder();
            sb.append(a.getId()).append("|");
            sb.append(a.getCustomerId() != null ? a.getCustomerId() : "").append("|");
            sb.append(a.getServiceType() != null ? a.getServiceType() : "NORMAL").append("|");
            sb.append(a.getDate() != null ? a.getDate() : "").append("|");
            sb.append(a.getStartTime() != null ? a.getStartTime() : "").append("|");
            sb.append(a.getDuration()).append("|");
            sb.append(a.getStatus() != null ? a.getStatus() : "PENDING").append("|");
            // Technician ID - write empty string if null
            sb.append(a.getTechnicianId() != null ? a.getTechnicianId() : "").append("|");
            // Counter Staff ID - write empty string if null
            sb.append(a.getCounterStaffId() != null ? a.getCounterStaffId() : "").append("|");
            // Amount (price) - this is the 10th field, ALWAYS save it
            sb.append(a.getAmount() > 0 ? a.getAmount() : (a.getServiceType() == ServiceType.MAJOR ? 300.0 : 100.0));
            // Exactly 10 fields total (no extra fields)
            lines.add(sb.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}