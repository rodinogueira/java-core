public class MissionControl {
    private final Repository repository;

    // Constructor Injection
    public MissionControl(Repository repository) {
        this.repository = repository;
    }

    public void deploy(Point point) {
        if (point.isSafe()) {
            repository.save(point);
            System.out.println("Deployment successful: " + point.getCodename());
        } else {
            System.out.println("ABORT: Point is too dangerous!");
        }
    }

    public void terminate(int id) {
        repository.deleteById(id);
    }

    public void update(Point point) {
        repository.update(point);
    }

}