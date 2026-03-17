package bus_ticket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages Oracle DB connections for the application.
 * Throws RuntimeException on connection failure so callers never silently receive null.
 */
public class DBconnection {

    private static final String URL      = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER     = "system";
    private static final String PASSWORD = "Rupasree123";

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC driver not found on classpath.", e);
        }
    }

    /** Returns a fresh connection. Never returns null — throws on failure. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Startup health-check. Prints result and returns false if DB is unreachable. */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}