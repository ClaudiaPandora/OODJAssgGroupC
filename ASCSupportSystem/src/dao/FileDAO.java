package dao;

import java.util.List;

public interface FileDAO<T> {
    List<T> readAll();
    T findById(String id);
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(String id);
    String getFileName();
}