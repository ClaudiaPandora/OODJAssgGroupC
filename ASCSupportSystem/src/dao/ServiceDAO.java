package dao;

import enums.ServiceType;
import models.Service;
import utils.FileUtils;
import java.util.*;

public class ServiceDAO implements FileDAO<Service> {
    
    private static final String FILE_NAME = "src/data/services.txt";
    
    @Override
    public List<Service> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Service> services = new ArrayList<>();
        for (String line : lines) {
            Service s = Service.fromString(line);
            if (s != null) services.add(s);
        }
        return services;
    }
    
    @Override
    public Service findById(String id) {
        return null;
    }
    
    public Service findByType(ServiceType type) {
        return readAll().stream()
            .filter(s -> s.getType() == type)
            .findFirst()
            .orElse(null);
    }
    
    public double getPrice(ServiceType type) {
        Service service = findByType(type);
        return service != null ? service.getPrice() : 0.0;
    }
    
    @Override
    public boolean save(Service service) {
        List<Service> all = readAll();
        all.add(service);
        return saveAll(all);
    }
    
    @Override
    public boolean update(Service service) {
        List<Service> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getType() == service.getType()) {
                all.set(i, service);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        return false;
    }
    
    private boolean saveAll(List<Service> services) {
        List<String> lines = new ArrayList<>();
        for (Service s : services) {
            lines.add(s.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
}