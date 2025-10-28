package com.pug.in452.IN452_MovieLens;

/**
 * DemoMovieLensDB provides lightweight, deterministic, in-memory responses
 * that emulate a MovieLens database. This class is intended for development
 * and UI demonstration where a real JDBC database is not available or
 * desired. It extends MovieLensDB but overrides query methods to return
 * canned strings rather than executing SQL.
 *
 * Usage notes:
 * - This class is not intended for production use.
 * - Methods return human-readable strings formatted similarly to the
 *   real MovieLensDB outputs so the simulator and GUI can display them
 *   without any changes.
 */
public class DemoMovieLensDB extends MovieLensDB {

    /**
     * Default constructor. Calls the parent constructor with an empty
     * connection string. The class overrides all database access methods
     * so the connection string is unused.
     */
    public DemoMovieLensDB() {
        // Call the String constructor with a dummy connection string; we will override methods
        super("");
    }

    /**
     * Returns a demo movie count.
     * @return a short string representing the total number of movies
     */
    @Override
    public String getMovieCount() {
        return "100 (demo)";
    }

    /**
     * Returns a short list of demo movie titles.
     * @return formatted titles string
     */
    @Override
    public String getMovieTitles() {
        return "Movie Titles (demo):\nMovie A\nMovie B\nMovie C\n";
    }

    /**
     * Returns a demo ratings count.
     * @return formatted ratings count
     */
    @Override
    public String getRatingsCount() {
        return "1000 (demo)";
    }

    /**
     * Returns a short demo top-rated movies list.
     * @return formatted top rated movies string
     */
    @Override
    public String getTopRatedMovies() {
        return "Top Rated Movies (demo):\nTop Movie 1 - 4.8\nTop Movie 2 - 4.7\n";
    }

    /**
     * Returns a demo total user count.
     * @return formatted total user count
     */
    @Override
    public String getTotalUsers() {
        return "500 (demo)";
    }

    /**
     * Returns a demo popular genres report.
     * @return formatted popular genres string
     */
    @Override
    public String getPopularGenres() {
        return "Popular Genres (demo):\nDrama - 50\nComedy - 45\n";
    }

    /**
     * Returns a demo total tags count.
     * @return formatted total tags
     */
    @Override
    public String getTotalTags() {
        return "200 (demo)";
    }

    /**
     * Returns a demo list of popular tags.
     * @return formatted popular tags string
     */
    @Override
    public String getPopularTags() {
        return "Popular Tags (demo):\ntag1 - 30\ntag2 - 25\n";
    }
}