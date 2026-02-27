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


                        // 2. Tabela de Auditoria (Os Rastros) - O NOVO BLOCO AQUI:
                stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (\n" +
                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    operation TEXT,\n" +
                        "    target_id INTEGER,\n" +
                        "    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP\n" +
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

            pstmt.setString(2, point.getCodename()); // Name é o 2º ?
            pstmt.setInt(3, point.getDangerLevel()); // Level é o 3º ?

            pstmt.executeUpdate();
            // 3. Pegamos o ID que o banco acabou de criar
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    System.out.println("[+] File hardened: " + point.getCodename() + " (ID: " + generatedId + ")");
                    
                    // 4. Agora o rastro no Log tem o ID real do banco!
                    logOperation("INSERT", generatedId);
                }
            }       
        } catch (SQLException e) {
            System.out.println("[+] File hardened: " + point.getCodename() + " implantado.");
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

            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                // Não precisa de GeneratedKeys! O ID você já recebeu lá em cima.
                logOperation("DELETE", id);
                System.out.println("[-] Target eliminated: " + id);
            } else {
                System.out.println("[?] Target not found for elimination: " + id);
            }

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
                logOperation("UPDATE", point.getId());
                System.out.println("[#] Registro ID " + point.getId() + " atualizado para: " + point.getCodename());
            } else {
                System.out.println("[?] ID " + point.getId() + " não encontrado para update.");
            }
        } catch (SQLException e) {
            System.err.println("[!] Erro na reescrita: " + e.getMessage());
        }
    }

    private void logOperation(String operation, int targetId) {
        String sql = "INSERT INTO audit_logs (operation, target_id) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, operation);
            pstmt.setInt(2, targetId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // No hacking corporativo, se o log falha, a gente avisa, mas não para a missão
            System.err.println("[!] Audit Failure: " + e.getMessage());
        }
    }
}