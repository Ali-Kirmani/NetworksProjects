import java.net.Socket;

import java.io.FileInputStream;

import java.io.DataOutputStream;

public class readThread implements Runnable{

@Override
public void run()
        {
            int numBytes = 0;
		 byte[] buff = new byte[bufferSize];
		 // while not EOF from client side
		 while ( (numBytes = fileInputStream.read(buff)) != -1) {
			 // process data in buff ...
			 outputStream.write(buff, 0, numBytes);
			 System.out.println("W " + numBytes);
			 outputStream.flush(); // not needed, but a good practice

             socket.shutdownOutput();
		 }
        }
    }

    public readThread(DataOutputStream outputStream, FileInputStream inputStream, int bufferSize, Socket socket){
        private  DataOutputStream outputStream;
        private FileInputStream fileInputStream;
        private int bufferSize;
        private Socket socket;
    }
