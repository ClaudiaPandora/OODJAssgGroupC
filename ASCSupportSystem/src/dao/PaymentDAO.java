package dao;

import models.Payment;
import utils.FileUtils;
import utils.IDGenerator;
import java.util.*;

public class PaymentDAO implements FileDAO<Payment> {
    
    private static final String FILE_NAME = "src/data/payments.txt";
    
    @Override
    public List<Payment> readAll() {
        List<String> lines = FileUtils.readLines(FILE_NAME);
        List<Payment> payments = new ArrayList<>();
        for (String line : lines) {
            Payment p = Payment.fromString(line);
            if (p != null) payments.add(p);
        }
        return payments;
    }
    
    @Override
    public Payment findById(String id) {
        return readAll().stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Payment> findByAppointmentId(String appointmentId) {
        List<Payment> result = new ArrayList<>();
        for (Payment p : readAll()) {
            if (p.getAppointmentId().equals(appointmentId)) {
                result.add(p);
            }
        }
        return result;
    }
    
    @Override
    public boolean save(Payment payment) {
        List<Payment> all = readAll();
        payment.setId(IDGenerator.generatePaymentID(all.size()));
        all.add(payment);
        return saveAll(all);
    }
    
    @Override
    public boolean update(Payment payment) {
        List<Payment> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(payment.getId())) {
                all.set(i, payment);
                return saveAll(all);
            }
        }
        return false;
    }
    
    @Override
    public boolean delete(String id) {
        List<Payment> all = readAll();
        boolean removed = all.removeIf(p -> p.getId().equals(id));
        if (removed) {
            return saveAll(all);
        }
        return false;
    }
    
    private boolean saveAll(List<Payment> payments) {
        List<String> lines = new ArrayList<>();
        for (Payment p : payments) {
            lines.add(p.toString());
        }
        return FileUtils.writeLines(FILE_NAME, lines);
    }
    
    @Override
    public String getFileName() {
        return FILE_NAME;
    }
    
    public double getTotalRevenue() {
        return readAll().stream().mapToDouble(Payment::getAmount).sum();
    }
    
    public double getRevenueByDate(String date) {
        return readAll().stream()
            .filter(p -> p.getPaymentDate().equals(date))
            .mapToDouble(Payment::getAmount)
            .sum();
    }
}