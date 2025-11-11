package com.pug.in452.IN452_MovieLens;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * QuickRun - small in-memory runner to exercise the recommendation system
 * without a real database. It populates movies and ratings via reflection
 * and runs the benchmark (including optimization) so you can measure runtimes.
 */
public class QuickRun {
    public static void main(String[] args) throws Exception {
        MovieRecommendationSystem sys = new MovieRecommendationSystem("jdbc:mock:dummy");

        // Build small synthetic dataset
        int numMovies = 100;
        int numUsers = 50;
        String[] genresPool = new String[]{"Action", "Comedy", "Drama", "Sci-Fi", "Romance", "Thriller"};
        Random rnd = new Random(42);

        Map<Integer, String> movies = new HashMap<>();
        Map<Integer, List<String>> movieGenres = new HashMap<>();
        Map<Integer, Map<Integer, Double>> userRatings = new HashMap<>();

        for (int m = 1; m <= numMovies; m++) {
            movies.put(m, "Movie " + m);
            List<String> g = new ArrayList<>();
            int gcount = 1 + rnd.nextInt(2);
            for (int k = 0; k < gcount; k++) g.add(genresPool[rnd.nextInt(genresPool.length)]);
            movieGenres.put(m, g);
        }

        for (int u = 1; u <= numUsers; u++) {
            Map<Integer, Double> ratings = new HashMap<>();
            // ensure at least 12 ratings per user (to pass split thresholds)
            int rcount = 12 + rnd.nextInt(9); // 12..20
            for (int i = 0; i < rcount; i++) {
                int mid = 1 + rnd.nextInt(numMovies);
                double rating = 1.0 + rnd.nextInt(5); // 1..5
                ratings.put(mid, rating);
            }
            userRatings.put(u, ratings);
        }

        // inject into private fields via reflection
        Field fMovies = MovieRecommendationSystem.class.getDeclaredField("movies");
        Field fGenres = MovieRecommendationSystem.class.getDeclaredField("movieGenres");
        Field fRatings = MovieRecommendationSystem.class.getDeclaredField("userRatings");
        fMovies.setAccessible(true);
        fGenres.setAccessible(true);
        fRatings.setAccessible(true);
        fMovies.set(sys, movies);
        fGenres.set(sys, movieGenres);
        fRatings.set(sys, userRatings);

        System.out.println("QuickRun: populated synthetic dataset: movies=" + movies.size() + " users=" + userRatings.size());

        // Run benchmark (this will also call runOptimization)
        sys.runBenchmark(1, 5);
    }
}
