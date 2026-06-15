package practice3;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.Message;

import java.net.*;

public class StoreClientUDP {

    private static final String HOST = "localhost";
    private static final int PORT = 8089;
    private static final int BUFFER_SIZE = 4096;
    private static final int TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 5;

    public static void main(String[] args) throws Exception {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        int clientId = (int)(Math.random() * 1000);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddr = InetAddress.getByName(HOST);

            for (int i = 0; i < 5; i++) {
                Message msg    = Message.fromString(1, clientId,
                        "UDP msg #" + i + " from client " + clientId);
                byte[]  packet = encryptor.entry_take(msg);

                boolean acked    = false;
                int     attempts = 0;

                while (!acked && attempts < MAX_RETRIES) {
                    attempts++;
                    System.out.println("[UDP Client " + clientId + "] Sending msg #" + i + " (attempt " + attempts + ")");

                    socket.send(new DatagramPacket(
                            packet, packet.length, serverAddr, PORT));

                    try {
                        byte[] buf = new byte[BUFFER_SIZE];
                        DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
                        socket.receive(inPacket);

                        byte[] responseData = new byte[inPacket.getLength()];
                        System.arraycopy(inPacket.getData(), 0, responseData, 0, inPacket.getLength());

                        Message reply = decryptor.decode(responseData);
                        System.out.println("[UDP Client " + clientId + "] ACK: " + reply.getPayloadAsString());
                        acked = true;

                    } catch (SocketTimeoutException e) {
                        System.out.println("[UDP Client " + clientId
                                + "] Timeout, retrying...");
                    }
                }

                if (!acked) {
                    System.out.println("[UDP Client " + clientId + "] Failed to deliver msg #" + i + " after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }
}