import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteRepository<T> implements Repository<T> {
    private static final String DB_URL = "jdbc:sqlite:deaddrop.db";

    // O segredo: uma interface interna para mapear os dados
    public interface EntityMapper<T> {
        void mapToSave(PreparedStatement pstmt, T entity) throws SQLException;

        void mapToUpdate(PreparedStatement pstmt, T entity) throws SQLException;

        T mapFromResultSet(ResultSet rs) throws SQLException;

        int getEntityId(T entity);

        String getEntityName(T entity);
    }

    private final EntityMapper<T> mapper;
    private final String tableName;

    public SqliteRepository(String tableName, EntityMapper<T> mapper) {

        this.tableName = tableName;
        this.mapper = mapper;

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
    public void save(T entity) {
        String sql = "INSERT INTO " + tableName + " (name, level) VALUES (?, ?)";

        // ADICIONADO: Statement.RETURN_GENERATED_KEYS é obrigatório aqui!
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            mapper.mapToSave(pstmt, entity);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    System.out.println("[+] Record hardened in " + tableName + " (ID: " + generatedId + ")");
                    logOperation("INSERT", generatedId);
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Falha no save em " + tableName + ": " + e.getMessage());
        }
    }

    @Override
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        // Usamos a variável tableName para ser universal
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // O Mapper faz a mágica: ele conhece a classe real (Point, Agent, etc.)
                entities.add(mapper.mapFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("[!] Erro na leitura de " + tableName + ": " + e.getMessage());
        }

        return entities;
    }

    // 2. DELETE (Apagar os rastros)

    @Override
    public void deleteById(int id) {
        // CORRIGIDO: Agora usa o tableName para ser genérico de verdade
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                logOperation("DELETE", id);
                System.out.println("[-] Target eliminated from " + tableName + ": " + id);
            } else {
                System.out.println("[?] Target " + id + " not found in " + tableName);
            }
        } catch (SQLException e) {
            System.err.println("[!] Erto ao deletar em " + tableName + ": " + e.getMessage());
        }
    }

    @Override
    public void update(T entity) {
        // 1. ADICIONE ESPAÇOS: "UPDATE " + tableName + " SET ..."
        String sql = "UPDATE " + tableName + " SET name = ?, level = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 2. O Mapper preenche os 3 parâmetros: Name, Level e o ID para o WHERE
            mapper.mapToUpdate(pstmt, entity);

            int affected = pstmt.executeUpdate();

            if (affected > 0) {
                // Pegamos as informações através do mapper para o Log
                int id = mapper.getEntityId(entity);
                logOperation("UPDATE", id);
                System.out.println("[#] [" + tableName + "] ID " + id + " atualizado com sucesso.");
            } else {
                System.out.println("[?] ID " + mapper.getEntityId(entity) + " não encontrado em " + tableName);
            }
        } catch (SQLException e) {
            System.err.println("[!] Erro na reescrita em " + tableName + ": " + e.getMessage());
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