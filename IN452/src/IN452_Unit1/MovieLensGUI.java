package IN452_Unit1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private JButton topRatedButton;
    private JButton genresButton;

    /**
     * Constructor: Initializes the GUI components.
     * Sets up layout, labels, buttons, and action listeners.
     */
    public MovieLensGUI() {
        // --- 1. Create and set up the frame ---
        super("MovieLens Database Explorer"); // Set window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Use a 5x2 grid layout with spacing
        setLayout(new GridLayout(5, 2, 10, 10));

        // --- 2. Create GUI Components ---
        connectButton = new JButton("Connect to MovieLens DB");
        countButton = new JButton("Movie Count");
        titlesButton = new JButton("Get Movie Titles");
        topRatedButton = new JButton("Get Top Rated Movies");
        genresButton = new JButton("Get Movie Genres");
//        usersButton = new JButton("Get Total Users");
//        popGenresButton = new JButton("Get Popular Genres");
//        tagsButton = new JButton("Get Total Tags");
//        popTagsButton = new JButton("Get Popular Tags");

        // --- 3. Add Components to the Frame ---
        add(new JLabel("Connect:", SwingConstants.CENTER));
        add(connectButton);
        add(new JLabel("Count:", SwingConstants.CENTER));
        add(countButton);
        add(new JLabel("Titles:", SwingConstants.CENTER));
        add(titlesButton);
        add(new JLabel("Top Rated:", SwingConstants.CENTER));
        add(topRatedButton);
        add(new JLabel("Genres:", SwingConstants.CENTER));
        add(genresButton);
//        add(new JLabel("User Count:", SwingConstants.CENTER));
//        add(usersButton);
//        add(new JLabel("Popular Genres:", SwingConstants.CENTER));
//        add(popGenresButton);
//        add(new JLabel("Tags Count:", SwingConstants.CENTER));
//        add(tagsButton);
//        add(new JLabel("Popular Tags:", SwingConstants.CENTER));
//        add(popTagsButton);

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
            movieDB = new MovieLensDB(); // Initialize with default constructor
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
                showScrollableMessage("First 100 Movie Titles", titles);
            }
        });

        // Action Listener for the Get Top Rated Movies Button
        topRatedButton.addActionListener(e -> {
            if (checkConnection()) {
                String topMovies = movieDB.getTopRatedMovies();
                showScrollableMessage("Top Rated Movies", topMovies);
            }
        });

        // Action Listener for the Get Movie Genres Button
        genresButton.addActionListener(e -> {
            if (checkConnection()) {
                String genres = movieDB.getMovieGenres();
                showScrollableMessage("Unique Movie Genres", genres);
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