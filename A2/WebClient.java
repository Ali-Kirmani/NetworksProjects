
/*
 * WebClient Class
 * 
 * CPSC 441
 * Assignment 2
 * 
 * @author 	Majid Ghaderi
 * @version	2024
 *
 * 
 * Ali Kirmani
 * 30115539
 */

 import java.io.*;
 import java.util.HashMap;
 import java.util.logging.*;
 
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLSocketFactory;
 
 import java.net.Socket;
 
 public class WebClient {
 
     String objectURL;
 
     private static final Logger logger = Logger.getLogger("WebClient"); // global logger
 
     /**
      * Default no-arg constructor
      */
     public WebClient() {
         // nothing to do!
     }
     
     /**
      * Downloads the object specified by the parameter url.
      *
      * @param url	URL of the object to be downloaded. It is a fully qualified URL.
      */
 public void getObject(String url){  //The argument is supposed to be an array of strings not a regular string
 
     // Manually parse the URL for protocol, server name, port number, and object path
     String protocol = "";
     String serverName = "";
     int port = -1;
     String objectPath = "";
 
     // Check if the URL starts with "http://" or "https://"
     if (url.startsWith("http://")) {
         protocol = "HTTP";
         url = url.substring(7); // Remove "http://"
         port = 80; // Default port for HTTP
     } else if (url.startsWith("https://")) {
         protocol = "HTTPS";
         url = url.substring(8); // Remove "https://"
         port = 443; // Default port for HTTPS
     } else {
         logger.severe("Unsupported protocol in the URL.");
         return;
     }
 
     // Find the end index of the server name, which may include a port number
     int slashIndex = url.indexOf('/');
     int colonIndex = url.indexOf(':');
     int endIndex = (colonIndex != -1 && colonIndex < slashIndex) ? colonIndex : slashIndex;
 
     // Extract server name and port number
     if (endIndex != -1) {
         String serverAndPort = url.substring(0, endIndex);
         url = url.substring(endIndex);
         int leftBracketIndex = serverAndPort.indexOf('[');
         int rightBracketIndex = serverAndPort.indexOf(']');
         if (leftBracketIndex != -1 && rightBracketIndex != -1 && leftBracketIndex < rightBracketIndex) {
             // Extract the port number enclosed in square brackets  //**FIX THIS IT WONT GIVE CUSTOM PORT NUMBER**
             try {
                 port = Integer.parseInt(serverAndPort.substring(leftBracketIndex + 1, rightBracketIndex));
                 serverName = serverAndPort.substring(0, leftBracketIndex);
             } catch (NumberFormatException e) {
                 logger.warning("Invalid port number, defaulting to " + port);
             }
         } else {
             // No square brackets found, parse server name normally
             int portIndex = serverAndPort.lastIndexOf(':');
             if (portIndex != -1) {
                 serverName = serverAndPort.substring(0, portIndex);
                 try {
                     port = Integer.parseInt(serverAndPort.substring(portIndex + 1));
                 } catch (NumberFormatException e) {
                     logger.warning("Invalid port number, defaulting to " + port);
                 }
             } else {
                 serverName = serverAndPort;
             }
         }
     } else {
         serverName = url;
     }
 
     // Extract object path from the URL
     int pathIndex = url.indexOf('/');
     if (pathIndex != -1) {
         objectPath = url.substring(pathIndex); // Include the slash
     }
 
     // Output the parsed components
     System.out.println("Protocol: " + protocol);
     //System.out.println("Server Name: " + serverName);
     //System.out.println("Port: " + port);
     //System.out.println("Object Path: " + objectPath);
                
 
 
     if (protocol.equals("HTTP")) {
         try (Socket socket = new Socket(serverName, port);
              PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
              BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
     
             // Send GET request with required header lines
             out.println("GET " + objectPath + " HTTP/1.1\r");
             out.println("Host: " + serverName + "\r");
             out.println("Connection: close\r");
             out.println("\r"); // Empty line to signal end of headers
     
             // Read the status line of the server response
             String statusLine = in.readLine();
             System.out.println("Server Response Status: " + statusLine);
     
             // Check if the response status is "200 OK"
             if (statusLine.startsWith("HTTP/1.1 200")) {
                 // Extract object name from the URL
                 String[] urlParts = url.split("/");
                 String objectName = urlParts[urlParts.length - 1]; // Assuming the object name is the last part of the URL
     
                 // Create a local file with the object name
                 File file = new File(objectName);
     
                 // Open a FileOutputStream to write to the local file
                 try (FileOutputStream fos = new FileOutputStream(file)) {
                     // Write the remaining response body to the local file
                     String responseLine;
                     while ((responseLine = in.readLine()) != null) {
                         fos.write(responseLine.getBytes());
                         fos.write("\n".getBytes()); // Add newline character between lines
                     }
     
                     //System.out.println("File downloaded successfully: " + file.getAbsolutePath());
                 } catch (IOException e) {
                     logger.log(Level.SEVERE, "Error writing to file: " + e.getMessage(), e);
                 }
             } else {
                 System.out.println("Server response status is not '200 OK'.");
             }
     
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Error: " + e.getMessage(), e);
         }
     }
 
     else if (protocol.equals("HTTPS")) {
         try {
             SocketFactory factory = SSLSocketFactory.getDefault();
             try (Socket socket = factory.createSocket(serverName, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                  BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
     
                 // Send GET request with required header lines
                 out.println("GET " + objectPath + " HTTP/1.1\r");
                 out.println("Host: " + serverName + "\r");
                 out.println("Connection: close\r");
                 out.println("\r"); // Empty line to signal end of headers
     
                 // Read the status line of the server response
                 String statusLine = in.readLine();
                 System.out.println("Server Response Status: " + statusLine);
     
                 // Check if the response status is "200 OK"
                 if (statusLine.startsWith("HTTP/1.1 200")) {
                     // Extract object name from the URL
                     String[] urlParts = url.split("/");
                     String objectName = urlParts[urlParts.length - 1]; // Assuming the object name is the last part of the URL
                     // Create a local file with the object name
                     File file = new File(objectName);
     
                     // Open a FileOutputStream to write to the local file
                     try (FileOutputStream fos = new FileOutputStream(file)) {
                         String responseLine;
                         while ((responseLine = in.readLine()) != null && !responseLine.isEmpty()) ;
     
                         // Write the remaining response body to the local file
                         while ((responseLine = in.readLine()) != null) {
                             fos.write(responseLine.getBytes());
                             fos.write("\n".getBytes());
                         }
     
                         //System.out.println("File downloaded successfully: " + file.getAbsolutePath());
                     } catch (IOException e) {
                         logger.log(Level.SEVERE, "Error writing to file: " + e.getMessage(), e);
                     }
                 } else {
                     System.out.println("Server response status is not '200 OK'.");
                 }
     
             }
         } catch (IOException e) {
             logger.log(Level.SEVERE, "Error creating SSL socket: " + e.getMessage(), e);
         }
     }
     
     
 }
 
 }
 