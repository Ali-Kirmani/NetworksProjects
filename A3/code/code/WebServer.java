

/*
 * WebServer Class
 * 
 * Ali Kirmani 30115539
 * CPSC 441
 * Assignment 3
 * 
 * Implements a multi-threaded web server
 * supporting non-persistent connections.
 *
 */


 import java.io.*;
 import java.net.*;
 import java.util.logging.*;

 public class WebServer extends Thread {
    // global logger object, configures in the driver class
    private static final Logger logger = Logger.getLogger("WebServer");

    private boolean shutdown = false; // shutdown flag
    private ServerSocket serverSocket;
    private int port;
    private String root;
    private int timeout;

    /**
     * Constructor to initialize the web server
     *
     * @param port     Server port at which the web server listens > 1024
     * @param root     Server's root file directory
     * @param timeout  Idle connection timeout in milli-seconds
     */
    public WebServer(int port, String root, int timeout) {
        this.port = port;
        this.root = root;
        this.timeout = timeout;
    }

    /**
     * Main method in the web server thread.
     * The web server remains in listening mode
     * and accepts connection requests from clients
     * until it receives the shutdown signal.
     */
    public void run() {
        try {
            // Create server socket
            serverSocket = new ServerSocket(port);
            logger.info("Web server started on port " + port);

            // Listen for connections and handle them in separate threads
            while (!shutdown) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted connection from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                WorkerThread workerThread = new WorkerThread(clientSocket, root, timeout);
                workerThread.start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in web server: " + e.getMessage(), e);
        } finally {
            // Close server socket when shutdown
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    logger.info("Server socket closed");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error closing server socket: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Signals the web server to shutdown.
     */
    public void shutdown() {
        shutdown = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing server socket during shutdown: " + e.getMessage(), e);
            }
        }
    }

}