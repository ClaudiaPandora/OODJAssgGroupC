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
        // Ensure file exists with default data
        ensureFileExists();
    }
    
    private void ensureFileExists() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        if (lines.isEmpty()) {
            // Create some default appointments
            List<Appointment> defaultAppointments = new ArrayList<>();
            
            Appointment a1 = new Appointment();
            a1.setId("A0001");
            a1.setCustomerId("U001");
            a1.setServiceType(ServiceType.NORMAL);
            a1.setDate("2024-04-10");
            a1.setStartTime("10:00");
            a1.setDuration(1);
            a1.setStatus(AppointmentStatus.COMPLETED);
            a1.setTechnicianId("T001");
            a1.setCounterStaffId("C001");
            a1.setAmount(100.0);
            defaultAppointments.add(a1);
            
            Appointment a2 = new Appointment();
            a2.setId("A0002");
            a2.setCustomerId("U001");
            a2.setServiceType(ServiceType.MAJOR);
            a2.setDate("2024-04-11");
            a2.setStartTime("14:00");
            a2.setDuration(3);
            a2.setStatus(AppointmentStatus.COMPLETED);
            a2.setTechnicianId("T001");
            a2.setCounterStaffId("C001");
            a2.setAmount(300.0);
            defaultAppointments.add(a2);
            
            Appointment a3 = new Appointment();
            a3.setId("A0003");
            a3.setCustomerId("U002");
            a3.setServiceType(ServiceType.NORMAL);
            a3.setDate("2024-04-12");
            a3.setStartTime("09:00");
            a3.setDuration(1);
            a3.setStatus(AppointmentStatus.ASSIGNED);
            a3.setTechnicianId("T002");
            a3.setCounterStaffId("C002");
            a3.setAmount(100.0);
            defaultAppointments.add(a3);
            
            saveAll(defaultAppointments);
        }
    }
    
    @Override
    public List<Appointment> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Appointment> appointments = new ArrayList<>();
        
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    try {
                        Appointment a = new Appointment();
                        a.setId(parts[0].trim());
                        a.setCustomerId(parts[1].trim());
                        a.setServiceType(ServiceType.valueOf(parts[2].trim()));
                        a.setDate(parts[3].trim());
                        a.setStartTime(parts[4].trim());
                        a.setDuration(Integer.parseInt(parts[5].trim()));
                        a.setStatus(AppointmentStatus.valueOf(parts[6].trim()));
                        a.setTechnicianId(parts[7].trim().isEmpty() ? null : parts[7].trim());
                        a.setCounterStaffId(parts[8].trim().isEmpty() ? null : parts[8].trim());
                        
                        // Try to read amount if available (parts length >= 10)
                        if (parts.length >= 10 && !parts[9].trim().isEmpty()) {
                            a.setAmount(Double.parseDouble(parts[9].trim()));
                        }
                        
                        appointments.add(a);
                    } catch (Exception e) {
                        System.err.println("Error parsing appointment: " + line);
                    }
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
            if (technicianId.equals(a.getTechnicianId())) {
                result.add(a);
            }
        }
        return result;
    }
    
    @Override
    public boolean save(Appointment appointment) {
        List<Appointment> all = readAll();
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            appointment.setId(IDGenerator.generateAppointmentID(all.size()));
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
            sb.append(a.getCustomerId()).append("|");
            sb.append(a.getServiceType()).append("|");
            sb.append(a.getDate()).append("|");
            sb.append(a.getStartTime()).append("|");
            sb.append(a.getDuration()).append("|");
            sb.append(a.getStatus()).append("|");
            sb.append(a.getTechnicianId() != null ? a.getTechnicianId() : "").append("|");
            sb.append(a.getCounterStaffId() != null ? a.getCounterStaffId() : "").append("|");
            sb.append(a.getAmount() > 0 ? a.getAmount() : "");
            lines.add(sb.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}