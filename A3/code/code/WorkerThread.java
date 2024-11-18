/*
 * WorkerThread Class
 * 
 * Ali Kirmani 30115539
 * CPSC 441
 * Assignment 3
 *
 */



import java.io.*;
import java.net.*;
import java.util.logging.*;

public class WorkerThread extends Thread {
    private static final Logger logger = Logger.getLogger("WebServer");

    private Socket clientSocket;
    private String root;
    private int timeout;

    public WorkerThread(Socket clientSocket, String root, int timeout) {
        this.clientSocket = clientSocket;
        this.root = root;
        this.timeout = timeout;
    }

    public void run() {
        PrintWriter out = null; // declare PrintWriter variable

        try {
            // Set timeout for reading from socket
            clientSocket.setSoTimeout(timeout);
    
            // Open input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true); // initialize PrintWriter
    
            // Read and parse request
            String requestLine = in.readLine();
            logger.info("Received request: " + requestLine);
    
            // Parse request line
            String[] parts = requestLine.split("\\s+");
            if (parts.length != 3 || !parts[0].equals("GET") || !parts[2].equals("HTTP/1.1")) {
                sendResponse(out, 400, "Bad Request");
                return;
            }
    
            // Extract requested object path
            String objectPath = parts[1];
            if (objectPath.equals("/")) {
                objectPath = "/index.html";
            }
    
            // Construct file path
            String filePath = root + objectPath;
    
            // Check if file exists
            File file = new File(filePath);
            if (!file.exists()) {
                sendResponse(out, 404, "Not Found");
                return;
            }
    
            // Send OK response with file content
            sendResponse(out, 200, "OK", file);
        } catch (SocketTimeoutException e) {
            // Request timeout
            sendResponse(out, 408, "Request Timeout");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing request: " + e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket: " + e.getMessage(), e);
            }
        }
    }

    private void sendResponse(PrintWriter out, int statusCode, String statusPhrase) {
        out.println("HTTP/1.1 " + statusCode + " " + statusPhrase);
        out.println("Date: " + ServerUtils.getCurrentDate());
        out.println("Server: CustomWebServer");
        out.println("Connection: close");
        out.println();
        out.flush();
    }

    private void sendResponse(PrintWriter out, int statusCode, String statusPhrase, File file) {
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String contentType = ServerUtils.getContentType(file);
            String lastModified = ServerUtils.getLastModified(file);
            String contentLength = ServerUtils.getContentLength(file);

            out.println("HTTP/1.1 " + statusCode + " " + statusPhrase);
            out.println("Date: " + ServerUtils.getCurrentDate());
            out.println("Server: CustomWebServer");
            out.println("Last-Modified: " + lastModified);
            out.println("Content-Length: " + contentLength);
            out.println("Content-Type: " + contentType);
            out.println("Connection: close");
            out.println();

            String line;
            while ((line = fileReader.readLine()) != null) {
                out.println(line);
            }
            out.flush();

            fileReader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending response: " + e.getMessage(), e);
        }
    }
}