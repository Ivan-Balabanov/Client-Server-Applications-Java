package practice3;

import practice1.Decryptor;
import practice1.Message;

import java.io.*;
import java.net.*;

public class Main {

    private static final int PORT = 8088;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[TCP Server] Started on port " + PORT);

        try (serverSocket) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[TCP Server] Client connected: "
                        + socket.getRemoteSocketAddress());
                Receiver receiver = new Receiver(socket);
                receiver.start();
            }
        }
    }
}