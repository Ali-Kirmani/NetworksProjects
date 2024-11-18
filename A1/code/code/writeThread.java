import java.net.Socket;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class writeThread implements Runnable{

@Override
public void run()
        {
         int numBytes = 0;
		 byte[] buff = new byte[bufferSize];
		 // while not EOF from client side
		 while ((numBytes = inputStream.read(buff)) != -1) {
            System.out.println("R " + numBytes);
            fileOutputStream.write(buff, 0, numBytes);
            fileOutputStream.flush();
        }
        }
    

    public writeThread(DataOutputStream inputStream, FileOutputStream fileOutputStream, int bufferSize, Socket socket){
        private  DataInputStream inputStream;
        private FileOutputStream fileOutputStream;
        private int bufferSize;
        private Socket socket;
    }
}