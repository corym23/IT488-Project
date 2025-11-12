package com.pug.in452.IN452_MovieLens;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Unit 6 Assignment: MovieLensThreadApp
 * Simple Swing application to control MovieLensSimulator and display output.
 */
public class MovieLensThreadApp extends JFrame {

    private static final long serialVersionUID = 1L;

    private MovieDatabase controller;
    private MovieLensSimulator simulator;

    private final JTextArea logArea = new JTextArea();
    private final JButton connectButton = new JButton("Connect to DB");
    private final JButton demoConnectButton = new JButton("Connect (Demo)");
    private final JButton startButton = new JButton("Start Simulation");
    private final JButton stopButton = new JButton("Stop Simulation");
    private final JSlider speedSlider = new JSlider(100, 3000, 1000);

    public MovieLensThreadApp() {
        super("MovieLens Threading Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(600, 500));
        setLocationRelativeTo(null);

        // Text area for logs
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        add(scroll, BorderLayout.CENTER);

        // Control panels: top buttons and slider below so labels are not clipped.
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topButtons.add(connectButton);
        topButtons.add(demoConnectButton);
        topButtons.add(startButton);
        topButtons.add(stopButton);
        topButtons.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Put buttons in a container and the slider in its own panel to preserve vertical space for labels
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));

        JLabel speedLabel = new JLabel("Simulation Speed (ms):");
        speedLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        sliderPanel.add(speedLabel);

         // Configure labeled slider for simulation speed (milliseconds)
         // Create a label table so users see numeric values on the slider.
         Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
         labelTable.put(100, new JLabel("100")); 
         labelTable.put(500, new JLabel("500"));
         labelTable.put(1000, new JLabel("1000"));
         labelTable.put(2000, new JLabel("2000"));
         labelTable.put(3000, new JLabel("3000"));
         speedSlider.setLabelTable(labelTable);
         speedSlider.setPaintLabels(true);
         speedSlider.setPaintTicks(true);
         speedSlider.setPreferredSize(new Dimension(500,100)); 
         speedSlider.setMaximumSize(new Dimension(500,100));
         speedSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
         sliderPanel.add(speedSlider);

         // Container for controls: buttons at top, slider panel below
         JPanel controls = new JPanel(new BorderLayout());
         controls.add(topButtons, BorderLayout.NORTH);
         controls.add(sliderPanel, BorderLayout.CENTER); 
         add(controls, BorderLayout.NORTH);

         // Initial button states
         startButton.setEnabled(false);
         stopButton.setEnabled(false);

         // Redirect System.out to the text area
         redirectSystemOut();

         // Action handlers
         connectButton.addActionListener(e -> onConnect());
         demoConnectButton.addActionListener(e -> onDemoConnect());
         startButton.addActionListener(e -> onStart());
         stopButton.addActionListener(e -> onStop());

         //100 as major tick spacing so the numeric labels align under major ticks (100,500,1000,2000,3000)
         speedSlider.setMajorTickSpacing(100);
         speedSlider.setMinorTickSpacing(100);
         speedSlider.setPaintTicks(true);
         speedSlider.setPaintLabels(true);

         speedSlider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (!speedSlider.getValueIsAdjusting() && simulator != null) {
                     int value = speedSlider.getValue();
                     try {
                         simulator.setSimulationSpeed(value);
                     } catch (Exception ex) {
                         System.err.println("Failed to set speed: " + ex.getMessage());
                     }
                 }
             }
         });
     }

    private void onDemoConnect() {
        try {
            controller = new DemoMovieLensDB();
            simulator = new MovieLensSimulator(controller);
            startButton.setEnabled(true);
            System.out.println("Connected to demo database successfully.");
        } catch (Exception ex) {
            System.err.println("Demo connection failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onConnect() {
        // Hard-coded connection string (as requested). Adjust if your DB differs.
        String connStr = "jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;";
        try {
            MovieLensDB db = MovieLensDB.getInstance(connStr);
            boolean ok = db.testConnection();
            if (ok) {
                controller = db;
                simulator = new MovieLensSimulator(controller);
                startButton.setEnabled(true);
                System.out.println("Connected to database successfully.");
            } else {
                System.err.println("Connection failed. See stderr for details.");
            }
        } catch (Exception ex) {
            System.err.println("Connection failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onStart() {
        if (simulator == null) {
            System.err.println("Simulator not initialized. Connect to DB first.");
            return;
        }
        simulator.setSimulationSpeed(speedSlider.getValue());
        simulator.startSimulation();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void onStop() {
        if (simulator == null) {
            System.err.println("Simulator not initialized.");
            return;
        }
        simulator.stopSimulation();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void redirectSystemOut() {
        // Redirect System.out to the text area so simulation prints show up in GUI
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                SwingUtilities.invokeLater(() -> logArea.append(String.valueOf((char) b)));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                String s = new String(b, off, len);
                SwingUtilities.invokeLater(() -> logArea.append(s));
            }
        };
        PrintStream ps = new PrintStream(out, true);
        System.setOut(ps);
        System.setErr(ps);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MovieLensThreadApp app = new MovieLensThreadApp();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}