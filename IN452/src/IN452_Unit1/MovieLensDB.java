package IN452_Unit1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A class to interact with a MovieLens database.
 * Assumes a database schema with 'movies' and 'ratings' tables.
 */
public class MovieLensDB {

    // Private field to hold the database connection string.
    private final String connectionString;

    /**
     * Default constructor.
     * Initializes the database with a default connection string for a local SQL Server.
     */
    public MovieLensDB() {
        // Using the recommended semicolon-separated format for clarity and compatibility.
        this.connectionString = "jdbc:sqlserver://localhost:1433;databaseName=IN452;user=sa;password=KELSEY-rogelio-discard;encrypt=false;";
    }

    /**
     * Constructor that takes a custom connection string.
     * @param pConnectionString The full JDBC connection string.
     */
    public MovieLensDB(String pConnectionString) {
        this.connectionString = pConnectionString;
    }

    /**
     * Protected method to establish a database connection.
     * Centralizes the connection logic for reuse and easier testing.
     * @return A database Connection object.
     * @throws SQLException if a database access error occurs.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.connectionString);
    }

    /**
     * Tests the database connection by attempting to establish it.
     * @return true if the connection is successful, false otherwise.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            // If getConnection() doesn't throw an exception, the connection is valid.
            return true;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the total number of movies in the database.
     * @return A string representation of the count of movies.
     */
    public String getMovieCount() {
        String sql = "SELECT COUNT(*) FROM movies;";
        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return "0";
    }

    /**
     * Retrieves the titles of the first 100 movies in the database.
     * @return A string containing each movie title on a new line.
     */
    public String getMovieTitles() {
        String sql = "SELECT TOP 100 title FROM movies";
        StringBuilder titles = new StringBuilder();

        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                titles.append(rs.getString("title")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return titles.toString();
    }

    /**
     * Retrieves the top 20 highest-rated movies with an average rating above 4.0.
     * @return A formatted string of movie titles and their average ratings.
     */
    public String getTopRatedMovies() {
        String sql = "SELECT TOP 20 m.title, AVG(r.rating) AS average_rating " +
                     "FROM movies m " +
                     "JOIN ratings r ON m.movieId = r.movieId " +
                     "GROUP BY m.movieId, m.title " + 
                     "HAVING AVG(r.rating) > 4 " +  
                     "ORDER BY average_rating DESC"; 
        
           
        StringBuilder topMovies = new StringBuilder();

        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String title = rs.getString("title");
                double avgRating = rs.getDouble("average_rating");
                topMovies.append(String.format("%s (Average Rating: %.2f)\n", title, avgRating));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        
        return topMovies.toString();
  
    }

    /**
     * Retrieves all unique genre combinations in the database.
     * @return A string containing each unique genre combination on a new line.
     */
    public String getMovieGenres() {
        String sql = "SELECT DISTINCT genres FROM movies;";
        StringBuilder genres = new StringBuilder();

        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                genres.append(rs.getString("genres")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return genres.toString();
    }
}