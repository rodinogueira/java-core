import java.util.List;

public interface Repository {
    void save(Point point);
    void deleteById(int id);
    List<Point> findAll();
    void update(Point point);
}