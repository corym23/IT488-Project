package IN452_Unit1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SQLServerJDBCTest {
	public static void main(String[] args) {
		
		String url = "jdbc:sqlserver://localhost:1433;IN452=sa;encrypt=false";
        String user = "IN452_User";
        String password = "P@55W0rd!";

        try {
            // Load the JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Connect to the database
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println(url+user+password);
            System.out.println("Connection successful!");

            // Run a query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT @@VERSION");

            while (rs.next()) {
                System.out.println("SQL Server Version: " + rs.getString(1));
            }

            // Clean up
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
		
	}
