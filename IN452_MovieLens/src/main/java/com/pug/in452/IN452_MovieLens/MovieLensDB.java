package com.pug.in452.IN452_MovieLens;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to interact with a MovieLens database.
 * Singleton enforced via private constructors and public getInstance methods.
 */
public class MovieLensDB implements MovieDatabase {

    // Singleton instance
    private static MovieLensDB instance = null;

    // Private field to hold the database connection string.
    private final String connectionString;
    private Connection testConnection = null;

    // Make constructors private to enforce a strict singleton pattern.
    private MovieLensDB(String dbServer, String dbUsername, String dbPassword) {
        this.connectionString = "jdbc:sqlserver://" + dbServer + ";databaseName=IN452;user=" + dbUsername + ";password=" + dbPassword + ";encrypt=false;";
     }

    /**
     * Constructor that takes a custom connection string.
     * @param pConnectionString The full JDBC connection string.
     */
    private MovieLensDB(String pConnectionString) {
        this.connectionString = pConnectionString;
    }
    
    private MovieLensDB(Connection testConnection) {
        this.connectionString = null;
        this.testConnection = testConnection;
    }

    /**
     * Thread-safe singleton accessor using a full JDBC connection string.
     */
    public static synchronized MovieLensDB getInstance(String connectionString) {
        if (instance == null) {
            instance = new MovieLensDB(connectionString);
        }
        return instance;
    }

    /**
     * Thread-safe singleton accessor using server/username/password components.
     */
    public static synchronized MovieLensDB getInstance(String dbServer, String dbUsername, String dbPassword) {
        if (instance == null) {
            instance = new MovieLensDB(dbServer, dbUsername, dbPassword);
        }
        return instance;
    }

    /**
     * Thread-safe singleton accessor for tests that inject a Connection.
     */
    public static synchronized MovieLensDB getInstance(Connection testConnection) {
        if (instance == null) {
            instance = new MovieLensDB(testConnection);
        }
        return instance;
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
     * Retrieves the total number of movies in the database as a human-readable string.
     * Kept for backward compatibility with legacy callers that expect formatted text.
     * @return A string representation of the count of movies.
     */
    public String getMovieCountString() {
        String sql = "SELECT COUNT(*) FROM movies;";
        try (Connection conn = getConnection();
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
     * Retrieves the titles of the first 50 movies in the database as a formatted string.
     */
    public String getMovieTitlesString() {
        String sql = "SELECT TOP 50 title FROM movies ORDER BY movieId ASC;";

        StringBuilder titles = new StringBuilder();
        titles.append("Top 50 Movie Titles:\n\n");
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
     * Retrieves the total number of ratings in the database as a string.
     */
    public String getRatingsCountString() {
        String sql = "select Count(rating) TotalRatings from ratings";
        

        try (Connection conn = getConnection();
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
     * Retrieves the top 20 highest-rated movies with an average rating above 4.0 as a formatted string.
     */
    public String getTopRatedMoviesString() {
        String sql = "select TOP 20 m.title, avg(r.rating) as topRated\n"
                + "from movies m\n"
                + "join ratings r\n"
                + "on m.movieId = r.movieId\n"
                + "Group by m.title\n"
                + "HAVING avg(r.rating) >= 4\n"
                + "order by topRated Desc";
        StringBuilder topRated = new StringBuilder();
        topRated.append("Top Rated Movies (Top 20):\n");

        try (Connection conn = getConnection();
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
     * Retrieves total distinct user in the database as a string (legacy).
     */
    public String getTotalUsersString() {
        String sql = "select count(distinct userid) as totalUsers from ratings;";
            try (Connection conn = getConnection();
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
     * Retrieves Popular Genres with ratings of >= 5 as a formatted string (legacy).
     */
    public String getPopularGenresString() {
        String sql = "select TOP 10 m.genres, count(r.rating) as popGenres\n"
                + "from movies m\n"
                + "join ratings r\n"
                + "on m.movieId = r.movieId \n"
                + "where r.rating >= 5\n"
                + "group by m.genres\n"
                + "order by popGenres desc;";
        StringBuilder popGenres = new StringBuilder();
        popGenres.append("Popular Genres (Top 10):\n");

        try (Connection conn = getConnection();
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
     * Retrieves Total number of tags in the database as a string (legacy).
     */
    public String getTotalTagsString() {
        String sql = "select Count(tag)  totaltags from tags;";
        try (Connection conn = getConnection();
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
     * Retrieves the Top 15 Popular tags in the database as a formatted string (legacy).
     */
    public String getPopularTagsString() {
        String sql = "select TOP 15 tag popTags, count(tag) tagCount\n"
                + "from tags \n"
                + "Group by tag\n"
                + "order by tagCount desc\n;";
        StringBuilder popTags = new StringBuilder();
        popTags.append("Popular Tags (Top 15):\n");

        try (Connection conn = getConnection();
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

    // Typed methods used by adapter

    public int fetchMovieCount() {
        String sql = "SELECT COUNT(*) FROM movies";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String[] fetchMovieTitles(int limit) {
        if (limit <= 0) return new String[0];
        String sql = "SELECT TOP " + limit + " title FROM movies ORDER BY movieId ASC";
        List<String> out = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) out.add(rs.getString("title"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out.toArray(new String[0]);
    }

    public int fetchRatingsCount() {
        String sql = "SELECT COUNT(rating) FROM ratings";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MovieRating[] fetchTopRatedMovies(int limit) {
        if (limit <= 0) return new MovieRating[0];
        String sql = "select TOP " + limit + " m.title, avg(r.rating) as avgRating from movies m join ratings r on m.movieId = r.movieId Group by m.title order by avgRating desc";
        List<MovieRating> out = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new MovieRating(rs.getString("title"), rs.getDouble("avgRating")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out.toArray(new MovieRating[0]);
    }

    public int fetchUserCount() {
        String sql = "select count(distinct userid) from ratings";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public GenreCount[] fetchPopularGenres(int limit) {
        if (limit <= 0) return new GenreCount[0];
        String sql = "select TOP " + limit + " m.genres, count(r.rating) as cnt from movies m join ratings r on m.movieId = r.movieId where r.rating >= 5 group by m.genres order by cnt desc";
        List<GenreCount> out = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String genres = rs.getString(1);
                int cnt = rs.getInt(2);
                // movies.genres can be pipe-separated; split and count each genre separately
                if (genres != null && genres.contains("|")) {
                    String[] parts = genres.split("\\|");
                    for (String p : parts) {
                        out.add(new GenreCount(p.trim(), cnt));
                    }
                } else {
                    out.add(new GenreCount(genres, cnt));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (out.size() > limit) return out.subList(0, limit).toArray(new GenreCount[0]);
        return out.toArray(new GenreCount[0]);
    }

    public int fetchTagsCount() {
        String sql = "select count(tag) from tags";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public TagCount[] fetchPopularTags(int limit) {
        if (limit <= 0) return new TagCount[0];
        String sql = "select TOP " + limit + " tag, count(tag) as cnt from tags group by tag order by cnt desc";
        List<TagCount> out = new ArrayList<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new TagCount(rs.getString(1), rs.getInt(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out.toArray(new TagCount[0]);
    }

    // Implement MovieDatabase interface by delegating to typed fetch methods
    @Override
    public int getMovieCount() {
        return fetchMovieCount();
    }

    @Override
    public String[] getMovieTitles(int limit) {
        return fetchMovieTitles(limit);
    }

    @Override
    public int getRatingsCount() {
        return fetchRatingsCount();
    }

    @Override
    public MovieRating[] getTopRatedMovies(int limit) {
        return fetchTopRatedMovies(limit);
    }

    @Override
    public int getUserCount() {
        return fetchUserCount();
    }

    @Override
    public GenreCount[] getPopularGenres(int limit) {
        return fetchPopularGenres(limit);
    }

    @Override
    public int getTagsCount() {
        return fetchTagsCount();
    }

    @Override
    public TagCount[] getPopularTags(int limit) {
        return fetchPopularTags(limit);
    }

}