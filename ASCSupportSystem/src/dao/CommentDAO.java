package dao;

import models.Comment;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class CommentDAO implements FileDAO<Comment> {
    
    private static final String FILE_NAME = "src/data/comments.txt";
    
    public CommentDAO() {
        ensureFileExists();
    }
    
    private void ensureFileExists() {
        FileUtils.readLines(FILE_NAME);
    }
    
    @Override
    public List<Comment> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Comment> comments = new ArrayList<>();
        
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Comment c = new Comment();
                    c.setId(parts[0].trim());
                    c.setAppointmentId(parts[1].trim());
                    c.setCustomerId(parts[2].trim());
                    c.setTechnicianId(parts[3].trim().isEmpty() ? null : parts[3].trim());
                    c.setCounterStaffId(parts[4].trim().isEmpty() ? null : parts[4].trim());
                    c.setContent(parts[5].trim());
                    comments.add(c);
                }
            }
        }
        
        return comments;
    }
    
    @Override
    public Comment findById(String id) {
        return readAll().stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean save(Comment comment) {
        List<Comment> all = readAll();
        if (comment.getId() == null || comment.getId().isEmpty()) {
            comment.setId(IDGenerator.generateCommentID(all.size()));
        }
        all.add(comment);
        return saveAll(all);
    }
    
    @Override
    public boolean update(Comment comment) {
        List<Comment> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(comment.getId())) {
                all.set(i, comment);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<Comment> all = readAll();
        boolean removed = all.removeIf(c -> c.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<Comment> comments) {
        List<String> lines = new ArrayList<>();
        for (Comment c : comments) {
            lines.add(c.getId() + "|" + c.getAppointmentId() + "|" + c.getCustomerId() + "|" +
                     (c.getTechnicianId() != null ? c.getTechnicianId() : "") + "|" +
                     (c.getCounterStaffId() != null ? c.getCounterStaffId() : "") + "|" +
                     c.getContent());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}