package dao;

import models.Feedback;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class FeedbackDAO implements FileDAO<Feedback> {
    
    private static final String FILE_NAME = "src/data/feedback.txt";
    
    @Override
    public List<Feedback> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Feedback> notes = new ArrayList<>();
        for (String line : lines) {
            Feedback note = Feedback.fromString(line);
            if (note != null) notes.add(note);
        }
        return notes;
    }
    
    @Override
    public Feedback findById(String id) {
        return readAll().stream()
            .filter(n -> n.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Feedback> findByAppointmentId(String appointmentId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback note : readAll()) {
            if (note.getAppointmentId().equals(appointmentId)) {
                result.add(note);
            }
        }
        return result;
    }
    
    public List<Feedback> findByTechnicianId(String technicianId) {
        List<Feedback> result = new ArrayList<>();
        for (Feedback note : readAll()) {
            if (technicianId != null && technicianId.equals(note.getTechnicianId())) {
                result.add(note);
            }
        }
        return result;
    }
    
    @Override
    public boolean save(Feedback note) {
        List<Feedback> all = readAll();
        
        // Check if note already exists for this appointment and technician
        boolean exists = false;
        for (Feedback existing : all) {
            if (existing.getAppointmentId().equals(note.getAppointmentId()) 
                    && existing.getTechnicianId().equals(note.getTechnicianId())) {
                exists = true;
                break;
            }
        }
        
        // Only add if it doesn't exist (no duplicate)
        if (!exists) {
            note.setId(generateFeedbackID(all.size()));
            all.add(note);
            return saveAll(all);
        }
        
        return true; // Already exists, consider it successful
    }
    
    @Override
    public boolean update(Feedback note) {
        List<Feedback> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(note.getId())) {
                all.set(i, note);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<Feedback> all = readAll();
        boolean removed = all.removeIf(n -> n.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<Feedback> notes) {
        List<String> lines = new ArrayList<>();
        for (Feedback n : notes) {
            lines.add(n.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
    
    private String generateFeedbackID(int currentSize) {
        // Format: F + 4 digits (e.g., F0001, F0002, F0010)
        int nextId = currentSize + 1;
        return String.format("F%04d", nextId);
    }
}