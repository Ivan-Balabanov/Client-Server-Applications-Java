package practice3;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.Message;

import java.io.*;
import java.net.*;

public class ClientMain { //severely updated for better testing structure, recommended by AI

    private static final String HOST = "localhost";
    private static final int PORT = 8088;
    private static final int RETRY_MS = 3000;
    private static final int MESSAGES = 5;

    public static void main(String[] args) {
        Encryptor encryptor = new Encryptor();
        Decryptor decryptor = new Decryptor();

        int clientId = (int)(Math.random() * 1000);

        while (true) {
            try (
                    Socket socket = new Socket(HOST, PORT);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream())
            ) {
                System.out.println("[Client " + clientId + "] Connected.");

                for (int i = 0; i < MESSAGES; i++) {
                    // Send
                    Message msg = Message.fromString(1, clientId,"Hello from client " + clientId + ", msg #" + i);
                    byte[] packet = encryptor.entry_take(msg);
                    out.write(packet);
                    out.flush();

                    // Read response header
                    byte[] header = new byte[16];
                    in.readFully(header);

                    int wLen = ((header[10] & 0xFF) << 24)
                            | ((header[11] & 0xFF) << 16)
                            | ((header[12] & 0xFF) << 8)
                            |  (header[13] & 0xFF);

                    byte[] rest = new byte[wLen + 2];
                    in.readFully(rest);

                    byte[] response = new byte[header.length + rest.length];
                    System.arraycopy(header, 0, response, 0,             header.length);
                    System.arraycopy(rest, 0, response, header.length, rest.length);

                    Message reply = decryptor.decode(response);
                    System.out.println("[Client " + clientId + "] Reply: "
                            + reply.getPayloadAsString());
                }

                break; // all messages sent successfully

            } catch (ConnectException e) {
                System.out.println("[Client " + clientId
                        + "] Server unavailable, retrying in " + RETRY_MS + "ms...");
                sleep(RETRY_MS);
            } catch (EOFException e) {
                System.out.println("[Client " + clientId
                        + "] Connection lost, retrying in " + RETRY_MS + "ms...");
                sleep(RETRY_MS);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static void sleep(int ms) {
        try {Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}