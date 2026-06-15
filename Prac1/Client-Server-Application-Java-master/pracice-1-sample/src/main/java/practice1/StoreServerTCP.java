package practice1;

import java.io.*;
import java.net.*;

public class StoreServerTCP {

    private static final int PORT = 8088;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("TCP Server started on port " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected: " + socket.getRemoteSocketAddress());
            new Thread(new TCPHandler(socket)).start();
        }
    }

    static class TCPHandler implements Runnable {
        private final Socket socket;

        TCPHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    DataInputStream in  = new DataInputStream(socket.getInputStream());
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream())
            ) {
                Decryptor decryptor = new Decryptor();

                while (true) {
                    // Read packet length first so we know how many bytes to expect
                    // Header is 16 bytes: 1+1+8+4+2 = 16
                    byte[] header = new byte[16];
                    in.readFully(header);

                    // wLen is at offset 10, 4 bytes
                    int wLen = ((header[10] & 0xFF) << 24)
                            | ((header[11] & 0xFF) << 16)
                            | ((header[12] & 0xFF) << 8)
                            |  (header[13] & 0xFF);

                    // Read body: wLen bytes + 2 bytes CRC
                    byte[] rest = new byte[wLen + 2];
                    in.readFully(rest);

                    // Reassemble full packet
                    byte[] packet = new byte[header.length + rest.length];
                    System.arraycopy(header, 0, packet, 0, header.length);
                    System.arraycopy(rest,   0, packet, header.length, rest.length);

                    // Decode and print
                    Message msg = decryptor.decode(packet);
                    System.out.println("[TCP Server] Received from userId="
                            + msg.getUserId()
                            + " commandType=" + msg.getCommandType()
                            + " payload=" + msg.getPayloadAsString());

                    // Echo the same packet back
                    out.write(packet);
                    out.flush();
                }

            } catch (EOFException e) {
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
            } catch (Exception e) {
                System.out.println("Handler error: " + e.getMessage());
            }
        }
    }
}