package com.pug.in452.IN452_MovieLens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class creates the user interface for interacting with the MovieLens
 * database.
 */
public class MovieLensGUI extends JFrame {

	// The database access object. It's null until the user connects.
	private MovieDatabase movieDB;

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
	private JPasswordField dbPassword; // changed to JPasswordField
	private JTextField dbServer;

	// New components for password strength feedback
	private JProgressBar passwordStrengthBar;
	private JLabel passwordStrengthLabel;
	private JTextArea passwordFeedbackArea; // added to show detailed feedback
	private JScrollPane passwordFeedbackScroll; // scroll pane for feedback, hidden until needed
	private JButton helpButton;
	private PasswordDFA passwordValidator;

	/**
	 * Constructor: Initializes the GUI components. Sets up layout, labels, buttons,
	 * and action listeners.
	 */
	public MovieLensGUI() {
		// --- 1. Create and set up the frame ---
		super("MovieLens Database Explorer"); // Set window title
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// We'll use a BorderLayout and compose panels for a cleaner layout
		setLayout(new BorderLayout(10, 10));

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
		dbPassword = new JPasswordField("P@55W0rd!");
		dbServer = new JTextField("localhost");

		// Set default size for all buttons to 285x35
		Dimension defaultButtonSize = new Dimension(285, 35);
		JButton[] allButtons = new JButton[] { connectButton, countButton, titlesButton, totalRatingsButton,
				topRatedButton, usersButton, popGenresButton, tagsButton, popTagsButton };
		for (JButton b : allButtons) {
			b.setPreferredSize(defaultButtonSize);
			b.setMinimumSize(defaultButtonSize);
			b.setMaximumSize(defaultButtonSize);
		}

		// Help button for password requirements
		helpButton = new JButton("Password Help");
		// Password validator
		passwordValidator = new PasswordDFA();

		// --- Top panel: form for username/password and strength indicator ---
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Row 0 - Username label + field
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		topPanel.add(new JLabel("DB Username:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		topPanel.add(dbUsername, gbc);

		// Row 1 - Password label + field + help button
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		topPanel.add(new JLabel("DB Password:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		topPanel.add(dbPassword, gbc);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.weightx = 0.0;
		topPanel.add(helpButton, gbc);

		// Row 2 - Password strength bar directly below password field
		passwordStrengthBar = new JProgressBar(0, 4);
		passwordStrengthBar.setValue(0);
		passwordStrengthBar.setStringPainted(false);
		// Force a BasicProgressBarUI and make opaque so setForeground is honored across
		// LAFs
		passwordStrengthBar.setOpaque(true);
		passwordStrengthBar.setBackground(Color.LIGHT_GRAY);
		// Use a custom UI that paints using the foreground color to avoid LAF-specific
		// blue tint
		passwordStrengthBar.setUI(new ColorProgressBarUI());
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		topPanel.add(passwordStrengthBar, gbc);

		// Row 3 - Strength text label
		passwordStrengthLabel = new JLabel("Password Strength: Not Evaluated");
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		topPanel.add(passwordStrengthLabel, gbc);

		// Row 4 - Feedback area (multiline) - show validator feedback
		passwordFeedbackArea = new JTextArea(3, 40);
		passwordFeedbackArea.setEditable(false);
		passwordFeedbackArea.setLineWrap(true);
		passwordFeedbackArea.setWrapStyleWord(true);
		passwordFeedbackArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		passwordFeedbackArea.setText("Password validation feedback will appear here.");
		passwordFeedbackScroll = new JScrollPane(passwordFeedbackArea);
		passwordFeedbackScroll.setVisible(false); // hide until user types a password
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		topPanel.add(passwordFeedbackScroll, gbc);

		// Row 5 - Server label + field
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		topPanel.add(new JLabel("DB Server:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		topPanel.add(dbServer, gbc);

		// Row 6 - Connect button directly under DB Server (full width over the two input columns)
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		topPanel.add(connectButton, gbc);

		add(topPanel, BorderLayout.NORTH);

		// --- Center panel: main action buttons arranged in a 4X2 grid ---
		JPanel centerPanel = new JPanel(new GridLayout(4, 2, 4, 4));

		// Add the remaining action buttons (exclude connectButton which is now in topPanel)
		centerPanel.add(countButton);
		centerPanel.add(titlesButton);
		centerPanel.add(totalRatingsButton);
		centerPanel.add(topRatedButton);
		centerPanel.add(usersButton);
		centerPanel.add(popGenresButton);
		centerPanel.add(popTagsButton);
		centerPanel.add(tagsButton);

		add(centerPanel, BorderLayout.CENTER);
		// removed the separate southButtons panel so nothing is added to BorderLayout.SOUTH

		if (dbServer.getText().isEmpty()) {
			dbServer.setText("Please enter a valid server");
		}

		// --- 4. Add Action Listeners for each button ---
		setupActionListeners();

		// Key listener on password field to evaluate strength in real time
		dbPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String pwd = new String(dbPassword.getPassword());
				PasswordDFA.PasswordResult res = passwordValidator.validate(pwd);
				int score = res.getStrengthScore();
				passwordStrengthBar.setValue(score);
				passwordStrengthLabel.setText("Password Strength: " + res.getStrengthText());
				// Update feedback area with detailed messages
				if (pwd == null || pwd.isEmpty()) {
					// hide feedback until there is any input
					passwordFeedbackScroll.setVisible(false);
					passwordFeedbackScroll.revalidate();
					passwordFeedbackScroll.repaint();
				} else {
					passwordFeedbackScroll.setVisible(true);
					String fb = res.getFeedback();
					if (fb == null || fb.trim().isEmpty())
						fb = "Password validation feedback will appear here.";
					passwordFeedbackArea.setText(fb);
					// Ensure top-most text is visible
					passwordFeedbackArea.setCaretPosition(0);
					passwordFeedbackScroll.getViewport().setViewPosition(new Point(0, 0));
					int approxCharsPerLine = 60;
					int estimatedLines = Math.max(1, (fb.length() + approxCharsPerLine - 1) / approxCharsPerLine);
					
					// also consider explicit newlines
					int explicitLines = fb.split("\n").length;
					int rows = Math.min(10, Math.max(2, Math.max(estimatedLines, explicitLines)));
					passwordFeedbackArea.setRows(rows);
					passwordFeedbackArea.revalidate();
					passwordFeedbackScroll.revalidate();
					
					// If the top panel exists in scope, revalidate it too so layout updates
					Container parent = passwordFeedbackScroll.getParent();
					if (parent != null)
						parent.revalidate();
				}
				// Color feedback based on validity
				if (res.isValid()) {
					passwordFeedbackArea.setForeground(new Color(0, 120, 0));
				} else {
					passwordFeedbackArea.setForeground(Color.RED.darker());
				}
				// Change color based on score
				Color c;
				switch (score) {
				case 4:
					c = new Color(51, 153, 51);
					break; // dark lime green
				case 3:
					c = new Color(0, 160, 0);
					break; // green
				case 2:
					c = new Color(255, 180, 0);
					break; // amber
				case 1:
					c = new Color(255, 175, 83);
					break; // orange
				case 0:
					c = Color.RED;
					break; // explicit very weak
				case -1:
					c = Color.RED;
					break; // explicit very weak
				default:
					c = Color.RED;
					break;
				}
				// Ensure the progress bar is opaque and uses our custom UI so foreground color
				// is honored
				passwordStrengthBar.setOpaque(true);
				passwordStrengthBar.setBorderPainted(true);
				passwordStrengthBar.setBackground(Color.LIGHT_GRAY);
				passwordStrengthBar.setForeground(c);
				// Reapply custom UI in case LAF replaced it
				passwordStrengthBar.setUI(new ColorProgressBarUI());
				// Ensure UI updates immediately
				passwordStrengthBar.revalidate();
				passwordStrengthBar.repaint();
			}
		});

		// Help button action - show requirements
		helpButton.addActionListener(e -> {
			String helpText = "- At least 8 characters long\n" + "- Must contain at least one digit (0-9)\n"
					+ "- Must contain at least one uppercase letter (A-Z)\n"
					+ "- Must contain at least one lowercase letter (a-z)\n\n" + "Password strength increases with:\n"
					+ "- Use of digits\n" + "- Use of uppercase letters\n" + "- Use of lowercase letters\n"
					+ "- Use of special characters (!@#$%^&*()_-+=<>?/[]{}|~)";
			JOptionPane.showMessageDialog(this, helpText, "Password Requirements", JOptionPane.INFORMATION_MESSAGE);
		});

		// --- Finalize and Display the Frame ---
		pack(); // Adjusts window size to fit all components

		// Set a default window size (override packed size) to 758x760
		setSize(new Dimension(758, 760));

		// Prevent the frame from being resized
		setResizable(false);

		// Re-apply custom UI and initial color after packing (some LAFs reset UI during
		// pack())
		passwordStrengthBar.setUI(new ColorProgressBarUI());
		passwordStrengthBar.setForeground(Color.RED);
		passwordStrengthBar.repaint();

		setLocationRelativeTo(null); // Center the window on the screen
		setVisible(true);
		
	}

	/**
	 * Helper method to configure all button action listeners.
	 */
	private void setupActionListeners() {

		// Action Listener for the Connect Button
		connectButton.addActionListener(e -> {
			try {
				// Use the singleton accessor to obtain the DB instance
				MovieLensDB db = MovieLensDB.getInstance(dbServer.getText(), dbUsername.getText(),
						new String(dbPassword.getPassword()));
				if (db.testConnection()) {
					movieDB = db;
					JOptionPane.showMessageDialog(this, "Connection to MovieLens DB successful!", "Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					movieDB = null; // Reset on failure
					JOptionPane.showMessageDialog(this, "Connection failed. Check console for errors.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (Exception ex) {
				movieDB = null;
				JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		// Action Listener for the Movie Count Button
		countButton.addActionListener(e -> {
			if (checkConnection()) {
				int count = movieDB.getMovieCount();
				JOptionPane.showMessageDialog(this, "Total Movies: " + count, "Movie Count",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Action Listener for the Get Movie Titles Button
		titlesButton.addActionListener(e -> {
			if (checkConnection()) {
				String[] titles = movieDB.getMovieTitles(50);
				StringBuilder sb = new StringBuilder();
				sb.append("Top 50 Movie Titles:\n\n");
				for (String t : titles)
					sb.append(t).append('\n');
				showScrollableMessage("Movie Titles", sb.toString());

			}
		});

		// Action Listener for the Get Total Ratings Button
		totalRatingsButton.addActionListener(e -> {
			if (checkConnection()) {
				int ratingsCount = movieDB.getRatingsCount();
				JOptionPane.showMessageDialog(this, "The total number of ratings is: " + ratingsCount, "Total Ratings",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Action Listener for the Get The Top 20 Rated Movies Button
		topRatedButton.addActionListener(e -> {
			if (checkConnection()) {
				MovieRating[] top = movieDB.getTopRatedMovies(20);
				StringBuilder sb = new StringBuilder();
				sb.append("Top 20 Rated Movie Titles:\n\n");
				for (MovieRating r : top)
					sb.append(r.getTitle()).append(" - ").append(r.getRating()).append('\n');
				showScrollableMessage("Top Rated Movies: ", sb.toString());
			}
		});
		// Action Listener for the Get Total Users Button
		usersButton.addActionListener(e -> {
			if (checkConnection()) {
				int totUsers = movieDB.getUserCount();
				JOptionPane.showMessageDialog(this, "Total number of users is: " + totUsers, "User Count",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		// Action Listener for the Get Popular Genres Button
		popGenresButton.addActionListener(e -> {
			if (checkConnection()) {
				GenreCount[] popGenres = movieDB.getPopularGenres(10);
				StringBuilder sb = new StringBuilder();
				sb.append("Popular Genres:\n\n");
				for (GenreCount g : popGenres)
					sb.append(g.getGenre()).append(" - ").append(g.getCount()).append('\n');
				showScrollableMessage("Popular Genres", sb.toString());
			}
		});
		// Action Listener for the Get Total Tags Button
		tagsButton.addActionListener(e -> {
			if (checkConnection()) {
				int totTags = movieDB.getTagsCount();
				JOptionPane.showMessageDialog(this, "The total number of tags is: " + totTags, "Total Tags",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		// Action Listener for the Get Popular Tags Button
		popTagsButton.addActionListener(e -> {
			if (checkConnection()) {
				TagCount[] popTags = movieDB.getPopularTags(15);
				StringBuilder sb = new StringBuilder();
				sb.append("Popular Tags:\n\n");
				for (TagCount t : popTags)
					sb.append(t.getTag()).append(" - ").append(t.getCount()).append('\n');
				showScrollableMessage("Popular Tags", sb.toString());
			}
		});
	}

	/**
	 * Checks if the database object is initialized. If not, shows an error message.
	 * 
	 * @return true if connected, false otherwise.
	 */
	private boolean checkConnection() {
		if (movieDB == null) {
			JOptionPane.showMessageDialog(this, "You must connect to the database first!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * Displays a message in a JOptionPane with a scrollable text area.
	 * 
	 * @param title   The title of the dialog window.
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
	 * Main method: Entry point for the application. Creates and displays the GUI
	 * frame.
	 */
	public static void main(String[] args) {
		// Ensures the GUI is created on the Event Dispatch Thread for thread safety
		SwingUtilities.invokeLater(() -> new MovieLensGUI());
	}

	// Custom UI to ensure progress bar uses its foreground color when painted
	private static class ColorProgressBarUI extends javax.swing.plaf.basic.BasicProgressBarUI {
		@Override
		public void paintDeterminate(Graphics g, JComponent c) {
			Insets b = progressBar.getInsets(); // area for border
			int barRectWidth = progressBar.getWidth() - (b.left + b.right);
			int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
			if (barRectWidth <= 0 || barRectHeight <= 0) {
				return;
			}

			int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

			// Paint background
			g.setColor(progressBar.getBackground());
			g.fillRect(b.left, b.top, barRectWidth, barRectHeight);

			// Choose fill color based on value (0-4) to avoid LAF inconsistencies
			Color fillColor;
			try {
				int v = progressBar.getValue();
				switch (v) {
					case 4: fillColor = new Color(51, 153, 51); break; // very strong (green)
					case 3: fillColor = new Color(0, 160, 0); break; // strong (green)
					case 2: fillColor = new Color(255, 180, 0); break; // fair (amber)
					case 1: fillColor = new Color(255, 80, 0); break; // weak (orange)
					case 0: fillColor = Color.RED; break; // very weak
					default: fillColor = progressBar.getForeground(); break;
				}
			} catch (Exception ex) {
				fillColor = progressBar.getForeground();
			}

			// Paint the progress using the selected fill color
			g.setColor(fillColor);
			g.fillRect(b.left, b.top, amountFull, barRectHeight);

			// Paint border (optional)
			if (progressBar.isBorderPainted()) {
				g.setColor(Color.DARK_GRAY);
				g.drawRect(b.left, b.top, barRectWidth - 1, barRectHeight - 1);
			}
		}
	}
}