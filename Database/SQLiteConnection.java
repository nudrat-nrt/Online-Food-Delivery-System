package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    private static SQLiteConnection instance;
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:sqlite:database/food_delivery.db";

    private SQLiteConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("✅ Database connection established");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    public static SQLiteConnection getInstance() {
        if (instance == null) {
            synchronized (SQLiteConnection.class) {
                if (instance == null) {
                    instance = new SQLiteConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DATABASE_URL);
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database connection: " + e.getMessage());
        }
    }
}