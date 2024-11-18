//Ali Kirmani
//CPSC441 A4
//30115539
//
//

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class StopWaitFtp {
    private static final Logger logger = Logger.getLogger("StopWaitFtp");
    private int timeout;
    private int initialSeqNum;
    private int clientUDPPort;
    private int serverUDPPort;
    private InetAddress serverAddress;
    private Timer timer;

    public StopWaitFtp(int timeout) {
        this.timeout = timeout;
        timer = new Timer();
    }

    public boolean send(String serverName, int serverPort, String fileName) {
        try (Socket tcpSocket = new Socket(serverName, serverPort);
             DatagramSocket udpSocket = new DatagramSocket()) {

            serverAddress = InetAddress.getByName(serverName);
            clientUDPPort = udpSocket.getLocalPort();

            performHandshake(tcpSocket, fileName);

            sendFileContent(fileName, udpSocket);

            logger.info("File sent successfully.");
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred during file transfer: " + e.getMessage(), e);
            return false;
        }
    }

    private void performHandshake(Socket tcpSocket, String fileName) throws IOException {
        try (DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
             DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream())) {

            logger.info("Send it");
            outputStream.writeUTF(fileName);
            outputStream.flush();

            File file = new File(fileName);
            long flength = file.length();
            logger.info("flength: " + flength);
            outputStream.writeLong(flength);
            outputStream.flush();

            outputStream.writeInt(clientUDPPort);
            outputStream.flush();

            serverUDPPort = inputStream.readInt();
            initialSeqNum = inputStream.readInt();
            logger.info("Handshake completed.");
        }
    }

    private void sendFileContent(String fileName, DatagramSocket udpSocket) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            byte[] buffer = new byte[FtpSegment.MAX_PAYLOAD_SIZE];
            int bytesRead;
            int seqNum = initialSeqNum;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                FtpSegment segment = new FtpSegment(seqNum++, Arrays.copyOf(buffer, bytesRead));
                sendSegment(segment, udpSocket);
                waitForAck(segment, udpSocket);
            }
        }
    }

    private void sendSegment(FtpSegment segment, DatagramSocket udpSocket) throws IOException {
        logger.info("We made it!");
        byte[] segmentBytes = segment.toBytes();
        DatagramPacket packet = new DatagramPacket(segmentBytes, segmentBytes.length, serverAddress, serverUDPPort);
        udpSocket.send(packet);
        logger.info("Segment " + segment.getSeqNum() + " sent.");
    }

    private void waitForAck(FtpSegment segment, DatagramSocket udpSocket) throws IOException {
        boolean ackReceived = false;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("Timeout occurred, retransmitting segment " + segment.getSeqNum());
                    sendSegment(segment, udpSocket);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error occurred during retransmission: " + e.getMessage(), e);
                }
            }
        };

        timer.schedule(timerTask, timeout);

        byte[] receiveBuffer = new byte[FtpSegment.MAX_SEGMENT_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        while (!ackReceived) {
            udpSocket.receive(receivePacket);

            FtpSegment ackSegment = new FtpSegment();
            ackSegment.fromBytes(Arrays.copyOf(receivePacket.getData(), receivePacket.getLength()));

            if (ackSegment.getSeqNum() == segment.getSeqNum() + 1) {
                ackReceived = true;
                logger.info("ACK received for segment " + ackSegment.getSeqNum());
                timerTask.cancel();
            }
        }
    }
}
