import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/vegetable_prod?useSSL=false&serverTimezone=UTC", "root", "root");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM history LIMIT 1");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            System.out.println("Columns in history table:");
            for (int i = 1; i <= columnCount; i++ ) {
              System.out.println(rsmd.getColumnName(i) + " - " + rsmd.getColumnTypeName(i));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
