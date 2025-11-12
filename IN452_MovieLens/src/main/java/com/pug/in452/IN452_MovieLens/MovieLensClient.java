package com.pug.in452.IN452_MovieLens;

/**
 * MovieLensClient
 * ----------------
 * CLI client that demonstrates usage of the MovieLensAdapter to query a
 * live MovieLens-style SQL Server database and print summary information.
 * 
 * Overview of operations performed:
 *  - The client constructs a MovieLensAdapter with a JDBC URL (either the
 *    first command-line argument or a default hard-coded URL).
 *  - It then calls the adapter to retrieve typed domain objects for:
 *      * total movie count (int)
 *      * top-rated movies (MovieRating[])
 *      * popular genres (GenreCount[])
 *      * popular tags (TagCount[])
 *  - The output is printed.
 */
public class MovieLensClient {
    public static void main(String[] args) {
        // Determine JDBC URL to use. Priority:
        // 1) First command line argument (if provided)
        // 2) Default hard-coded URL (for local dev/test environments)
        String url = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty())
                ? args[0]
                : "jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;";

        try {
            // The adapter provides a clean interface and converts results
            // into domain objects used by this client.
            MovieLensAdapter adapter = new MovieLensAdapter(url);

            // 1) Movie count - small, cheap query returning an int
            int movieCount = adapter.getMovieCount();
            System.out.println("Movie Count: " + movieCount);

            // 2) Top-rated movies - fetch N top entries
            System.out.println();
            System.out.println("Top 5 Movies:");
            MovieRating[] top = adapter.getTopRatedMovies(5);
            if (top == null) {
                // If adapter contract ever returns null, handle gracefully by
                // logging an error. In production we might prefer an exception
                // to be thrown by the adapter to highlight the failure.
                System.err.println("No top-rated movies returned by the database.");
            } else {
                for (MovieRating r : top) {
                    // MovieRating provides getTitle() and getRating() (immutable POJO)
                    System.out.println(r.getTitle() + " - " + r.getRating());
                }
            }

            // 3) Popular genres - adapter returns GenreCount[] where each element
            // contains a genre string and a count.
            System.out.println();
            System.out.println("Popular Genres:");
            GenreCount[] genres = adapter.getPopularGenres(5);
            if (genres == null) {
                System.err.println("No popular genres returned by the database.");
            } else {
                for (GenreCount g : genres) {
                    System.out.println(g.getGenre() + " - " + g.getCount());
                }
            }

            // 4) Popular tags - adapter returns TagCount[] (tag string and count)
            System.out.println();
            System.out.println("Popular Tags:");
            TagCount[] tags = adapter.getPopularTags(5);
            if (tags == null) {
                System.err.println("No popular tags returned by the database.");
            } else {
                for (TagCount t : tags) {
                    System.out.println(t.getTag() + " - " + t.getCount());
                }
            }

        } catch (Exception e) {
            // Any unrecoverable error is reported and the process exits non-zero.
            // Keep the trace to aid debugging (stack trace includes SQL state).
            System.err.println("Error querying the database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}