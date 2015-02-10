import java.sql.*;

// Notice, do not import com.mysql.jdbc.*
// or you will have problems!

public class ConnectionTest {
    public static void main(String[] args) {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = null;
            conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb?user=root&password=cs307team");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Table1;");
            while (rs.next()) {
              System.out.println(rs.getInt("testcol"));
            }
        } catch (Exception ex) {
          System.out.println(ex);
            // handle the error
        }
    }
}