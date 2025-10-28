package com.pug.in452.IN452_MovieLens;

/**
 * MovieLensSimulator simulates database activity against a MovieLens-style
 * database using multiple background threads.  It is intentionally lightweight
 * and prints human-readable status messages to System.out so a GUI can
 * capture and display them.
 *
 * Behavior summary:
 * - When startSimulation() is called three threads are started:
 *   * movieThread: frequently queries movie-related endpoints (movie count,
 *     popular genres) at the configured simulation speed.
 *   * ratingThread: queries rating-related endpoints (ratings count,
 *     top-rated movies) at approximately half the frequency of movieThread.
 *   * monitorThread: tracks the number of monitor cycles and stops the
 *     simulation automatically after a predefined number of cycles (5).
 * - stopSimulation() stops all threads by flipping the running flag and
 *   interrupting sleeping threads so they terminate promptly.
 * - setSimulationSpeed(int) updates the delay used by the threads (in ms). It
 *   interrupts threads so they pick up the new value immediately.

 */
public class MovieLensSimulator {

    private final MovieLensDB dbController;
    private volatile boolean isRunning = false;
    private volatile int simulationSpeed = 1000; // milliseconds

    // Thread references so we can interrupt them when stopping
    private Thread movieThread;
    private Thread ratingThread;
    private Thread monitorThread;

    /**
     * Create a simulator bound to the provided MovieLensDB controller.
     *
     * @param controller MovieLensDB instance to use for queries. May be a
     *                   DemoMovieLensDB in environments where a real DB is
     *                   unavailable.
     */
    public MovieLensSimulator(MovieLensDB controller) {
        this.dbController = controller;
        System.out.println("MovieLensSimulator initialized (speed=" + simulationSpeed + " ms)");
    }

    /**
     * Start the simulator threads. If the simulator is already running this
     * method returns immediately.
     *
     * Threads launched:
     * - movieThread: calls getMovieCount() and getPopularGenres() every
     *   simulationSpeed milliseconds.
     * - ratingThread: calls getRatingsCount() and getTopRatedMovies() every
     *   simulationSpeed * 2 milliseconds (slower updates).
     * - monitorThread: sleeps for simulationSpeed each loop, increments an
     *   internal cycle counter, logs progress, and stops the simulation after
     *   5 cycles.
     */
    public synchronized void startSimulation() {
        if (isRunning) {
            System.out.println("Simulation is already running!");
            return;
        }

        isRunning = true;
        System.out.println("Starting MovieLens database simulation...");

        // Movie thread: regular speed
        movieThread = new Thread(() -> {
            try {
                while (isRunning) {
                    try {
                        String count = dbController.getMovieCount();
                        System.out.println("[MovieThread] Movie count: " + count);
                        String genres = dbController.getPopularGenres();
                        System.out.println("[MovieThread] Popular genres:\n" + genres);
                    } catch (Exception e) {
                        System.err.println("[MovieThread] Error during DB operations: " + e.getMessage());
                    }

                    try {
                        Thread.sleep(simulationSpeed);
                    } catch (InterruptedException ie) {
                        System.out.println("[MovieThread] Interrupted, exiting...");
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                System.out.println("[MovieThread] Exited");
            }
        }, "MovieLens-MovieThread");

        // Rating thread: half speed (slower updates)
        ratingThread = new Thread(() -> {
            try {
                while (isRunning) {
                    try {
                        String ratings = dbController.getRatingsCount();
                        System.out.println("[RatingThread] Ratings count: " + ratings);
                        String top = dbController.getTopRatedMovies();
                        System.out.println("[RatingThread] Top rated movies:\n" + top);
                    } catch (Exception e) {
                        System.err.println("[RatingThread] Error during DB operations: " + e.getMessage());
                    }

                    try {
                        // Run at half frequency compared to movieThread
                        Thread.sleep(Math.max(1, simulationSpeed * 2));
                    } catch (InterruptedException ie) {
                        System.out.println("[RatingThread] Interrupted, exiting...");
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                System.out.println("[RatingThread] Exited");
            }
        }, "MovieLens-RatingThread");

        // Monitor thread: counts cycles and stops after 5 cycles
        monitorThread = new Thread(() -> {
            int cycles = 0;
            try {
                while (isRunning && cycles < 5) {
                    try {
                        Thread.sleep(simulationSpeed);
                    } catch (InterruptedException ie) {
                        System.out.println("[MonitorThread] Interrupted, exiting...");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    cycles++;
                    System.out.println("[MonitorThread] Completed cycle " + cycles + "/5");
                }

                if (isRunning) {
                    System.out.println("[MonitorThread] Reached maximum cycles. Stopping simulation...");
                    stopSimulation();
                }

            } finally {
                System.out.println("[MonitorThread] Exited");
            }
        }, "MovieLens-MonitorThread");

        // Start threads
        movieThread.start();
        ratingThread.start();
        monitorThread.start();
    }

    /**
     * Stop the running simulation. This sets the running flag to false and
     * interrupts worker threads so they wake from sleep and terminate.
     * Calling stopSimulation() when the simulator is not running is a no-op.
     */
    public synchronized void stopSimulation() {
        if (!isRunning) {
            System.out.println("Simulation is not running.");
            return;
        }

        System.out.println("Stopping all simulation threads...");
        isRunning = false;

        // Interrupt threads to wake them from sleep
        if (movieThread != null) movieThread.interrupt();
        if (ratingThread != null) ratingThread.interrupt();
        if (monitorThread != null) monitorThread.interrupt();
    }

    /**
     * Update the simulation speed (delay between operations) in milliseconds.
     * A non-positive value will throw IllegalArgumentException.
     *
     * The method interrupts worker threads so they will pick up the new
     * simulation speed immediately rather than waiting for their sleep to
     * complete.
     *
     * @param milliseconds new speed in milliseconds (must be > 0)
     */
    public void setSimulationSpeed(int milliseconds) {
        if (milliseconds <= 0) {
            throw new IllegalArgumentException("simulationSpeed must be > 0");
        }
        this.simulationSpeed = milliseconds;
        System.out.println("Simulation speed changed to " + milliseconds + " milliseconds.");

        // Interrupt threads so they pick up the new speed immediately
        if (movieThread != null) movieThread.interrupt();
        if (ratingThread != null) ratingThread.interrupt();
        if (monitorThread != null) monitorThread.interrupt();
    }

    /**
     * Query whether the simulator is currently running.
     *
     * @return true if startSimulation() was called and stopSimulation() has
     * not yet completed; false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Return the current simulation speed in milliseconds.
     *
     * @return simulation speed in ms
     */
    public int getSimulationSpeed() {
        return simulationSpeed;
    }
}