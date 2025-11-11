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
 * MovieRecommendationSystem - a simple movie recommendation engine using
 * content-based and collaborative filtering algorithms.
 * 
 * This class connects to a SQL Server database to load movie and user rating
 * data, and provides methods to generate recommendations using different
 * algorithms. It also includes benchmarking and optimization routines to
 * evaluate and tune the recommendation performance.
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

	/**
	 * Loads movie and rating data from the configured SQL Server database.
	 * 
	 * This method populates the in-memory data structures with movies, genres, and
	 * user ratings. It must be called before any recommendation or benchmarking
	 * methods.
	 * 
	 * On success, prints the number of movies and users loaded. On failure, throws
	 * a RuntimeException to surface the error.
	 */
	public void loadData() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(dbURL);

			// Load movies
			String movieQuery = "SELECT MovieID, Title, Genres FROM movies";
			try (PreparedStatement ps = conn.prepareStatement(movieQuery); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int movieId = rs.getInt("MovieID");
					String title = rs.getString("Title");
					String genresStr = rs.getString("Genres");
					movies.put(movieId, title != null ? title : "(unknown title)");
					List<String> genres = new ArrayList<>();
					if (genresStr != null) {
						for (String g : genresStr.split("\\|")) {
							String gg = g.trim();
							if (!gg.isEmpty())
								genres.add(gg);
						}
					}
					movieGenres.put(movieId, genres);
				}
			}

			// Load ratings
			String ratingQuery = "SELECT UserID, MovieID, Rating FROM ratings";
			try (PreparedStatement ps = conn.prepareStatement(ratingQuery); ResultSet rs = ps.executeQuery()) {
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
			// Surface DB connection failures
			throw new RuntimeException("Database load failed", e);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignored) {
				}
		}
	}

	/**
	 * Splits the user rating data into training and test sets for validation.
	 * 
	 * This method partitions the data such that <code>testRatio</code> fraction of
	 * each user's ratings are moved to the test set. The remaining ratings are used
	 * for training.
	 * 
	 * Users with insufficient ratings (fewer than 10) are excluded from the split.
	 * The resulting training and test data can be accessed via the
	 * {@link #trainingData} and {@link #testData} fields, respectively.
	 *
	 * @param testRatio the proportion of each user's ratings to include in the test
	 *                  set
	 */
	public void splitDataForValidation(double testRatio) {
		trainingData.clear();
		testData.clear();

		for (Map.Entry<Integer, Map<Integer, Double>> e : userRatings.entrySet()) {
			int userId = e.getKey();
			Map<Integer, Double> ratings = e.getValue();
			// Require sufficient ratings from real users (original production threshold)
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

	/**
	 * Sets temporary user ratings for evaluation or optimization.
	 * 
	 * This method allows setting a custom set of user ratings that will be used in
	 * place of the actual user ratings for the duration of the evaluation or
	 * optimization. This can be used to test the impact of different ratings
	 * scenarios on the recommendations.
	 *
	 * @param tempRatings a map of user IDs to their corresponding movie ratings
	 */
	public void setTemporaryUserRatings(Map<Integer, Map<Integer, Double>> tempRatings) {
		this.tempUserRatings = tempRatings;
	}

	/**
	 * Resets any temporary data used for evaluation or optimization.
	 * 
	 * This method clears the temporary user ratings, if any were set using
	 * {@link #setTemporaryUserRatings(Map)}.
	 */
	public void resetTemporaryData() {
		this.tempUserRatings = null;
	}

	private Map<Integer, Map<Integer, Double>> getActiveUserRatings() {
		return (tempUserRatings != null) ? tempUserRatings : userRatings;
	}

	/**
	 * Generates content-based recommendations for a user.
	 * 
	 * This method uses the genres of the movies that the user has already rated to
	 * recommend other movies with similar genres. The more a user has rated movies
	 * of certain genres, the stronger the recommendations will be for those genres.
	 * 
	 * If the user has not rated any movies, or if an error occurs, the method falls
	 * back to random recommendations.
	 *
	 * @param userId             the ID of the user for whom to generate
	 *                           recommendations
	 * @param numRecommendations the number of recommendations to generate
	 * @return a list of movie IDs representing the recommended movies
	 */
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
			if (genres == null)
				continue;
			for (String g : genres) {
				genreScores.put(g, genreScores.getOrDefault(g, 0.0) + normalized);
			}
		}

		// Score unrated movies
		Map<Integer, Double> scores = new HashMap<>();
		for (Map.Entry<Integer, String> mv : movies.entrySet()) {
			int mid = mv.getKey();
			if (user.containsKey(mid))
				continue; // skip already rated
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

	/**
	 * Generates collaborative filtering recommendations for a user.
	 * 
	 * This method computes similarities between users based on their rating
	 * patterns and recommends movies that similar users have liked. The more
	 * similar other users found a movie, the higher it will rank in the
	 * recommendations for the target user.
	 * 
	 * If the user has not rated any movies, or if an error occurs, the method falls
	 * back to random recommendations.
	 *
	 * @param userId             the ID of the user for whom to generate
	 *                           recommendations
	 * @param numRecommendations the number of recommendations to generate
	 * @return a list of movie IDs representing the recommended movies
	 */
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
			if (otherId == userId)
				continue;
			double sim = calculateUserSimilarity(target, e.getValue());
			if (sim > 0)
				similarities.put(otherId, sim);
		}

		// Predict ratings for unrated movies
		Map<Integer, Double> predicted = new HashMap<>();
		for (Integer mid : movies.keySet()) {
			if (target.containsKey(mid))
				continue; // already rated
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
			for (Integer id : fill)
				if (!ranked.contains(id))
					ranked.add(id);
		}

		executionTime.put("collaborative", System.currentTimeMillis() - start);
		return ranked;
	}

	/**
	 * Generates hybrid recommendations for a user, combining content-based and
	 * collaborative filtering.
	 * 
	 * This method first generates separate ranked lists from content-based and
	 * collaborative filtering methods, then combines them using configurable
	 * weights to produce a final ranked list. This allows tuning the influence of
	 * each method on the final recommendations.
	 *
	 * @param userId              the ID of the user for whom to generate
	 *                            recommendations
	 * @param numRecommendations  the number of recommendations to generate
	 * @param contentWeight       the weight for content-based scores (higher means
	 *                            more influence)
	 * @param collaborativeWeight the weight for collaborative filtering scores
	 *                            (higher means more influence)
	 * @return a list of movie IDs representing the recommended movies
	 */
	public List<Integer> hybridRecommendation(int userId, int numRecommendations, double contentWeight,
			double collaborativeWeight) {
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
		if (wsum <= 0)
			wsum = 1.0;
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
			for (Integer id : fill)
				if (!ranked.contains(id))
					ranked.add(id);
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
		if (count == 0)
			return 0.0;
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

	/**
	 * Runs the benchmark tests for the recommendation algorithms.
	 * 
	 * This method executes the content-based, collaborative filtering, and hybrid
	 * recommendation methods for the specified user ID and prints the results,
	 * including execution time and recommended movie lists. It also computes and
	 * displays overlap statistics between the different recommendation lists.
	 *
	 * @param userId             the ID of the user for whom to run the benchmark
	 * @param numRecommendations the number of recommendations to generate for each
	 *                           algorithm
	 */
	public void runBenchmark(int userId, int numRecommendations) {
		System.out.println();
		System.out.println("======= MOVIE RECOMMENDATION SYSTEM =======");
		System.out.println();
		System.out.println("1. Loading movie data...");
		System.out.println("Loading data from database...");
		System.out.println("Loaded " + movies.size() + " movies and " + userRatings.size() + " users with ratings");
		System.out.println();
		System.out.println("2. Running algorithm benchmark...");
		System.out.println();
		System.out.println("=== RECOMMENDATION ALGORITHM BENCHMARK ===");
		System.out.println("Testing recommendation algorithms for user ID: " + userId);
		System.out.println();

		List<Integer> c = contentBasedRecommendation(userId, numRecommendations);
		long tContent = getLastExecutionTimeMillis("content");
		System.out.println("\nCONTENT-BASED RECOMMENDATIONS:");
		System.out.println("Execution time: " + tContent + " ms");
		printNumberedMovieList(c);

		List<Integer> cf = collaborativeFilteringRecommendation(userId, numRecommendations);
		long tCollab = getLastExecutionTimeMillis("collaborative");
		System.out.println("\nCOLLABORATIVE FILTERING RECOMMENDATIONS:");
		System.out.println("Execution time: " + tCollab + " ms");
		printNumberedMovieList(cf);

		List<Integer> h = hybridRecommendation(userId, numRecommendations, 0.5, 0.5);
		long tHybrid = getLastExecutionTimeMillis("hybrid");
		System.out.println("\nHYBRID RECOMMENDATIONS:");
		System.out.println("Execution time: " + tHybrid + " ms");
		printNumberedMovieList(h);

		System.out.println();
		System.out.println("=== PERFORMANCE COMPARISON ===");
		System.out.println(String.format("Content-based:        %d ms", tContent));
		System.out.println(String.format("Collaborative:        %d ms", tCollab));
		System.out.println(String.format("Hybrid:               %d ms", tHybrid));

		// Overlap
		int overlapCF = countOverlap(c, cf);
		int overlapCH = countOverlap(c, h);
		int overlapFH = countOverlap(cf, h);
		System.out.println();
		System.out.println("=== RECOMMENDATION OVERLAP ===");
		System.out.println(
				"Content-Collaborative: " + overlapCF + " movies (" + percent(overlapCF, numRecommendations) + ")");
		System.out.println(
				"Content-Hybrid:        " + overlapCH + " movies (" + percent(overlapCH, numRecommendations) + ")");
		System.out.println(
				"Collaborative-Hybrid:  " + overlapFH + " movies (" + percent(overlapFH, numRecommendations) + ")");
		// After benchmark, run optimization to search for best hybrid weights and print
		// results
		runOptimization();

		System.out.println();
		System.out.println("Movie Recommendation System complete!");
	}

	// Helper to print a numbered list of movie titles
	private void printNumberedMovieList(List<Integer> list) {
		for (int i = 0; i < list.size(); i++) {
			int id = list.get(i);
			String title = movies.getOrDefault(id, "(unknown)");
			System.out.println((i + 1) + ". " + title + " (" + id + ")");
		}
	}

	private int countOverlap(List<Integer> a, List<Integer> b) {
		Set<Integer> sa = new HashSet<>(a);
		int c = 0;
		for (Integer x : b)
			if (sa.contains(x))
				c++;
		return c;
	}

	private String percent(int count, int total) {
		if (total <= 0)
			return "0%";
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
		for (int i = 0; i < Math.min(limit, list.size()); i++)
			out.add(list.get(i).getKey());
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
			// Require a reasonable number of training ratings for evaluation
			if (userTrainRatings == null || userTrainRatings.size() < 5)
				continue;

			List<Integer> recs = hybridRecommendation(userId, numRecs, contentWeight, collaborativeWeight);

			for (Map.Entry<Integer, Double> tr : userTestRatings.entrySet()) {
				int mid = tr.getKey();
				double actual = tr.getValue();
				int pos = recs.indexOf(mid);
				double predicted;
				if (pos >= 0) {
					// map position to predicted rating: top -> 5, last -> 0
					predicted = Math.max(0.0, 5.0 - ((double) pos) * (5.0 / Math.max(1, recs.size() - 1)));
				} else {
					// not recommended -> neutral prediction
					predicted = 2.5;
				}
				totalError += Math.abs(predicted - actual);
				count++;
			}
		}

		// restore original ratings
		resetTemporaryData();

		if (count == 0)
			return Double.MAX_VALUE;
		return totalError / count; // MAE
	}

	/**
	 * Optimizes the hybrid recommendation algorithm parameters using grid search.
	 * 
	 * This method tests combinations of content and collaborative filtering weights
	 * to find the values that minimize the mean absolute error (MAE) of the hybrid
	 * recommendations on the test data.
	 * 
	 * The search is performed over the specified ranges of weights, and the best
	 * parameters are printed out.
	 *
	 * @param contentWeights       an array of content-based filtering weights to
	 *                             test
	 * @param collaborativeWeights an array of collaborative filtering weights to
	 *                             test
	 */
	public void optimizeHybridAlgorithm(double[] contentWeights, double[] collaborativeWeights) {
		double bestMAE = Double.MAX_VALUE;
		double bestC = 0.0;
		double bestCol = 0.0;

		System.out.println();
		System.out.println("=== RECOMMENDATION ALGORITHM OPTIMIZATION ===");
		System.out.println("Running grid search for optimal parameters...");
		System.out.println();

		for (double cw : contentWeights) {
			for (double colw : collaborativeWeights) {
				System.out.println(String
						.format("Testing parameters: Content Weight = %.1f, Collaborative Weight = %.1f", cw, colw));
				double mae = evaluateHybridRecommendations(cw, colw);
				if (mae == Double.MAX_VALUE) {
					System.out.println("MAE: (insufficient test data)");
				} else {
					// print MAE using default toString to match expected output
					System.out.println("MAE: " + Double.toString(mae));
					// update best if valid
					if (mae < bestMAE) {
						bestMAE = mae;
						bestC = cw;
						bestCol = colw;
					}
				}
				System.out.println();
			}
		}

		System.out.println("=== OPTIMIZATION RESULTS ===");
		if (bestMAE == Double.MAX_VALUE) {
			System.out.println("Optimization did not find valid MAE (no test data).");
		} else {
			System.out.println("Best parameters found:");
			System.out.println("Content Weight: " + String.format("%.1f", bestC));
			System.out.println("Collaborative Weight: " + String.format("%.1f", bestCol));
			System.out.println("Mean Absolute Error: " + Double.toString(bestMAE));
		}
	}

	/**
	 * Runs the optimization routine for the recommendation algorithms. This method
	 * performs the train/test data split and then runs the hybrid algorithm
	 * optimization to find the best content and collaborative filtering weights.
	 * The results are printed out for review.
	 */
	public void runOptimization() {
		System.out.println();
		System.out.println("3. Running algorithm optimization...");
		System.out.println("Splitting data into training and test sets...");
		// default 20% test split
		splitDataForValidation(0.2);
		System.out.println("Data split complete. " + trainingData.size() + " users in training set and "
				+ testData.size() + " users in test set.");
		// Search weights including 0.0 so optimizer can prefer pure
		// collaborative/content if appropriate
		double[] weights = new double[] { 0.0, 0.2, 0.4, 0.6, 0.8, 1.0 };
		optimizeHybridAlgorithm(weights, weights);
	}

	// Retrieve last execution time for a given algorithm
	public long getLastExecutionTimeMillis(String key) {
		return executionTime.getOrDefault(key, -1L);
	}

	public static void main(String[] args) {
		MovieRecommendationSystem sys;
		int userId = 1;
		int numRecs = 5;
		// Use a hard-coded JDBC URL.
		String url = "jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;";
		System.out.println("Using hard-coded JDBC URL for database connection.");

		if (args.length >= 2) {
			try {
				userId = Integer.parseInt(args[1]);
			} catch (NumberFormatException ignored) {
			}
		}
		if (args.length >= 3) {
			try {
				numRecs = Integer.parseInt(args[2]);
			} catch (NumberFormatException ignored) {
			}
		}

		sys = new MovieRecommendationSystem(url);
		sys.loadData();
		sys.runBenchmark(userId, numRecs);
	}

}