package practice3;

import practice1.Decryptor;
import practice1.Message;

import java.io.*;
import java.net.Socket;

public class Receiver extends Thread {

    private final Socket socket;
    private final DataInputStream  in;
    private final DataOutputStream out;

    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        Decryptor decryptor = new Decryptor();

        try {
            while (true) {
                // Read header: 1+1+8+4+2 = 16 bytes
                byte[] header = new byte[16];
                in.readFully(header);

                // wLen at offset 10, 4 bytes big-endian (with the help of the recalculation by AI, so it does not break again)
                int wLen = ((header[10] & 0xFF) << 24)
                        | ((header[11] & 0xFF) << 16)
                        | ((header[12] & 0xFF) << 8)
                        |  (header[13] & 0xFF);

                // Read body + 2 byte CRC
                byte[] rest = new byte[wLen + 2];
                in.readFully(rest);

                // Reassemble full packet
                byte[] packet = new byte[header.length + rest.length];
                System.arraycopy(header, 0, packet, 0, header.length);
                System.arraycopy(rest,   0, packet, header.length, rest.length);

                // Decode
                Message msg = decryptor.decode(packet);
                System.out.println("[Receiver] userId=" + msg.getUserId()
                        + " cmd=" + msg.getCommandType()
                        + " payload=" + msg.getPayloadAsString());

                // Pass to Processor and send response back
                Processor processor = new Processor(msg);
                Message reply = processor.process();

                Sender sender = new Sender(out);
                sender.sendMessage(reply);
            }

        } catch (EOFException e) {
            System.out.println("[Receiver] Client disconnected: "
                    + socket.getRemoteSocketAddress());
        } catch (Exception e) {
            System.out.println("[Receiver] Error: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("[Receiver] Could not close socket: " + e.getMessage());
        }
        System.out.println("[Receiver] Socket closed: " + socket.getRemoteSocketAddress());
    }
}