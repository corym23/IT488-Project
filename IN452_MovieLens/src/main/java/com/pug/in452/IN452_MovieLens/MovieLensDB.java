package com.pug.in452.IN452_MovieLens;

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
    private Connection testConnection = null;

    /**
     * Constructor that takes server information, username, and password as Strings.
     * Initializes the database with a connection string for a local SQL Server.
     */
    public MovieLensDB(String dbServer, String dbUsername, String dbPassword) {
        this.connectionString = "jdbc:sqlserver://" + dbServer + ";databaseName=IN452;user=" + dbUsername + ";password=" + dbPassword + ";encrypt=false;";
     }

    /**
     * Constructor that takes a custom connection string.
     * @param pConnectionString The full JDBC connection string.
     */
    public MovieLensDB(String pConnectionString) {
        this.connectionString = pConnectionString;
    }
    
    public MovieLensDB(Connection testConnection) {
        this.connectionString = null;
        this.testConnection = testConnection;
    }

    /**
     * Protected method to establish a database connection.
     * Centralizes the connection logic for reuse and easier testing.
     * @return A database Connection object.
     * @throws SQLException if a database access error occurs.
     */
    protected Connection getConnection() throws SQLException {
        if (testConnection != null) {
            return testConnection;
        }
        // Ensure the SQL Server JDBC driver class is loaded. If the driver jar
        // is missing from the runtime classpath this will report a clear message.
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver class not found: " + e.getMessage() + ".\n" +
                    "Make sure the Microsoft SQL Server JDBC driver JAR is on the application's classpath.");
            // Let DriverManager attempt to find a driver anyway; this exception is only diagnostic.
        }

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
     * @return a string containing each movie title on a new line.
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
     * Retrieves the total number of ratings in the database.
     * @return the total number of ratings in the database.
     */
    public String getRatingsCount() {
        String sql = "select Count(rating) TotalRatings from ratings"; 

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
     * Retrieves the top 20 highest-rated movies with an average rating above 4.0.
     * @return the top 20 highest-rated movies with their average ratings.
     */
    public String getTopRatedMovies() {
        String sql = "select TOP 20 m.title, avg(r.rating) as topRated\n"
        		+ "from movies m\n"
        		+ "join ratings r\n"
        		+ "on m.movieId = r.movieId\n"
        		+ "Group by m.title\n"
        		+ "HAVING avg(r.rating) >= 4\n"
        		+ "order by topRated Desc";
        StringBuilder topRated = new StringBuilder();
        topRated.append("Top Rated Movies (Top 20):\n");
        
    
        try (Connection conn = getConnection(); // MODIFIED to use the helper method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                topRated.append(rs.getString("title")).append(" - ").append(rs.getDouble("topRated")).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
        return topRated.toString();
    }
    /**
     * Retrieves total distinct user in the database.
     * Returns the count of unique users in the ratings table.
     */
    public String getTotalUsers() {
    	String sql = "select count(distinct userid) as totalUsers from ratings;";
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
     * Retrieves Popular Genres with ratings of >= 5.
     * @return the top 10 most common genres with their counts.
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
    	popGenres.append("Popular Genres (Top 10):\n");
    	
   	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			popGenres.append(rs.getString("genres")).append(" - ").append(rs.getInt("popGenres")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return popGenres.toString();
    }
    /**
     * Retrieves Total number of tags in the database.
     * @return the total number of tags in the database.
     */
    public String getTotalTags() {
    	String sql = "select Count(tag)  totaltags from tags;";
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
     * Retrieves the Top 15 Popular tags in the database.
     * @return the top 15 most frequently used tags with their counts.
     */
    public String getPopularTags() {
    	String sql = "select TOP 15 tag popTags, count(tag) tagCount\n"
    			+ "from tags \n"
    			+ "Group by tag\n"
    			+ "order by tagCount desc\n;";
    	StringBuilder popTags = new StringBuilder();
    	popTags.append("Popular Tags (Top 15):\n");
    	

    	
    	try (Connection conn = getConnection(); // MODIFIED to use the helper method
    			Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(sql)) {
    		
    		while (rs.next()) {
    			popTags.append(rs.getString("popTags")).append(" - ").append(rs.getInt("tagCount")).append("\n");
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    		return "Error: " + e.getMessage();
    	}
    	return popTags.toString();
    }
}