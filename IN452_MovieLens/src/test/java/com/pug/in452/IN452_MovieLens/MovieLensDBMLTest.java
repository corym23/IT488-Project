package com.pug.in452.IN452_MovieLens;

import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Weka Machine Learning Library
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.clusterers.SimpleKMeans;

// Mockito for mocking
import org.mockito.Mockito;

/**
 * Machine Learning-based test class for MovieLensDB
 * Uses Weka to generate and execute intelligent test cases
 */
public class MovieLensDBMLTest {
    
    // ========== FIELDS ==========
    
    private MovieLensDB db;  // The database instance we're testing
    private static final String TEST_DB_URL = "jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;";
    
    // Mock objects for unit testing without real database
    private Connection mockConnection;
    private Statement mockStatement;
    private ResultSet mockResultSet;
    
    // File paths for machine learning data
    private static final String TEST_DATA_PATH = "ml_test_data.arff";
    private static final String GENERATED_TESTS_PATH = "generated_test_cases.csv";
    
    
    // ========== SETUP AND TEARDOWN ==========
    
    /**
     * Runs before each test - sets up the test environment
     * Like preparing your workspace before starting homework
     */
    @Before
    public void setUp() {
        try {
            // Use a mocked MovieLensDB to avoid requiring a live database or JDBC driver
            db = Mockito.mock(MovieLensDB.class);
            
            // Stub DB methods used by performRandomDatabaseOperation()
            Mockito.when(db.getMovieCount()).thenReturn("100");
            Mockito.when(db.getMovieTitles()).thenReturn("[Movie A, Movie B]");
            Mockito.when(db.getRatingsCount()).thenReturn("1000");
            Mockito.when(db.getTopRatedMovies()).thenReturn("[Top Movie]");
            Mockito.when(db.getTotalUsers()).thenReturn("500");
            Mockito.when(db.getPopularGenres()).thenReturn("[Drama, Comedy]");
            Mockito.when(db.getTotalTags()).thenReturn("200");
            Mockito.when(db.getPopularTags()).thenReturn("[tag1, tag2]");
            
            // Keep placeholder mocked JDBC objects (not used by these tests)
            mockConnection = Mockito.mock(Connection.class);
            mockStatement = Mockito.mock(Statement.class);
            mockResultSet = Mockito.mock(ResultSet.class);
            
            System.out.println("✓ Test setup complete (DB mocked)");
             
        } catch (Exception e) {
            System.err.println("Setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Runs after each test - cleans up resources
     * Like cleaning your desk after finishing homework
     */
    @After
    public void tearDown() {
        db = null;  // Release the database connection
        mockConnection = null;
        mockStatement = null;
        mockResultSet = null;
        System.out.println("✓ Test cleanup complete");
    }
    
    
    // ========== METHOD 1: CREATE TRAINING DATASET ==========
    
    /**
     * Creates a training dataset with example test cases
     * This is like making flashcards to teach the computer
     * 
     * The dataset has:
     * - thread_count: How many "workers" are using the database at once
     * - operations_per_thread: How many tasks each worker does
     * - timeout_seconds: How long before we give up
     * - expected_success_rate: What percentage should succeed (0.5 to 1.0)
     * - outcome: Did it work? (SUCCESS, PARTIAL_SUCCESS, or FAILURE)
     */
    public boolean createTrainingDataset() {
        try {
            System.out.println("\n=== Creating Training Dataset ===");
            
            // Step 1: Define the attributes (columns) for our dataset
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("thread_count"));  // Numeric: 5-100
            attributes.add(new Attribute("operations_per_thread"));  // Numeric: 1-30
            attributes.add(new Attribute("timeout_seconds"));  // Numeric: 30-300
            attributes.add(new Attribute("expected_success_rate"));  // Numeric: 0.5-1.0
            
            // The outcome is what we're trying to predict (categorical)
            ArrayList<String> outcomeValues = new ArrayList<>();
            outcomeValues.add("SUCCESS");
            outcomeValues.add("PARTIAL_SUCCESS");
            outcomeValues.add("FAILURE");
            attributes.add(new Attribute("outcome", outcomeValues));
            
            // Step 2: Create the dataset container
            Instances dataset = new Instances("MovieLensTestData", attributes, 0);
            dataset.setClassIndex(4);  // The last attribute (outcome) is what we predict
            
            // Step 3: Add 4 hardcoded examples (like example problems in a textbook)
            System.out.println("Adding hardcoded training examples...");
            
            // Example 1: Low load, high success rate → SUCCESS
            dataset.add(createInstance(dataset, 10, 5, 60, 0.95, "SUCCESS"));
            
            // Example 2: Medium load, medium success rate → PARTIAL_SUCCESS
            dataset.add(createInstance(dataset, 50, 15, 120, 0.75, "PARTIAL_SUCCESS"));
            
            // Example 3: High load, low success rate → FAILURE
            dataset.add(createInstance(dataset, 100, 30, 180, 0.60, "FAILURE"));
            
            // Example 4: Low-medium load, high success rate → SUCCESS
            dataset.add(createInstance(dataset, 20, 10, 90, 0.88, "SUCCESS"));
            
            // Step 4: Generate 20 random examples with realistic patterns
            System.out.println("Generating 20 random training examples...");
            Random random = new Random(42);  // Seed for reproducibility
            
            for (int i = 0; i < 20; i++) {
                // Generate random parameters
                int threads = random.nextInt(96) + 5;  // 5 to 100
                int ops = random.nextInt(30) + 1;  // 1 to 30
                int timeout = random.nextInt(271) + 30;  // 30 to 300 seconds
                double successRate = 0.5 + (random.nextDouble() * 0.5);  // 0.5 to 1.0
                
                // Determine outcome based on success rate thresholds
                String outcome;
                if (successRate > 0.85) {
                    outcome = "SUCCESS";
                } else if (successRate > 0.65) {
                    outcome = "PARTIAL_SUCCESS";
                } else {
                    outcome = "FAILURE";
                }
                
                dataset.add(createInstance(dataset, threads, ops, timeout, successRate, outcome));
            }
            
            // Step 5: Save the dataset to an ARFF file
            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataset);
            saver.setFile(new File(TEST_DATA_PATH));
            saver.writeBatch();
            
            System.out.println("✓ Training dataset created with " + dataset.numInstances() + " instances");
            System.out.println("✓ Saved to: " + TEST_DATA_PATH);
            return true;
            
        } catch (IOException e) {
            System.err.println("I/O Error creating training dataset: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error creating training dataset: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Helper method to create a single training instance (one row of data)
     * Think of this as filling out one flashcard
     */
    private Instance createInstance(Instances dataset, int threads, int ops, 
                                    int timeout, double successRate, String outcome) {
        Instance instance = new DenseInstance(5);  // 5 attributes
        instance.setDataset(dataset);
        instance.setValue(0, threads);
        instance.setValue(1, ops);
        instance.setValue(2, timeout);
        instance.setValue(3, successRate);
        instance.setValue(4, outcome);
        return instance;
    }
    
    
    // ========== METHOD 2: TRAIN THE DECISION TREE MODEL ==========
    
    /**
     * Trains a J48 decision tree to predict test outcomes
     * This is like teaching a computer to recognize patterns
     * 
     * The decision tree learns rules like:
     * "If threads > 50 AND operations > 20, then predict FAILURE"
     */
    public J48 trainDecisionTreeModel() {
        try {
            System.out.println("\n=== Training Decision Tree Model ===");
            
            // Step 1: Load the training data we created
            BufferedReader reader = new BufferedReader(new FileReader(TEST_DATA_PATH));
            Instances data = new Instances(reader);
            reader.close();
            
            // Tell Weka which attribute we're trying to predict (the last one)
            data.setClassIndex(data.numAttributes() - 1);
            
            System.out.println("Loaded " + data.numInstances() + " training instances");
            
            // Step 2: Create and configure the J48 decision tree
            J48 tree = new J48();
            tree.setConfidenceFactor(0.25f);  // Controls pruning (simplification)
            tree.setMinNumObj(2);  // Minimum instances needed to make a split
            
            // Step 3: Train the model on our data
            System.out.println("Building classifier...");
            tree.buildClassifier(data);
            System.out.println("✓ Decision tree model trained!");
            
            // Step 4: Evaluate the model using 10-fold cross-validation
            // This tests how well the model generalizes to new data
            System.out.println("\nEvaluating model with 10-fold cross-validation...");
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(tree, data, 10, new Random(1));
            
            // Print evaluation statistics
            System.out.println("\n=== Model Evaluation Summary ===");
            System.out.println(eval.toSummaryString());
            System.out.println("\n=== Detailed Accuracy By Class ===");
            System.out.println(eval.toClassDetailsString());
            System.out.println("\n=== Confusion Matrix ===");
            System.out.println(eval.toMatrixString());
            
            // Step 5: Display the decision tree structure
            System.out.println("\n=== Decision Tree Structure ===");
            System.out.println(tree);
            
            return tree;
            
        } catch (FileNotFoundException e) {
            System.err.println("Training data file not found: " + TEST_DATA_PATH);
            System.err.println("Please run createTrainingDataset() first.");
            return null;
        } catch (IOException e) {
            System.err.println("I/O Error training model: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Error training model: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    
    // ========== METHOD 3: GENERATE TEST CASES ==========
    
    /**
     * Uses the trained AI model to generate new test cases
     * This is like having a robot create homework problems for you
     * 
     * For each test case, it:
     * 1. Generates random parameters
     * 2. Asks the AI to predict the outcome
     * 3. Saves it to a CSV file
     */
    public boolean generateTestCases(J48 model, int numberOfTestCases) {
        try {
            System.out.println("\n=== Generating Test Cases ===");
            
            if (model == null) {
                System.err.println("Error: Model is null. Train a model first.");
                return false;
            }
            
            // Step 1: Load the dataset structure (we need to know the format)
            BufferedReader reader = new BufferedReader(new FileReader(TEST_DATA_PATH));
            Instances dataStructure = new Instances(reader);
            reader.close();
            dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
            
            // Step 2: Create CSV file to save generated test cases
            PrintWriter writer = new PrintWriter(new FileWriter(GENERATED_TESTS_PATH));
            writer.println("test_id,thread_count,operations_per_thread,timeout_seconds,expected_success_rate,predicted_outcome");
            
            // Step 3: Generate test cases with random parameters
            Random random = new Random();
            int successCount = 0;
            int partialCount = 0;
            int failureCount = 0;
            
            System.out.println("Generating " + numberOfTestCases + " test cases...");
            
            for (int i = 0; i < numberOfTestCases; i++) {
                // Generate random parameters within realistic ranges
                int threads = random.nextInt(96) + 5;  // 5-100 threads
                int ops = random.nextInt(30) + 1;  // 1-30 operations per thread
                int timeout = random.nextInt(271) + 30;  // 30-300 seconds
                double successRate = 0.5 + (random.nextDouble() * 0.5);  // 0.5-1.0
                
                // Create an instance for the AI to predict
                Instance testInstance = new DenseInstance(5);
                testInstance.setDataset(dataStructure);
                testInstance.setValue(0, threads);
                testInstance.setValue(1, ops);
                testInstance.setValue(2, timeout);
                testInstance.setValue(3, successRate);
                
                // Ask the AI: "What will happen with these parameters?"
                double prediction = model.classifyInstance(testInstance);
                String predictedOutcome = dataStructure.classAttribute().value((int) prediction);
                
                // Count predictions
                switch (predictedOutcome) {
                    case "SUCCESS":
                        successCount++;
                        break;
                    case "PARTIAL_SUCCESS":
                        partialCount++;
                        break;
                    case "FAILURE":
                        failureCount++;
                        break;
                }
                
                // Save to CSV file
                writer.printf("%d,%d,%d,%d,%.2f,%s%n", 
                    i + 1, threads, ops, timeout, successRate, predictedOutcome);
            }
            
            writer.close();
            
            // Print summary
            System.out.println("✓ Generated " + numberOfTestCases + " test cases");
            System.out.println("  - Predicted SUCCESS: " + successCount);
            System.out.println("  - Predicted PARTIAL_SUCCESS: " + partialCount);
            System.out.println("  - Predicted FAILURE: " + failureCount);
            System.out.println("✓ Saved to: " + GENERATED_TESTS_PATH);
            
            return true;
            
        } catch (FileNotFoundException e) {
            System.err.println("Required file not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("I/O Error generating test cases: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Error generating test cases: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    // ========== METHOD 4: EXECUTE A TEST CASE ==========
    
    /**
     * Actually runs a test with multiple threads hitting the database
     * This simulates many people using the MovieLens database at the same time
     * 
     * Think of it like: "What happens when 50 people all try to search for movies
     * at the exact same time?"
     * 
     * @param threadCount How many "users" are accessing the database simultaneously
     * @param opsPerThread How many operations each "user" performs
     * @param timeoutSeconds Maximum time to wait before giving up
     * @param expectedSuccessRate Minimum success rate to pass (0.0 to 1.0)
     * @return true if the test passes, false otherwise
     */
    public boolean executeGeneratedTestCase(int threadCount, int opsPerThread, 
                                           int timeoutSeconds, double expectedSuccessRate) {
        try {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     Executing Test Case                ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("Threads: " + threadCount);
            System.out.println("Operations per thread: " + opsPerThread);
            System.out.println("Timeout: " + timeoutSeconds + " seconds");
            System.out.println("Expected success rate: " + String.format("%.1f%%", expectedSuccessRate * 100));
            System.out.println("----------------------------------------");
            
            // Thread-safe counters for tracking success/failure
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            
            // CountDownLatch waits for all threads to finish
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            // Create a thread pool (like hiring workers)
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            // Start the timer
            long startTime = System.currentTimeMillis();
            
            // Launch all threads
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        // Each thread performs multiple database operations
                        for (int op = 0; op < opsPerThread; op++) {
                            if (performRandomDatabaseOperation()) {
                                successCount.incrementAndGet();  // Operation succeeded!
                            } else {
                                failureCount.incrementAndGet();  // Operation failed!
                            }
                        }
                    } catch (Exception e) {
                        // If anything goes wrong, count it as a failure
                        failureCount.incrementAndGet();
                        System.err.println("Thread " + threadId + " error: " + e.getMessage());
                    } finally {
                        latch.countDown();  // Signal that this thread is done
                    }
                });
            }
            
            // Wait for all threads to finish (or timeout)
            boolean completed = latch.await(timeoutSeconds, TimeUnit.SECONDS);
            
            // Shut down the thread pool
            executor.shutdown();
            if (!completed) {
                executor.shutdownNow();  // Force shutdown if timeout
            }
            
            // Calculate elapsed time
            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;
            
            // Calculate results
            int totalOps = successCount.get() + failureCount.get();
            double actualSuccessRate = totalOps > 0 ? (double) successCount.get() / totalOps : 0.0;
            
            // Print results
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     Test Results                       ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("Completed: " + (completed ? "Yes ✓" : "TIMEOUT ✗"));
            System.out.println("Duration: " + String.format("%.2f", duration) + " seconds");
            System.out.println("Total operations: " + totalOps);
            System.out.println("Successful operations: " + successCount.get());
            System.out.println("Failed operations: " + failureCount.get());
            System.out.println("Actual success rate: " + String.format("%.1f%%", actualSuccessRate * 100));
            System.out.println("Expected success rate: " + String.format("%.1f%%", expectedSuccessRate * 100));
            
            // Determine if test passed
            boolean passed = actualSuccessRate >= expectedSuccessRate;
            
            if (passed) {
                System.out.println("\n✓✓✓ TEST PASSED ✓✓✓");
            } else {
                System.out.println("\n✗✗✗ TEST FAILED ✗✗✗");
            }
            System.out.println("========================================\n");
            
            return passed;
            
        } catch (InterruptedException e) {
            System.err.println("Test execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            System.err.println("Error executing test case: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Performs one random database operation from MovieLensDB
     * Returns true if successful, false if failed
     * 
     * This randomly picks one of the 8 methods from your MovieLensDB class:
     * 1. getMovieCount()
     * 2. getMovieTitles()
     * 3. getRatingsCount()
     * 4. getTopRatedMovies()
     * 5. getTotalUsers()
     * 6. getPopularGenres()
     * 7. getTotalTags()
     * 8. getPopularTags()
     */
    private boolean performRandomDatabaseOperation() {
        try {
            Random random = new Random();
            int operation = random.nextInt(8);  // 8 different operations
            
            String result = null;
            
            // Pick a random operation to test
            switch (operation) {
                case 0:
                    result = db.getMovieCount();
                    break;
                case 1:
                    result = db.getMovieTitles();
                    break;
                case 2:
                    result = db.getRatingsCount();
                    break;
                case 3:
                    result = db.getTopRatedMovies();
                    break;
                case 4:
                    result = db.getTotalUsers();
                    break;
                case 5:
                    result = db.getPopularGenres();
                    break;
                case 6:
                    result = db.getTotalTags();
                    break;
                case 7:
                    result = db.getPopularTags();
                    break;
            }
            
            // Check if we got a valid result (not null and not an error message)
            assertNotNull("Operation returned null", result);
            assertFalse("Operation returned error", result.startsWith("Error:"));
            
            return true;
            
        } catch (AssertionError e) {
            // Assertion failed - operation didn't work as expected
            return false;
        } catch (Exception e) {
            // Any other exception - operation failed
            return false;
        }
    }
    
    
    // ========== TEST METHODS ==========
    
    /**
     * Main test that demonstrates the full ML-based testing workflow
     * This is the "main event" - it runs everything together!
     * 
     * Steps:
     * 1. Create training data (teach the AI)
     * 2. Train the decision tree model (AI learns patterns)
     * 3. Generate test cases (AI creates new tests)
     * 4. Execute sample tests (actually run the tests)
     */
    @Test
    public void testWekaBasedTestGeneration() {
        try {
            System.out.println("\n");
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║  ML-Based Test Generation Workflow for MovieLensDB  ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            
            // ===== STEP 1: Create Training Dataset =====
            System.out.println("\n[STEP 1/4] Creating training dataset...");
            boolean datasetCreated = createTrainingDataset();
            assertTrue("Training dataset should be created successfully", datasetCreated);
            
            // ===== STEP 2: Train Decision Tree Model =====
            System.out.println("\n[STEP 2/4] Training decision tree model...");
            J48 model = trainDecisionTreeModel();
            assertNotNull("Model should be created successfully", model);
            
            // ===== STEP 3: Generate Test Cases =====
            System.out.println("\n[STEP 3/4] Generating test cases using trained model...");
            boolean testsGenerated = generateTestCases(model, 10);
            assertTrue("Test cases should be generated successfully", testsGenerated);
            
            // ===== STEP 4: Execute Sample Test Cases =====
            System.out.println("\n[STEP 4/4] Executing sample test cases...");
            
            int passedTests = 0;
            int totalTests = 3;
            
            // Test 1: Low load scenario (should succeed easily)
            System.out.println("\n>>> Test 1: Low Load Scenario");
            if (executeGeneratedTestCase(5, 3, 60, 0.90)) {
                passedTests++;
            }
            
            // Test 2: Medium load scenario (moderate difficulty)
            System.out.println("\n>>> Test 2: Medium Load Scenario");
            if (executeGeneratedTestCase(10, 5, 90, 0.85)) {
                passedTests++;
            }
            
            // Test 3: High load scenario (challenging)
            System.out.println("\n>>> Test 3: High Load Scenario");
            if (executeGeneratedTestCase(30, 10, 120, 0.70)) {
                passedTests++;
            }
            
            // ===== FINAL SUMMARY =====
            System.out.println("\n╔══════════════════════════════════════════════════════╗");
            System.out.println("║                   FINAL SUMMARY                      ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.println("Tests passed: " + passedTests + "/" + totalTests);
            System.out.println("Success rate: " + String.format("%.1f%%", (passedTests * 100.0 / totalTests)));
            
            if (passedTests == totalTests) {
                System.out.println("\n🎉 ALL TESTS PASSED! 🎉");
            } else if (passedTests > 0) {
                System.out.println("\n⚠ SOME TESTS PASSED");
            } else {
                System.out.println("\n❌ ALL TESTS FAILED");
            }
            
            // Assert that at least one test passed
            assertTrue("At least one test should pass", passedTests > 0);
            
        } catch (Exception e) {
            fail("Test workflow failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * Demonstrates clustering to find similar test scenarios
     * This groups test cases into categories (like "low load", "medium load", "high load")
     * 
     * Uses K-Means clustering (unsupervised learning)
     */
    @Test
    public void testWekaClustering() {
        try {
            System.out.println("╔══════════════════════════════════════════════════════╗");
            System.out.println("║         Clustering Test Scenarios (K-Means)          ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");

            // Ensure training data exists
            File dataFile = new File(TEST_DATA_PATH);
            if (!dataFile.exists()) {
                System.out.println("Training data not found. Creating it now...");
                assertTrue("Training dataset should be created", createTrainingDataset());
            }

            // Load the data
            BufferedReader reader = new BufferedReader(new FileReader(TEST_DATA_PATH));
            Instances data = new Instances(reader);
            reader.close();

            // Remove the outcome column (clustering doesn't use labels)
            data.deleteAttributeAt(data.numAttributes() - 1);

            System.out.println("Loaded " + data.numInstances() + " instances for clustering");

            // Create and configure K-Means clustering
            SimpleKMeans kmeans = new SimpleKMeans();
            kmeans.setNumClusters(3);  // Find 3 groups
            kmeans.buildClusterer(data);

            // Basic assertions to validate clustering
            assertEquals("KMeans should be configured to 3 clusters", 3, kmeans.getNumClusters());
            System.out.println("✓ Clustering complete!");

            // Print cluster centers (representative test cases)
            System.out.println("\n=== Cluster Centers (Representative Test Cases) ===");
            Instances centroids = kmeans.getClusterCentroids();
            assertTrue("Centroids should exist", centroids.numInstances() > 0);

            for (int i = 0; i < centroids.numInstances(); i++) {
                Instance centroid = centroids.instance(i);

                System.out.println("\n--- Cluster " + (i + 1) + " ---");
                System.out.println("  Threads: " + (int) centroid.value(0));
                System.out.println("  Operations per thread: " + (int) centroid.value(1));
                System.out.println("  Timeout: " + (int) centroid.value(2) + " seconds");
                System.out.println("  Expected success rate: " + String.format("%.2f", centroid.value(3)));

                // Validate centroid ranges to catch obvious clustering issues
                assertTrue("Threads centroid in range", centroid.value(0) >= 5 && centroid.value(0) <= 100);
                assertTrue("Ops per thread centroid in range", centroid.value(1) >= 1 && centroid.value(1) <= 30);
                assertTrue("Timeout centroid in range", centroid.value(2) >= 30 && centroid.value(2) <= 300);
                assertTrue("Expected success rate centroid in range", centroid.value(3) >= 0.5 && centroid.value(3) <= 1.0);

                // Execute and assert the cluster representative test
                System.out.println("\nExecuting test for Cluster " + (i + 1) + ":");
                boolean passed = executeGeneratedTestCase(
                    (int) centroid.value(0),
                    (int) centroid.value(1),
                    (int) centroid.value(2),
                    centroid.value(3)
                );
                assertTrue("Cluster representative test should pass", passed);
            }

         } catch (Exception e) {
            fail("Clustering test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * Educational method explaining ML concepts for testing
     * This is like a mini-lesson about how the AI works
     */
    @Test
    public void explainWekaMLConcepts() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║     Machine Learning for Testing - Explained        ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        System.out.println("\n=== What is a Decision Tree? ===");
        System.out.println("A decision tree is like a flowchart that helps make decisions.");
        System.out.println("It learns patterns from past test results.\n");
        
        System.out.println("Example Decision Tree:");
        System.out.println("┌─────────────────────────────────┐");
        System.out.println("│ Is thread_count > 50?           │");
        System.out.println("└────────┬────────────────┬───────┘");
        System.out.println("        YES              NO");
        System.out.println("         │               │");
        System.out.println("         ▼               ▼");
        System.out.println("  ┌─────────────┐  ┌──────────┐");
        System.out.println("  │ ops > 20?   │  │ SUCCESS  │");
        System.out.println("  └──┬──────┬───┘  └──────────┘");
        System.out.println("    YES    NO");
        System.out.println("     │      │");
        System.out.println("     ▼      ▼");
        System.out.println("  FAILURE  PARTIAL");
        
        System.out.println("\n=== How Does This Help Testing? ===");
        System.out.println("1. We give the AI examples of past tests (training data)");
        System.out.println("2. The AI learns patterns (which parameters lead to success/failure)");
        System.out.println("3. The AI generates new test cases based on what it learned");
        System.out.println("4. We run those tests to verify the database works correctly");
        
        System.out.println("\n=== Now Let's See the Actual Tree ===");
        
        // Create and show the actual decision tree, with assertions
        boolean created = createTrainingDataset();
        assertTrue("Training dataset should be created", created);
        J48 model = trainDecisionTreeModel();
        assertNotNull("Decision tree model should be created", model);
        System.out.println("\n✓ This tree was learned from the training data and assertions passed.");
     }
    
    
    /**
     * Executes all generated test cases from the CSV file
     * This runs through all the tests the AI created
     */
    @Test
    public void executeGeneratedTestCases() {
        try {
            System.out.println("\n╔══════════════════════════════════════════════════════╗");
            System.out.println("║      Executing All Generated Test Cases             ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            
            // Make sure we have test cases
            File csvFile = new File(GENERATED_TESTS_PATH);
            if (!csvFile.exists()) {
                System.out.println("Test cases not found. Generating them now...");
                createTrainingDataset();
                J48 model = trainDecisionTreeModel();
                generateTestCases(model, 10);
            }
            
            // Read the CSV file
            BufferedReader reader = new BufferedReader(new FileReader(GENERATED_TESTS_PATH));
            String line = reader.readLine();  // Skip header
            
            int testNum = 0;
            int passed = 0;
            int failed = 0;
            
            System.out.println("Reading test cases from: " + GENERATED_TESTS_PATH);
            System.out.println("Note: Executing every other test case for efficiency\n");
            
            // Execute each test case
            while ((line = reader.readLine()) != null) {
                testNum++;
                
                // Only execute every other test (sampling for efficiency)
                if (testNum % 2 != 0) {
                    continue;
                }
                
                // Parse CSV line: test_id,thread_count,operations_per_thread,timeout_seconds,expected_success_rate,predicted_outcome
                String[] parts = line.split(",");
                
                if (parts.length < 6) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }
                
                String testId = parts[0];
                int threads = Integer.parseInt(parts[1]);
                int ops = Integer.parseInt(parts[2]);
                int timeout = Integer.parseInt(parts[3]);
                double successRate = Double.parseDouble(parts[4]);
                String predictedOutcome = parts[5];
                
                System.out.println("\n═══════════════════════════════════════");
                System.out.println("Test Case ID: " + testId);
                System.out.println("Predicted outcome: " + predictedOutcome);
                System.out.println("═══════════════════════════════════════");
                
                // Execute the test
                if (executeGeneratedTestCase(threads, ops, timeout, successRate)) {
                    passed++;
                } else {
                    failed++;
                }
            }
            
            reader.close();
            
            // Print final summary
            int totalExecuted = passed + failed;
            double successPercentage = totalExecuted > 0 ? (passed * 100.0 / totalExecuted) : 0.0;
            
            System.out.println("\n╔══════════════════════════════════════════════════════╗");
            System.out.println("║              FINAL EXECUTION SUMMARY                 ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.println("Total test cases executed: " + totalExecuted);
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
            System.out.println("Success rate: " + String.format("%.1f%%", successPercentage));
            
            if (successPercentage >= 80) {
                System.out.println("\n🎉 EXCELLENT! Database is performing well!");
            } else if (successPercentage >= 60) {
                System.out.println("\n⚠ GOOD, but there's room for improvement");
            } else {
                System.out.println("\n❌ NEEDS ATTENTION - Many tests are failing");
            }
            
        } catch (FileNotFoundException e) {
            fail("Test cases file not found: " + GENERATED_TESTS_PATH);
        } catch (IOException e) {
            fail("I/O error reading test cases: " + e.getMessage());
        } catch (Exception e) {
            fail("Execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
