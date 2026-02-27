import java.util.List;

public interface Repository<T> {
    void save(T entity);
    List<T> findAll();
    void update(T entity);
    void deleteById(int id);
}