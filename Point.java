public class Point {
    private int id;
    private String codename;
    private int dangerLevel;

    public Point(int id, String codename, int dangerLevel) {
        this.id = id;
        this.codename = codename;
        this.dangerLevel = dangerLevel;
    }
    
    public boolean isSafe() {
        return this.dangerLevel < 4;
    }

    public String getCodename() { return this.codename; }
    public int getDangerLevel() { return this.dangerLevel; }

    public int getId() { return this.id; }
}