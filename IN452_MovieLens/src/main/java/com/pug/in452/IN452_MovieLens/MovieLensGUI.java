package com.pug.in452.IN452_MovieLens;

	import javax.swing.*;
	import java.awt.*;

	/**
	 * This class creates the user interface for interacting with the MovieLens database.
	 */
	public class MovieLensGUI extends JFrame {

	    // The database access object. It's null until the user connects.
	    private MovieLensDB movieDB;

	    // GUI Components
	    private JButton connectButton;
	    private JButton countButton;
	    private JButton titlesButton;
	    private JButton totalRatingsButton;
	    private JButton topRatedButton;
	    private JButton usersButton;
	    private JButton popGenresButton;
	    private JButton tagsButton;
	    private JButton popTagsButton;
	    private JTextField dbUsername;
	    private JTextField dbPassword;
	    private JTextField dbServer;
	    

	    /**
	     * Constructor: Initializes the GUI components.
	     * Sets up layout, labels, buttons, and action listeners.
	     */
	    public MovieLensGUI() {
	        // --- 1. Create and set up the frame ---
	        super("MovieLens Database Explorer"); // Set window title
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        // Use a 12x2 grid layout with spacing
	        setLayout(new GridLayout(12, 2, 10, 10));


	        // --- 2. Create GUI Components ---
	        connectButton = new JButton("Connect to MovieLens DB");
	        countButton = new JButton("Get Total Movies");
	        titlesButton = new JButton("Get Movie Titles (Top 50)");
	        totalRatingsButton = new JButton("Get Total Ratings");
	        topRatedButton = new JButton("Get Top Rated Movies");
	        usersButton = new JButton("Get Total Users");
	        popGenresButton = new JButton("Get Popular Genres");
	        tagsButton = new JButton("Get Total Tags");
	        popTagsButton = new JButton("Get Popular Tags");
	        dbUsername = new JTextField("IN452_User");
	        dbPassword = new JTextField("P@55W0rd!");
	        dbServer = new JTextField("localhost");
	        

	        // --- 3. Add Components to the Frame ---
	        add(new JLabel("DB Username", SwingConstants.CENTER));
	        add(dbUsername);
	        add(new JLabel("DB Password", SwingConstants.CENTER));
	        add(dbPassword);
	        add(new JLabel("DB Server", SwingConstants.CENTER));
	        add(dbServer);
	        if (dbServer.getText().isEmpty()) {
				dbServer.setText("Please enter a valid server");
			}
	        add(new JLabel("Connect", SwingConstants.CENTER));
	        add(connectButton);
	        add(new JLabel("Movie Count", SwingConstants.CENTER));
	        add(countButton);
	        add(new JLabel("Movie Titles", SwingConstants.CENTER));
	        add(titlesButton);
	        add(new JLabel("Rating Count", SwingConstants.CENTER));
	        add(totalRatingsButton);
	        add(new JLabel("Top Rated Movies", SwingConstants.CENTER));
	        add(topRatedButton);
	        add(new JLabel("User Count", SwingConstants.CENTER));
	        add(usersButton);
	        add(new JLabel("Popular Genres", SwingConstants.CENTER));
	        add(popGenresButton);
	        add(new JLabel("Tags Count", SwingConstants.CENTER));
	        add(tagsButton);
	        add(new JLabel("Popular Tags", SwingConstants.CENTER));
	        add(popTagsButton);


	        // --- 4. Add Action Listeners for each button ---
	        setupActionListeners();

	        // --- 5. Finalize and Display the Frame ---
	        pack(); // Adjusts window size to fit all components
	        setLocationRelativeTo(null); // Center the window on the screen
	        setVisible(true);
	        
	    }

	    /**
	     * Helper method to configure all button action listeners.
	     */
	    private void setupActionListeners() {

	        // Action Listener for the Connect Button
	        connectButton.addActionListener(e -> {
	            movieDB = new MovieLensDB(dbServer.getText(), dbUsername.getText(), dbPassword.getText()); // Initialize with default constructor
	            if (movieDB.testConnection()) {
	                JOptionPane.showMessageDialog(this, "Connection to MovieLens DB successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
	            } else {
	                movieDB = null; // Reset on failure
	                JOptionPane.showMessageDialog(this, "Connection failed. Check console for errors.", "Error", JOptionPane.ERROR_MESSAGE);
	            }
	        });

	        // Action Listener for the Movie Count Button
	        countButton.addActionListener(e -> {
	            if (checkConnection()) {
	                String count = movieDB.getMovieCount();
	                JOptionPane.showMessageDialog(this, "Total Movies: " + count, "Movie Count", JOptionPane.INFORMATION_MESSAGE);
	            }
	        });

	        // Action Listener for the Get Movie Titles Button
	        titlesButton.addActionListener(e -> {
	            if (checkConnection()) {
	                String titles = movieDB.getMovieTitles();
	                showScrollableMessage("Movie Titles", titles);
	                
	            }
	        });

	        // Action Listener for the Get Total Ratings Button
	        totalRatingsButton.addActionListener(e -> {
	            if (checkConnection()) {
	                String ratingsCount = movieDB.getRatingsCount();
	                JOptionPane.showMessageDialog(this, "The total number of ratings is: " + ratingsCount, "Total Ratings", JOptionPane.INFORMATION_MESSAGE);
	            }
	        });

	        // Action Listener for the Get The Top Rated Movies Button
	        topRatedButton.addActionListener(e -> {
	            if (checkConnection()) {
	                String title = movieDB.getTopRatedMovies();
	                showScrollableMessage("Top Rated Movies: ", title);
	            }
	        });
	        // Action Listener for the Get Total Users Button
	        usersButton.addActionListener(e -> {
	        	if (checkConnection()) {
	        		String totUsers = movieDB.getTotalUsers();
	        		JOptionPane.showMessageDialog(this,"Total number of users is: " + totUsers, "User Count", JOptionPane.INFORMATION_MESSAGE);
	        	}
	        });
	        // Action Listener for the Get Popular Genres Button
	        popGenresButton.addActionListener(e -> {
	        	if (checkConnection()) {
	        		String popGenres = movieDB.getPopularGenres();
	        		showScrollableMessage("Popular Genres",popGenres);
	        	}
	        });
	        // Action Listener for the Get Total Tags Button
	        tagsButton.addActionListener(e -> {
	        	if (checkConnection()) {
	        		String totTags = movieDB.getTotalTags();
	        		JOptionPane.showMessageDialog(this, "The total number of tags is: " + totTags, "Total Tags", JOptionPane.INFORMATION_MESSAGE);
	        	}
	        });
	        // Action Listener for the Get Popular Tags Button
	        popTagsButton.addActionListener(e -> {
	        	if (checkConnection()) {
	        		String popTags = movieDB.getPopularTags();
	        		showScrollableMessage("Popular Tags", popTags);
	        	}
	        });
	    }

	    /**
	     * Checks if the database object is initialized. If not, shows an error message.
	     * @return true if connected, false otherwise.
	     */
	    private boolean checkConnection() {
	        if (movieDB == null) {
	            JOptionPane.showMessageDialog(this, "You must connect to the database first!", "Error", JOptionPane.ERROR_MESSAGE);
	            return false;
	        }
	        return true;
	    }

	    /**
	     * Displays a message in a JOptionPane with a scrollable text area.
	     * @param title The title of the dialog window.
	     * @param message The text content to display.
	     */
	    private void showScrollableMessage(String title, String message) {
	        JTextArea textArea = new JTextArea(25, 60); // 25 rows, 60 columns
	        textArea.setText(message);
	        textArea.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(textArea);
	        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
	    }

	    /**
	     * Main method: Entry point for the application.
	     * Creates and displays the GUI frame.
	     */
	    public static void main(String[] args) {
	        // Ensures the GUI is created on the Event Dispatch Thread for thread safety
	        SwingUtilities.invokeLater(() -> new MovieLensGUI());
	    }
	}
