package dao;

import models.Comment;
import utils.FileUtils;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

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
                Comment c = Comment.fromString(line);
                if (c != null) {
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
            lines.add(c.getId() + "|" + c.getAppointmentId() + "|" + c.getCustomerId() + "|"
                    + (c.getTechnicianId() != null ? c.getTechnicianId() : "") + "|"
                    + (c.getCounterStaffId() != null ? c.getCounterStaffId() : "") + "|"
                    + c.getContent() + "|" + c.getRating());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }

    @Override
    public String getFileName() {
        return FILE_NAME;
    }

    public List<Comment> findByCustomerId(String customerId) {
        List<Comment> result = new ArrayList<>();
        for (Comment c : readAll()) {
            if (customerId.equals(c.getCustomerId())) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Comment> findByAppointmentId(String appointmentId) {
        List<Comment> result = new ArrayList<>();
        for (Comment c : readAll()) {
            if (appointmentId.equals(c.getAppointmentId())) {
                result.add(c);
            }
        }
        return result;
    }
}
