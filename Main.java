import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // 1. O "Tradutor" - Define como o Point vira SQL e vice-versa
        // 1. O "Tradutor" com todas as cláusulas assinadas
        SqliteRepository.EntityMapper<Point> pointMapper = new SqliteRepository.EntityMapper<Point>() {
            @Override
            public void mapToSave(PreparedStatement pstmt, Point p) throws SQLException {
                pstmt.setString(1, p.getCodename());
                pstmt.setInt(2, p.getDangerLevel());
            }

            @Override
            public void mapToUpdate(PreparedStatement pstmt, Point p) throws SQLException {
                pstmt.setString(1, p.getCodename());
                pstmt.setInt(2, p.getDangerLevel());
                pstmt.setInt(3, p.getId()); // O ID para o WHERE id = ?
            }

            @Override
            public Point mapFromResultSet(ResultSet rs) throws SQLException {
                return new Point(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("level"));
            }

            @Override
            public int getEntityId(Point p) {
                return p.getId();
            }

            @Override
            public String getEntityName(Point p) {
                return p.getCodename();
            }
        };
        // 2. A "Arma" - Agora parametrizada com <Point> e configurada
        Repository<Point> db = new SqliteRepository<>("points", pointMapper);

        // 3. Mission Control (Certifique-se que o construtor dele aceita
        // Repository<Point>)
        MissionControl control = new MissionControl(db);

        // 4. O Alvo (Dica: Use ID 0 para novos alvos, o SQLite gera o resto)
        Point alpha = new Point(0, "Omega", 3);

        // 5. Execução
        control.deploy(alpha);
    }
}