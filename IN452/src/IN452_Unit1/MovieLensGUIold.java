package IN452_Unit1;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import IN452_Unit1.MovieLensDB;
import javax.swing.*;

public class MovieLensGUIold {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MovieLensGUIold window = new MovieLensGUIold();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MovieLensGUIold() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		//frame.setBounds(100, 100, 450, 300);
		frame.setSize(350,350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Movie Application");
		frame.setResizable(false);
		frame.setLayout(new BorderLayout());
		
		
		//Title Panel
		JPanel titlePanel = new JPanel();
		titlePanel.setPreferredSize(new Dimension(350,50));
		
		JLabel titleLable = new JLabel("Movie Lens");
		titleLable.setFont(new Font("Arial",Font.BOLD,18));
		titlePanel.add(titleLable);
		frame.add(titlePanel, BorderLayout.NORTH);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setPreferredSize(new Dimension(350,300));
		btnPanel.setLayout(null);
		
		//Create Buttons
		
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(350, 300));
        buttonPanel.setLayout(null);

        // Create buttons
        JButton connectBtn = new JButton("Connect");
        connectBtn.setBounds(75, 20, 200, 40);
        buttonPanel.add(connectBtn);
      
        JButton countBtn = new JButton("Movie Count");
        countBtn.setBounds(75, 70, 200, 40);
        buttonPanel.add(countBtn);

        JButton titlesBtn = new JButton("Movie Titles");
        titlesBtn.setBounds(75, 120, 200, 40);
        buttonPanel.add(titlesBtn);

        JButton topRatedBtn = new JButton("Top Rated Movies");
        topRatedBtn.setBounds(75, 170, 200, 40);
        buttonPanel.add(topRatedBtn);

        JButton genresBtn = new JButton("Movie Genres");
        genresBtn.setBounds(75, 220, 200, 40);
        buttonPanel.add(genresBtn);

        frame.add(buttonPanel, BorderLayout.CENTER);
		
     // 🎯 Add ActionListener to Connect button
        
//        MovieLensDB db = new MovieLensDB();
//       
//        
//        System.out.println("## Total Movie Count ##");
//        String movieCount = db.getMovieCount();
//        System.out.println("Total movies in database: " + movieCount);
//        System.out.println("----------------------------------------\n");


	}

}
