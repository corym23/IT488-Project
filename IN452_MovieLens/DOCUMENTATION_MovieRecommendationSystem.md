MovieRecommendationSystem — Documentation

Overview

The MovieRecommendationSystem class is a standalone CLI utility that loads movie metadata and user ratings from a Microsoft SQL Server database and produces three types of recommendations for a given user:

- Content-based recommendations (genre preference scoring)
- Collaborative filtering recommendations (user-based similarity)
- Hybrid recommendations (combines content + collaborative rankings)

The class also provides benchmarking and a simple grid-search optimization routine to choose hybrid weighting parameters using a training/test split and Mean Absolute Error (MAE) as the evaluation metric.

Location

Source: src/main/java/com/pug/in452/IN452_MovieLens/MovieRecommendationSystem.java

Main responsibilities

- Connect to a SQL Server database and load movies (MovieID, Title, Genres) and ratings (UserID, MovieID, Rating).
- Provide recommendation methods:
  - contentBasedRecommendation(userId, numRecommendations)
  - collaborativeFilteringRecommendation(userId, numRecommendations)
  - hybridRecommendation(userId, numRecommendations, contentWeight, collaborativeWeight)
- Benchmark those methods and print a human-readable report (execution times, overlaps).
- Split data into training/test sets and run a grid search to optimize hybrid weights (optimizeHybridAlgorithm).

Prerequisites

- JDK 17 (project configured for Java 17)
- Maven (to build and run with dependencies)
- A running Microsoft SQL Server instance containing tables expected by the code:
  - movies table with columns: MovieID, Title, Genres
  - ratings table with columns: UserID, MovieID, Rating
- SQL Server JDBC driver is declared as a project dependency (mssql-jdbc) so running via Maven exec:java will include the driver on the classpath.

How it connects to the database

- The class currently uses a hard-coded JDBC connection string inside main():

  jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;

- To change the connection, edit main() in MovieRecommendationSystem.java and set a different URL/credentials.

NOTE: Hard-coded credentials in source are insecure. For any production use, move credentials to a secure location (environment variables, external config, secret store).

Running the program

1) Via Maven (recommended; Maven places the JDBC driver on the classpath):
   mvn -f /path/to/pom.xml exec:java

   The exec plugin in the project is configured to execute MovieRecommendationSystem by default.

2) Directly with java (you must add the JDBC driver to the classpath):
   mvn -f /path/to/pom.xml dependency:copy-dependencies -DincludeArtifactIds=mssql-jdbc -DoutputDirectory=target/dependency
   java -cp target/IN452_MovieLens-0.0.1-SNAPSHOT.jar:target/dependency/* com.pug.in452.IN452_MovieLens.MovieRecommendationSystem [userId] [numRecommendations]

   Examples:
   - Default user and rec count: (no args) => userId=1, numRecs=5
   - Specifying user and number: java -cp ... MovieRecommendationSystem 2 10

CLI behaviour and arguments

- main(String[] args)
  - If no arguments are passed, the class uses the hard-coded JDBC URL and defaults to userId=1 and numRecommendations=5.
  - If two numeric arguments are provided, they are parsed as userId and numRecommendations respectively.

What the program prints

The program prints a multi-section human-readable report. The main sections are:

1) Header and load summary
   - Number of movies loaded and number of users with ratings.

2) Recommendation Algorithm Benchmark
   - For a specified user it prints:
     - CONTENT-BASED recommendations (numbered list + execution time)
     - COLLABORATIVE FILTERING recommendations (numbered list + execution time)
     - HYBRID recommendations (numbered list + execution time)
   - Performance comparison (execution time per algorithm)
   - Overlap statistics between algorithm outputs

3) Algorithm optimization
   - Splits data into training/test sets and runs grid search over weighting parameters.
   - Prints MAE for each parameter combination and records the best parameters found.

Important implementation notes and assumptions

- Data split thresholds (production defaults):
  - Users with fewer than 10 ratings are skipped during train/test split and are not used for optimization.
  - During evaluation of a user (for MAE computation), users must have at least 5 training ratings.

- Similarity metric (collaborative):
  - Uses a simple position-independent similarity computed from rating absolute differences mapped into 0..1: sim = (5.0 - |r1 - r2|)/5.0, averaged over shared items.

- Hybrid combination: position-scoring on ranked lists is used to synthesize content and collaborative lists; combined score = weighted combination of position-based scores.

- Timing: each algorithm sets an executionTime entry (content/collaborative/hybrid) storing the elapsed milliseconds.

- If loadData() fails to connect or read the DB, the method throws a RuntimeException and prints the SQLException stacktrace so failures are visible.

Developer notes and recommended improvements

- Credentials: move the JDBC URL and credentials out of source code. Suggested approaches:
  - Read from environment variables and prefer them over a hard-coded default.
  - Use a properties file outside version control.
  - Use a secret manager for production deployments.

- Configurability: allow the default JDBC URL, userId, number of recommendations, and optimization parameters to be passed via system properties or environment variables.

- Performance: collaborative filtering implementation is O(users * movies) in the current naive approach; consider optimizing via inverted indices, precomputed item similarities, or using an established library if performance becomes a concern.

- Testing: add integration tests that run against a dedicated test database or a local containerized SQL Server instance so the benchmark and optimization can be validated automatically.

Public API (summary of important methods)

- MovieRecommendationSystem(String dbURL)
  Constructor - create instance bound to given JDBC URL.

- void loadData()
  Connects to the DB and loads movies and ratings into memory (movies, movieGenres, userRatings).

- List<Integer> contentBasedRecommendation(int userId, int numRecommendations)
  Returns a ranked list of movie IDs recommended by the content-based algorithm.

- List<Integer> collaborativeFilteringRecommendation(int userId, int numRecommendations)
  Returns a ranked list of movie IDs recommended by collaborative filtering.

- List<Integer> hybridRecommendation(int userId, int numRecommendations, double contentWeight, double collaborativeWeight)
  Returns hybrid recommendations (combined ranking) for the user.

- void splitDataForValidation(double testRatio)
  Splits user ratings into trainingData and testData maps used for optimization/evaluation.

- double evaluateHybridRecommendations(double contentWeight, double collaborativeWeight)
  Internal evaluator that computes MAE over testData given hybrid weights.

- void optimizeHybridAlgorithm(double[] contentWeights, double[] collaborativeWeights)
  Runs a grid search over provided weight arrays and prints MAE results and best found parameters.

- void runBenchmark(int userId, int numRecommendations)
  Runs the benchmark and prints the formatted human-readable report.

- void runOptimization()
  Coordinates data splitting and optimization run, printing progress and results.

Contact / Next steps

If you want, I can:
- Replace the hard-coded JDBC string with an environment-variable-based lookup and update the POM to document the variable name; or
- Remove the hard-coded credentials and read them from the POM property (jdbc.url) so overrides via -Djdbc.url work; or
- Generate a short README snippet describing how to run the class with your specific DB host/credentials.

Choose one of the above and I will implement it.
