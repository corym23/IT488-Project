package com.pug.in452.IN452_MovieLens;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * MovieRecommendationSystem
 * 
 * Loads movies and ratings from a SQL Server database and provides
 * content-based, collaborative filtering, and hybrid recommendations.
 * Also provides benchmarking and optimization utilities.
 */
public class MovieRecommendationSystem {

    private final String dbURL;

    // Core data structures
    private final Map<Integer, String> movies = new HashMap<>(); // movieId -> title
    private final Map<Integer, List<String>> movieGenres = new HashMap<>(); // movieId -> genres
    private final Map<Integer, Map<Integer, Double>> userRatings = new HashMap<>(); // userId -> (movieId -> rating)

    // Split data
    private final Map<Integer, Map<Integer, Double>> trainingData = new HashMap<>();
    private final Map<Integer, Map<Integer, Double>> testData = new HashMap<>();

    // Temporary ratings used during evaluation/optimization
    private Map<Integer, Map<Integer, Double>> tempUserRatings = null;

    // Random for reproducible behavior
    private final Random random = new Random(42);

    // Execution times for algorithms
    private final Map<String, Long> executionTime = new HashMap<>();

    public MovieRecommendationSystem(String dbURL) {
        this.dbURL = dbURL;
        // Data structures already initialized
    }

    public void loadData() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbURL);

            // Load movies
            String movieQuery = "SELECT MovieID, Title, Genres FROM movies";
            try (PreparedStatement ps = conn.prepareStatement(movieQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int movieId = rs.getInt("MovieID");
                    String title = rs.getString("Title");
                    String genresStr = rs.getString("Genres");
                    movies.put(movieId, title != null ? title : "(unknown title)");
                    List<String> genres = new ArrayList<>();
                    if (genresStr != null) {
                        for (String g : genresStr.split("\\|")) {
                            String gg = g.trim();
                            if (!gg.isEmpty()) genres.add(gg);
                        }
                    }
                    movieGenres.put(movieId, genres);
                }
            }

            // Load ratings
            String ratingQuery = "SELECT UserID, MovieID, Rating FROM ratings";
            try (PreparedStatement ps = conn.prepareStatement(ratingQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("UserID");
                    int movieId = rs.getInt("MovieID");
                    double rating = rs.getDouble("Rating");
                    userRatings.computeIfAbsent(userId, k -> new HashMap<>()).put(movieId, rating);
                }
            }

            System.out.println("Loaded " + movies.size() + " movies and ratings for " + userRatings.size() + " users.");

        } catch (SQLException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public void splitDataForValidation(double testRatio) {
        trainingData.clear();
        testData.clear();

        for (Map.Entry<Integer, Map<Integer, Double>> e : userRatings.entrySet()) {
            int userId = e.getKey();
            Map<Integer, Double> ratings = e.getValue();
            if (ratings.size() < 10) {
                // skip users with too few ratings
                continue;
            }

            Map<Integer, Double> train = new HashMap<>();
            Map<Integer, Double> test = new HashMap<>();

            for (Map.Entry<Integer, Double> r : ratings.entrySet()) {
                if (random.nextDouble() < testRatio) {
                    test.put(r.getKey(), r.getValue());
                } else {
                    train.put(r.getKey(), r.getValue());
                }
            }

            // Ensure at least one rating in training data
            if (train.isEmpty() && !test.isEmpty()) {
                Map.Entry<Integer, Double> moved = test.entrySet().iterator().next();
                train.put(moved.getKey(), moved.getValue());
                test.remove(moved.getKey());
            }

            if (!train.isEmpty()) {
                trainingData.put(userId, train);
            }
            if (!test.isEmpty()) {
                testData.put(userId, test);
            }
        }

        System.out.println("Split data: training users=" + trainingData.size() + " test users=" + testData.size());
    }

    public void setTemporaryUserRatings(Map<Integer, Map<Integer, Double>> tempRatings) {
        this.tempUserRatings = tempRatings;
    }

    public void resetTemporaryData() {
        this.tempUserRatings = null;
    }

    private Map<Integer, Map<Integer, Double>> getActiveUserRatings() {
        return (tempUserRatings != null) ? tempUserRatings : userRatings;
    }

    public List<Integer> contentBasedRecommendation(int userId, int numRecommendations) {
        long start = System.currentTimeMillis();
        Map<Integer, Map<Integer, Double>> ratingsData = getActiveUserRatings();
        Map<Integer, Double> user = ratingsData.get(userId);
        if (user == null || user.isEmpty()) {
            List<Integer> randoms = getRandomRecommendations(numRecommendations);
            executionTime.put("content", System.currentTimeMillis() - start);
            return randoms;
        }

        // Build genre preference scores
        Map<String, Double> genreScores = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : user.entrySet()) {
            int mid = entry.getKey();
            double rating = entry.getValue();
            double normalized = rating - 2.5; // -2.5 .. +2.5
            List<String> genres = movieGenres.get(mid);
            if (genres == null) continue;
            for (String g : genres) {
                genreScores.put(g, genreScores.getOrDefault(g, 0.0) + normalized);
            }
        }

        // Score unrated movies
        Map<Integer, Double> scores = new HashMap<>();
        for (Map.Entry<Integer, String> mv : movies.entrySet()) {
            int mid = mv.getKey();
            if (user.containsKey(mid)) continue; // skip already rated
            List<String> genres = movieGenres.get(mid);
            double score = 0.0;
            if (genres != null) {
                for (String g : genres) {
                    score += genreScores.getOrDefault(g, 0.0);
                }
            }
            scores.put(mid, score);
        }

        List<Integer> ranked = sortByValueDescending(scores, numRecommendations);
        executionTime.put("content", System.currentTimeMillis() - start);
        return ranked;
    }

    public List<Integer> collaborativeFilteringRecommendation(int userId, int numRecommendations) {
        long start = System.currentTimeMillis();
        Map<Integer, Map<Integer, Double>> ratingsData = getActiveUserRatings();
        Map<Integer, Double> target = ratingsData.get(userId);
        if (target == null || target.isEmpty()) {
            List<Integer> randoms = getRandomRecommendations(numRecommendations);
            executionTime.put("collaborative", System.currentTimeMillis() - start);
            return randoms;
        }

        // Compute similarities
        Map<Integer, Double> similarities = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> e : ratingsData.entrySet()) {
            int otherId = e.getKey();
            if (otherId == userId) continue;
            double sim = calculateUserSimilarity(target, e.getValue());
            if (sim > 0) similarities.put(otherId, sim);
        }

        // Predict ratings for unrated movies
        Map<Integer, Double> predicted = new HashMap<>();
        for (Integer mid : movies.keySet()) {
            if (target.containsKey(mid)) continue; // already rated
            double num = 0.0;
            double den = 0.0;
            for (Map.Entry<Integer, Double> s : similarities.entrySet()) {
                int otherId = s.getKey();
                double sim = s.getValue();
                Map<Integer, Double> otherRatings = ratingsData.get(otherId);
                if (otherRatings != null && otherRatings.containsKey(mid)) {
                    num += sim * otherRatings.get(mid);
                    den += sim;
                }
            }
            if (den > 0) {
                predicted.put(mid, num / den);
            }
        }

        List<Integer> ranked = sortByValueDescending(predicted, numRecommendations);
        // If not enough predictions, fill with randoms
        if (ranked.size() < numRecommendations) {
            List<Integer> fill = getRandomRecommendations(numRecommendations - ranked.size());
            for (Integer id : fill) if (!ranked.contains(id)) ranked.add(id);
        }

        executionTime.put("collaborative", System.currentTimeMillis() - start);
        return ranked;
    }

    public List<Integer> hybridRecommendation(int userId, int numRecommendations, double contentWeight, double collaborativeWeight) {
        long start = System.currentTimeMillis();
        List<Integer> contentList = contentBasedRecommendation(userId, numRecommendations);
        List<Integer> collabList = collaborativeFilteringRecommendation(userId, numRecommendations);

        Map<Integer, Double> contentPosScores = new HashMap<>();
        Map<Integer, Double> collabPosScores = new HashMap<>();

        // Position-based scoring: max 5.0 -> decreasing
        for (int i = 0; i < contentList.size(); i++) {
            int mid = contentList.get(i);
            double score = 5.0 - (i * (5.0 / Math.max(1, numRecommendations)));
            contentPosScores.put(mid, score);
        }
        for (int i = 0; i < collabList.size(); i++) {
            int mid = collabList.get(i);
            double score = 5.0 - (i * (5.0 / Math.max(1, numRecommendations)));
            collabPosScores.put(mid, score);
        }

        Map<Integer, Double> combined = new HashMap<>();
        double wsum = contentWeight + collaborativeWeight;
        if (wsum <= 0) wsum = 1.0;
        Set<Integer> candidates = new HashSet<>();
        candidates.addAll(contentPosScores.keySet());
        candidates.addAll(collabPosScores.keySet());

        for (Integer mid : candidates) {
            double cs = contentPosScores.getOrDefault(mid, 0.0);
            double rs = collabPosScores.getOrDefault(mid, 0.0);
            double combinedScore = (contentWeight * cs + collaborativeWeight * rs) / wsum;
            combined.put(mid, combinedScore);
        }

        List<Integer> ranked = sortByValueDescending(combined, numRecommendations);
        // fill if necessary
        if (ranked.size() < numRecommendations) {
            List<Integer> fill = getRandomRecommendations(numRecommendations - ranked.size());
            for (Integer id : fill) if (!ranked.contains(id)) ranked.add(id);
        }

        executionTime.put("hybrid", System.currentTimeMillis() - start);
        return ranked;
    }

    private double calculateUserSimilarity(Map<Integer, Double> user1Ratings, Map<Integer, Double> user2Ratings) {
        double total = 0.0;
        int count = 0;
        for (Map.Entry<Integer, Double> e : user1Ratings.entrySet()) {
            Integer mid = e.getKey();
            if (user2Ratings.containsKey(mid)) {
                double r1 = e.getValue();
                double r2 = user2Ratings.get(mid);
                double sim = (5.0 - Math.abs(r1 - r2)) / 5.0; // normalized 0..1
                total += sim;
                count++;
            }
        }
        if (count == 0) return 0.0;
        return total / count;
    }

    private List<Integer> getRandomRecommendations(int numRecommendations) {
        List<Integer> pool = new ArrayList<>(movies.keySet());
        Collections.shuffle(pool, random);
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < Math.min(numRecommendations, pool.size()); i++) {
            res.add(pool.get(i));
        }
        return res;
    }

    public void runBenchmark(int userId, int numRecommendations) {
        System.out.println("Running benchmark for user " + userId + " with " + numRecommendations + " recommendations");

        List<Integer> c = contentBasedRecommendation(userId, numRecommendations);
        List<Integer> cf = collaborativeFilteringRecommendation(userId, numRecommendations);
        List<Integer> h = hybridRecommendation(userId, numRecommendations, 0.5, 0.5);

        System.out.println("Content-based recommendations:");
        printMovieList(c);
        System.out.println("Collaborative recommendations:");
        printMovieList(cf);
        System.out.println("Hybrid recommendations:");
        printMovieList(h);

        System.out.println("Execution times (ms): content=" + executionTime.getOrDefault("content", -1L)
                + " collaborative=" + executionTime.getOrDefault("collaborative", -1L)
                + " hybrid=" + executionTime.getOrDefault("hybrid", -1L));

        // Overlap
        int overlapCF = countOverlap(c, cf);
        int overlapCH = countOverlap(c, h);
        int overlapFH = countOverlap(cf, h);
        System.out.println("Overlap: content vs collaborative = " + overlapCF + " (" + percent(overlapCF, numRecommendations) + ")");
        System.out.println("Overlap: content vs hybrid = " + overlapCH + " (" + percent(overlapCH, numRecommendations) + ")");
        System.out.println("Overlap: collaborative vs hybrid = " + overlapFH + " (" + percent(overlapFH, numRecommendations) + ")");
    }

    private void printMovieList(List<Integer> list) {
        for (Integer id : list) {
            System.out.println(" - " + id + " : " + movies.getOrDefault(id, "(unknown)"));
        }
    }

    private int countOverlap(List<Integer> a, List<Integer> b) {
        Set<Integer> sa = new HashSet<>(a);
        int c = 0;
        for (Integer x : b) if (sa.contains(x)) c++;
        return c;
    }

    private String percent(int count, int total) {
        if (total <= 0) return "0%";
        double p = (100.0 * count) / total;
        return String.format("%.1f%%", p);
    }

    private List<Integer> sortByValueDescending(Map<Integer, Double> map, int limit) {
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());
        list.sort(new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return Double.compare(o2.getValue(), o1.getValue());
            }
        });
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, list.size()); i++) out.add(list.get(i).getKey());
        return out;
    }

    private double evaluateHybridRecommendations(double contentWeight, double collaborativeWeight) {
        if (trainingData.isEmpty() || testData.isEmpty()) {
            System.out.println("No training/test split available. Run splitDataForValidation first.");
            return Double.MAX_VALUE;
        }

        // Use temporary trainingData for generating recommendations
        setTemporaryUserRatings(trainingData);

        double totalError = 0.0;
        int count = 0;

        final int numRecs = 100;

        for (Map.Entry<Integer, Map<Integer, Double>> te : testData.entrySet()) {
            int userId = te.getKey();
            Map<Integer, Double> userTestRatings = te.getValue();
            Map<Integer, Double> userTrainRatings = trainingData.get(userId);
            if (userTrainRatings == null || userTrainRatings.size() < 5) continue;

            List<Integer> recs = hybridRecommendation(userId, numRecs, contentWeight, collaborativeWeight);

            for (Map.Entry<Integer, Double> tr : userTestRatings.entrySet()) {
                int mid = tr.getKey();
                double actual = tr.getValue();
                int pos = recs.indexOf(mid);
                double predicted;
                if (pos >= 0) {
                    predicted = 5.0 - (pos * (5.0 / Math.max(1, numRecs)));
                    if (predicted < 0) predicted = 0;
                    if (predicted > 5) predicted = 5;
                } else {
                    predicted = 2.5; // neutral guess when not recommended
                }
                totalError += Math.abs(predicted - actual);
                count++;
            }
        }

        resetTemporaryData();

        if (count == 0) return Double.MAX_VALUE;
        return totalError / count;
    }

    public void optimizeHybridAlgorithm(double[] contentWeights, double[] collaborativeWeights) {
        double bestMAE = Double.MAX_VALUE;
        double bestCW = 0.0;
        double bestRW = 0.0;
        for (double cw : contentWeights) {
            for (double rw : collaborativeWeights) {
                double mae = evaluateHybridRecommendations(cw, rw);
                System.out.println(String.format("Weights content=%.3f collab=%.3f -> MAE=%.4f", cw, rw, mae));
                if (mae < bestMAE) {
                    bestMAE = mae;
                    bestCW = cw;
                    bestRW = rw;
                }
            }
        }
        System.out.println(String.format("Best weights: content=%.3f collab=%.3f with MAE=%.4f", bestCW, bestRW, bestMAE));
    }

    public void runOptimization() {
        // default split 80/20
        splitDataForValidation(0.2);
        double[] contentWeights = new double[] {0.2, 0.4, 0.6, 0.8, 1.0};
        double[] collaborativeWeights = new double[] {0.2, 0.4, 0.6, 0.8, 1.0};
        optimizeHybridAlgorithm(contentWeights, collaborativeWeights);
    }

}
