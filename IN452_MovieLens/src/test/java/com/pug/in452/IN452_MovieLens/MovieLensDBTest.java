// This is a unit test class for MovieLensDB, using JUnit and Mockito.
// It tests MovieLensDB methods without connecting to a real database by using mock objects.
// Each test sets up a fake database environment and verifies the output of MovieLensDB methods.
// The test class demonstrates best practices for isolated, repeatable unit testing.

package com.pug.in452.IN452_MovieLens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;

/**
 * Unit tests for the MovieLensDB class.
 * 
 * This class uses Mockito to create mock database objects (Connection,
 * Statement, ResultSet) so that tests do not require a real database. Each test
 * sets up the expected behavior of these mocks and verifies that MovieLensDB
 * methods produce the correct output.
 * 
 * The @BeforeEach method initializes the mocks and injects them into
 * MovieLensDB. The @AfterEach method cleans up after each test.
 */
class MovieLensDBTest {
	// The MovieLensDB instance under test. Uses a mock connection for isolation.
	private MovieLensDB underTest;// Instance of the class being tested
	// Mocked database connection, statement, and result set for simulating DB
	// operations.
	private Connection mockConnection;// Mock the Connection interface
	private Statement mockStatement;// Mock the Statement interface
	private ResultSet mockResultSet;// Mock the ResultSet interface

	/**
	 * Sets up mock database objects before each test and injects them into
	 * MovieLensDB.
	 */
	@BeforeEach
	void setUp() throws SQLException {
		// Initialize mock objects
		mockConnection = Mockito.mock(Connection.class);// Mock the Connection interface
		mockStatement = Mockito.mock(Statement.class);// Mock the Statement interface
		mockResultSet = Mockito.mock(ResultSet.class);// Mock the ResultSet interface
		// Initialize MovieLensDB with a mock connection for unit testing
		underTest = new MovieLensDB(mockConnection);// Inject the mock connection
	}

	/**
	 * Cleans up the MovieLensDB instance after each test.
	 */
	@AfterEach
	void tearDown() {
		underTest = null;// Release the MovieLensDB instance
	}

	/**
	 * Tests the getMovieTitles() method of MovieLensDB. Sets up the mock objects to
	 * simulate two movies in the result set and verifies that the output matches
	 * the expected format.
	 */
	@Test
	void testGetMovieCount() throws SQLException {
		// Arrange
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock executing a
																								// query
		Mockito.when(mockResultSet.next()).thenReturn(true, true, false);// Simulate two rows in the result set
		Mockito.when(mockResultSet.getInt(1)).thenReturn(1000);// Mock movie count
		// expected output
		int expected = 1000;

		// Act
		int count = Integer.parseInt(underTest.getMovieCount());// Call the method under test

		// Assert
		assertEquals(expected, count);// Assert the count is as expected
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());// Verify the query was executed once
	}

	@Test
	void testGetMovieTitles() throws SQLException {

		// Arrange - set up the test scenario
		// Set up mock behavior for database operations
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock executing a
																								// query
		Mockito.when(mockResultSet.next()).thenReturn(true, true, false);// Simulate two rows in the result set
		Mockito.when(mockResultSet.getString("title")).thenReturn("Movie 1", "Movie 2");// Mock movie titles
		// Expected output format
		String expected = "Movie Titles (Top 50):\nMovie 1\nMovie 2\n";

		// When - this is the action being tested
		// Call the method under test
		String titles = underTest.getMovieTitles();

		// Then - verify the results
		// Assert that the output matches expectations
		assertEquals(expected, titles);
	}

	@Test
	void testGetRatingsCount() throws SQLException {
		// Arrange
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock
		Mockito.when(mockResultSet.next()).thenReturn(true, false);// Simulate one row in the result set
		Mockito.when(mockResultSet.getInt(1)).thenReturn(100000);// Mock
		// expected output
		int expected = 100000;

		// Act
		int count = Integer.parseInt(underTest.getRatingsCount());

		// Assert
		assertEquals(expected, count);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());

	}

	@Test
	void testGetTopRatedMovies() throws Exception {
		// Arrange - set up the test scenario
		// Set up mock behavior for database operations
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock executing a
																								// query
		Mockito.when(mockResultSet.next()).thenReturn(true, true, false);// Simulate two rows in the result set
		Mockito.when(mockResultSet.getString("title")).thenReturn("Movie 1", "Movie 2");// Mock movie titles
		Mockito.when(mockResultSet.getDouble("topRated")).thenReturn(4.8, 4.7);// Mock average ratings
		// Expected output format
		String expected = "Top Rated Movies (Top 20):\n" + "Movie 1 - 4.8\n" + "Movie 2 - 4.7\n";

		// When - this is the action being tested
		// Call the method under test
		String titles = underTest.getTopRatedMovies();

		// Then - verify the results
		// Assert that the output matches expectations
		assertEquals(expected, titles);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());

	}

	@Test
	void testGetTotalUsers() throws SQLException {
		// Arrange
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock
		Mockito.when(mockResultSet.next()).thenReturn(true, false);// Simulate 1 row
		Mockito.when(mockResultSet.getInt(1)).thenReturn(610);// Mock
		// expected output
		int expected = 610;

		// Act
		int count = Integer.parseInt(underTest.getTotalUsers());

		// Assert
		assertEquals(expected, count);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());

	}

	@Test
	void testGetPopularGenres() throws SQLException {
		// Arrange - set up the test scenario
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock executing a
																								// query
		Mockito.when(mockResultSet.next()).thenReturn(true, true, false);// Simulate two rows in the result set
		Mockito.when(mockResultSet.getString("genres")).thenReturn("Drama", "Comedy");// Mock genres
		Mockito.when(mockResultSet.getInt("popGenres")).thenReturn(250, 200);// Mock genre counts
		// Expected output format
		String expected = "Popular Genres (Top 10):\n" + "Drama - 250\n" + "Comedy - 200\n";

		// When - this is the action being tested
		// Call the method under test
		String genres = underTest.getPopularGenres();

		// Then - verify the results
		// Assert that the output matches expectations
		assertEquals(expected, genres);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());
	}

	@Test
	void testGetTotalTags() throws SQLException {
		// Arrange
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock
		Mockito.when(mockResultSet.next()).thenReturn(true, false);// Simulate 1 row
		Mockito.when(mockResultSet.getInt(1)).thenReturn(1250);// Mock
		// expected output
		int expected = 1250;

		// Act
		int count = Integer.parseInt(underTest.getTotalTags());

		// Assert
		assertEquals(expected, count);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());
	}

	@Test
	void testGetPopularTags() throws SQLException {
		// Arrange - set up the test scenario
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock
		Mockito.when(mockResultSet.next()).thenReturn(true, true, false);// Simulate two rows in the result set
		Mockito.when(mockResultSet.getString("popTags")).thenReturn("action", "sci-fi");// Mock tags
		Mockito.when(mockResultSet.getInt("tagCount")).thenReturn(120, 100);// Mock tag counts
		// Expected output format
		String expected = "Popular Tags (Top 15):\n" + "action - 120\n" + "sci-fi - 100\n";

		// When - this is the action being tested
		// Call the method under test
		String tags = underTest.getPopularTags();

		// Then - verify the results
		// Assert that the output matches expectations
		assertEquals(expected, tags);
		verify(mockStatement, times(1)).executeQuery(Mockito.anyString());
	}

	@Test
	public void testHandleSQLException() throws SQLException {
		// Arrange - set up the test scenario
		Mockito.when(mockConnection.createStatement()).thenThrow(new SQLException("DB error"));// Simulate SQL exception

		// When - this is the action being tested
		String result = underTest.getMovieCount();// Call the method under test

		// Then - verify the results
		assertEquals("Error: DB error", result);// Assert error message is returned
		verify(mockConnection, times(1)).createStatement();

	}

	@Test
	public void testConnectionClose() throws SQLException {
		// Arrange - set up the test scenario
		Mockito.when(mockConnection.createStatement()).thenReturn(mockStatement);// Mock creating a statement
		Mockito.when(mockStatement.executeQuery(Mockito.anyString())).thenReturn(mockResultSet);// Mock executing a
																								// query
		Mockito.when(mockResultSet.next()).thenReturn(false);// Simulate no rows in the result set

		// When - this is the action being tested
		underTest.getMovieTitles();

		// Then - verify the results
		verify(mockResultSet, times(1)).close();
		verify(mockStatement, times(1)).close();
		verify(mockConnection, times(1)).close();
	}

	// Integration Test
	@Test

	public void integrationTestWithMovieCountGreaterThanZeroAndNotNull() throws SQLException {
		// Arrange - set up the test scenario

		MovieLensDB realDB = new MovieLensDB("jdbc:sqlserver://localhost:1433;databaseName=IN452;user=IN452_User;"
				+ "password=P@55W0rd!;encrypt=false");// Uses actual DB connection

		// When - this is the action being tested
		String count = realDB.getMovieCount();

		// Check to see if count is greater than 0 and is not null
		assertTrue(count != null && Integer.parseInt(count) > 0);// Check to see if count is greater than 0 and is not
																	// null

	}

	@Test
	//@Disabled("Requires live SQL Server; disable in CI")
	public void integrationTestWithMovieTitleGreaterThanZeroAndNotEmpty() throws SQLException {
		// Arrange - set up the test scenario

		MovieLensDB realDB = new MovieLensDB("jdbc:sqlserver://localhost:1433;databaseName=IN452;user=IN452_User;"
				+ "password=P@55W0rd!;encrypt=false");// Uses actual DB connection

		// When - this is the action being tested
		String count = realDB.getMovieTitles();

		// Check to see if Movie is not null and not empty
		assertTrue(count != null && !count.isEmpty());// Check to see if count is greater than 0 and is not null

	}

}