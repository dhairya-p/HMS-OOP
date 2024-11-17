package repository;

import entity.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Base repository interface
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void delete(ID id);
    boolean exists(ID id);
    void clearAll(); 
}
