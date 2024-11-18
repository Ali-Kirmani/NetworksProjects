
/**
 * StreamClient Class
 * 
 * CPSC 441
 * Assignment 1
 *
 */


 import java.io.*;
 import javax.net.ssl.*;
 import java.util.Scanner;
 import java.util.logging.*;
 
 
 public class StreamClient{
 
	 private static final Logger logger = Logger.getLogger("StreamClient"); // global logger
	 //InputStream inputStream;
	 //OutputStream outputStream;
	 //InputStream targetStream;
	 //OutputStream outStream;
 
	 private String serverName;
	 private int serverPort;
	 private int bufferSize;
 
	 /**
	  * Constructor to initialize the class.
	  * 
	  * @param serverName	remote server name
	  * @param serverPort	remote server port number
	  * @param bufferSize	buffer size used for read/write
	  */
	 public StreamClient(String serverName, int serverPort, int bufferSize)
	 {		
		 this.serverName = serverName;
		 this.serverPort = serverPort;
		 this.bufferSize = bufferSize;
	 }

	 //public void run(){}
	 
	 /**
	  * Compress the specified file via the remote server.
	  * 
	  * @param inName		name of the input file to be processed
	  * @param outName		name of the output file
	  */
	 public void getService(int serviceCode, String inName, String outName)
	 {
		 try{Socket socket = new Socket(serverName, serverPort);
 
			 // Create necessary streams
			 DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			 DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			 
 
		 outputStream.writeInt(serviceCode);
 
		 // send/receive messages to/from server
		 int numBytes = 0;
		 byte[] buff = new byte[bufferSize];
		 Thread readThread = new Thread(new readThread(outputStream, fileInputStream, numBytes, socket));
		 //readThread.start();
		 // while not EOF from client side
		 while ( (numBytes = fileInputStream.read(buff)) != -1) {
			 // process data in buff ...
			 outputStream.write(buff, 0, numBytes);
			 System.out.println("W " + numBytes);
			 outputStream.flush(); // not needed, but a good practice
		 }


 
		 Thread writeThread = new Thread(new writeThread(outputStream, fileOutputStream, numBytes, socket));
		 //writeThread.start();
		 // Step 5: Read from the socket and write to the output file
		while ((numBytes = inputStream.read(buff)) != -1) {
			 System.out.println("R " + numBytes);
			 fileOutputStream.write(buff, 0, numBytes);
			 fileOutputStream.flush();
		 }
		 inputStream.close();
		 outputStream.close();
		 socket.close();
	 
 
		 fileInputStream.close();
		 fileOutputStream.close();
		 }
 
		 catch(Exception e){
			 e.printStackTrace();
			 logger.log(Level.SEVERE, "Error: " + e.getMessage(), e);
		 }
	 }
 
 }