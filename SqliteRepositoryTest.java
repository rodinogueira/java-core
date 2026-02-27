import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SqliteRepositoryTest {

    public static void runTests() {
        System.out.println("[*] Iniciando Protocolo de Teste Genérico...");
        
        try {
            // 1. Definindo o Mapper "na hora" para o teste (Já que PointMapper não existe como classe)
            SqliteRepository.EntityMapper<Point> mapper = new SqliteRepository.EntityMapper<Point>() {
                @Override
                public void mapToSave(PreparedStatement pstmt, Point p) throws SQLException {
                    pstmt.setString(1, p.getCodename());
                    pstmt.setInt(2, p.getDangerLevel());
                }
                @Override
                public void mapToUpdate(PreparedStatement pstmt, Point p) throws SQLException {
                    pstmt.setString(1, p.getCodename());
                    pstmt.setInt(2, p.getDangerLevel());
                    pstmt.setInt(3, p.getId());
                }
                @Override
                public Point mapFromResultSet(ResultSet rs) throws SQLException {
                    return new Point(rs.getInt("id"), rs.getString("name"), rs.getInt("level"));
                }
                @Override public int getEntityId(Point p) { return p.getId(); }
                @Override public String getEntityName(Point p) { return p.getCodename(); }
            };

            // 2. Instanciando o Repositório
            Repository<Point> repo = new SqliteRepository<>("points", mapper);

            // 3. Operação de Inserção
            Point ghost = new Point(0, "Ghost_Test", 9);
            repo.save(ghost);

            // 4. Validação Manual (Substituindo o JUnit)
            List<Point> results = repo.findAll();
            if (results.isEmpty()) throw new RuntimeException("Falha: Banco vazio!");

            Point last = results.get(results.size() - 1);

            if (last.getId() <= 0) throw new RuntimeException("FALHA: ID não gerado!");
            if (!"Ghost_Test".equals(last.getCodename())) throw new RuntimeException("FALHA: Nome corrompido!");

            System.out.println("[V] TESTE GENÉRICO PASSOU: O motor salvou e recuperou o alvo com perfeição.");

        } catch (Exception e) {
            System.err.println("[X] FALHA CRÍTICA NO TESTE: " + e.getMessage());
        }
    }
}