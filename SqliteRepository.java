import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteRepository implements Repository {
    private static final String DB_URL = "jdbc:sqlite:deaddrop.db";

    public SqliteRepository() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                Statement stmt = conn.createStatement();
                // Removida a vírgula após INTEGER e removida a coluna status
                stmt.execute("CREATE TABLE IF NOT EXISTS points (\n" +
                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    name TEXT,\n" +
                        "    level INTEGER\n" +
                        ")");
            }
        } catch (Exception e) {
            System.err.println("Critical Failure: " + e.getMessage());
        }
    }

    @Override
    public void save(Point point) {
        // Especificamos as colunas para não sobrar '?' vazio
        String sql = "INSERT INTO points (id, name, level) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, point.getId()); // ID agora é o 1º ?
            pstmt.setString(2, point.getCodename()); // Name é o 2º ?
            pstmt.setInt(3, point.getDangerLevel()); // Level é o 3º ?

            pstmt.executeUpdate();
            System.out.println("[+] File hardened: " + point.getCodename() + " implantado.");

        } catch (SQLException e) {
            System.err.println("[!] Falha no save: " + e.getMessage());
        }
    }

    public List<Point> findAll() {

        List<Point> points = new ArrayList<>();

        String sql = "SELECT id, name, level FROM points";

        try (Connection conn = DriverManager.getConnection(DB_URL);

                Statement stmt = conn.createStatement();

                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                points.add(new Point(rs.getInt("id"), rs.getString("name"), rs.getInt("level")));

            }

        } catch (SQLException e) {

            System.err.println("Erro na leitura: " + e.getMessage());

        }

        return points;

    }

    // 2. DELETE (Apagar os rastros)

    public void deleteById(int id) {

        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);

                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            pstmt.executeUpdate();

            System.out.println("[-] Target eliminated: " + id);

        } catch (SQLException e) {

            System.err.println("Erro ao deletar: " + e.getMessage());

        }
    }

    @Override
    public void update(Point point) {
        // O SQL precisa de todos os campos que você quer mudar
        String sql = "UPDATE points SET name = ?, level = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, point.getCodename()); // Primeiro '?'
            pstmt.setInt(2, point.getDangerLevel()); // Segundo '?'
            pstmt.setInt(3, point.getId()); // O 'WHERE id = ?'

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                System.out.println("[#] Registro ID " + point.getId() + " atualizado para: " + point.getCodename());
            } else {
                System.out.println("[?] ID " + point.getId() + " não encontrado para update.");
            }
        } catch (SQLException e) {
            System.err.println("[!] Erro na reescrita: " + e.getMessage());
        }
    }
}