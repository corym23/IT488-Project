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
     * Constructor that takes username and password as Strings.
     * Initializes the database with a connection string for a local SQL Server.
     */
    public MovieLensDB(String dbUsername, String dbPassword) {
        this.connectionString = "jdbc:sqlserver://localhost:1433;databaseName=IN452;user=" + dbUsername + ";password=" + dbPassword + ";encrypt=false;";
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
     * Retrieves the titles of the first 50 movies in the database.
     * @return A string containing each movie title on a new line.
     */
    public String getMovieTitles() {
        String sql = "SELECT TOP 50 title FROM movies ORDER BY movieId ASC;";
        StringBuilder titles = new StringBuilder();
        titles.append("Movie Titles (Top 50):\n");
        try (Connection conn = getConnection();
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
     * Returns the top 20 highest-rated movies with their average ratings
     */
    public String getRatingsCount() {
        String sql = "select Count(rating) TotalRatings from ratings"; 
        System.out.println(sql);
        
           
        StringBuilder totRatings = new StringBuilder();
        totRatings.append("Top Rated Movies:\n");

        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int totalRatings = rs.getInt("TotalRatings");
                totRatings.append(String.format("%s (The Total number of ratings is:  d%n)\n", totalRatings));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        
        return totRatings.toString();
  
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
    /**
     * Retrieves total distinct user in the database.
     * Returns the count of unique users in the ratings table.
     */
    public String getTotalUsers() {
    	String sql = "select count(distinct userid) as totalUsers from ratings;";
    	StringBuilder userid = new StringBuilder();
    	
    	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			userid.append(rs.getString("totalUsers")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return userid.toString();
    }
    /**
     * Retrieves Popular Genres with ratings of 10.
     * Returns the top 10 most common genres with their counts.
     */
    public String getPopularGenres() {
    	String sql = "select TOP 10 m.genres, count(r.rating) as popGenres\n"
    			+ "from movies m\n"
    			+ "join ratings r\n"
    			+ "on m.movieId = r.movieId \n"
    			+ "where r.rating >= 5\n"
    			+ "group by m.genres\n"
    			+ "order by popGenres desc;";
    	StringBuilder popGenres = new StringBuilder();
    	
   	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			popGenres.append(rs.getString("genres")).append(" - ").append(rs.getString("popGenres")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return popGenres.toString();
    }
    /**
     * Retrieves Total number of tags in the database.
     * Returns the total number of tags in the database.
     */
    public String getTotalTags() {
    	String sql = "select Count(tag)  totaltags from tags;";
    	StringBuilder totalTags = new StringBuilder();
    	
    	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			totalTags.append(rs.getString("totaltags")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return totalTags.toString();
    }
    /**
     * Retrieves the Top 15 Popular tags in the database.
     * Returns the top 15 most frequently used tags with their counts.
     */
    public String getPopularTags() {
    	String sql = "select TOP 15 tag popTags, count(tag) tagCount\n"
    			+ "from tags \n"
    			+ "Group by tag\n"
    			+ "order by tagCount desc\n;";
    	StringBuilder popTags = new StringBuilder();
    	
    	System.out.println(sql);
    	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			popTags.append(rs.getString("popTags")).append(" - ").append(rs.getString("tagCount")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return popTags.toString();
    }
}