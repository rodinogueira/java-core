public class Main {
    public static void main(String[] args) {
        // 1. Create the implementation (The "Weapon")
        Repository db = new SqliteRepository();

        // 2. Inject it into the Service (Mission Control)
        MissionControl control = new MissionControl(db);

        // 3. Create a New Target (The Point)
        Point alpha = new Point(1, "Station", 4);

        // 4. Execute the operation
        control.update(alpha);
    }
}