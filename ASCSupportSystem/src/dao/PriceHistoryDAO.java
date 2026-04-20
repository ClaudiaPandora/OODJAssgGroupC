package dao;

import models.PriceHistory;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class PriceHistoryDAO implements FileDAO<PriceHistory> {
    
    private static final String FILE_NAME = "src/data/price_history.txt";
    
    public PriceHistoryDAO() {
        ensureFileExists();
    }
    
    private void ensureFileExists() {
        FileUtils.readLines(FILE_NAME);
    }
    
    @Override
    public List<PriceHistory> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<PriceHistory> history = new ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                PriceHistory ph = PriceHistory.fromString(line);
                if (ph != null) history.add(ph);
            }
        }
        return history;
    }
    
    public List<PriceHistory> readAllSortedByDate() {
        List<PriceHistory> history = readAll();
        history.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return history;
    }
    
    @Override
    public PriceHistory findById(String id) {
        return readAll().stream()
            .filter(h -> h.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    @Override
    public boolean save(PriceHistory history) {
        List<PriceHistory> all = readAll();
        if (history.getId() == null || history.getId().isEmpty()) {
            history.setId(IDGenerator.generateID("PH", all.size()));
        }
        all.add(history);
        return saveAll(all);
    }
    
    @Override
    public boolean update(PriceHistory history) {
        List<PriceHistory> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(history.getId())) {
                all.set(i, history);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<PriceHistory> all = readAll();
        boolean removed = all.removeIf(h -> h.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<PriceHistory> history) {
        List<String> lines = new ArrayList<>();
        for (PriceHistory h : history) {
            lines.add(h.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}