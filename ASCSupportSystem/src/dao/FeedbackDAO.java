package dao;

import models.Feedback;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class FeedbackDAO implements FileDAO<Feedback> {
    
    private static final String FILE_NAME = "src/data/feedback.txt";
    
    public FeedbackDAO() {
        ensureFileExists();
    }
    
    private void ensureFileExists() {
        FileUtils.readLines(FILE_NAME); // This will create the file if it doesn't exist
    }
    
    @Override
    public List<Feedback> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Feedback> feedbacks = new ArrayList<>();
        
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    Feedback f = new Feedback();
                    f.setId(parts[0].trim());
                    f.setAppointmentId(parts[1].trim());
                    f.setTechnicianId(parts[2].trim());
                    f.setContent(parts[3].trim());
                    
                    // Rating if available
                    if (parts.length >= 5) {
                        try {
                            f.setRating(Integer.parseInt(parts[4].trim()));
                        } catch (NumberFormatException e) {
                            f.setRating(5);
                        }
                    } else {
                        f.setRating(5);
                    }
                    
                    feedbacks.add(f);
                }
            }
        }
        
        return feedbacks;
    }
    
    @Override
    public Feedback findById(String id) {
        return readAll().stream()
            .filter(f -> f.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean save(Feedback feedback) {
        List<Feedback> all = readAll();
        if (feedback.getId() == null || feedback.getId().isEmpty()) {
            feedback.setId(IDGenerator.generateFeedbackID(all.size()));
        }
        all.add(feedback);
        return saveAll(all);
    }
    
    @Override
    public boolean update(Feedback feedback) {
        List<Feedback> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(feedback.getId())) {
                all.set(i, feedback);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<Feedback> all = readAll();
        boolean removed = all.removeIf(f -> f.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<Feedback> feedbacks) {
        List<String> lines = new ArrayList<>();
        for (Feedback f : feedbacks) {
            lines.add(f.getId() + "|" + f.getAppointmentId() + "|" + 
                     f.getTechnicianId() + "|" + f.getContent() + "|" + f.getRating());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}