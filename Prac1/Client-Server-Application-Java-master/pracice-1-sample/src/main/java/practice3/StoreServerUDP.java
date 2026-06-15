package practice3;

import practice1.Decryptor;
import practice1.Encryptor;
import practice1.Message;

import java.net.*;

public class StoreServerUDP {

    private static final int PORT = 8089;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("[UDP Server] Started on port " + PORT);

        Decryptor decryptor = new Decryptor();
        Encryptor encryptor = new Encryptor();
        byte[] buf = new byte[BUFFER_SIZE];

        while (true) {
            DatagramPacket inPacket = new DatagramPacket(buf, buf.length);
            socket.receive(inPacket);

            byte[] data = new byte[inPacket.getLength()];
            System.arraycopy(inPacket.getData(), 0, data, 0, inPacket.getLength());

            try {
                Message msg = decryptor.decode(data);
                System.out.println("[UDP Server] userId=" + msg.getUserId() + " payload=" + msg.getPayloadAsString());

                // Build and send echo response
                Message reply  = new Processor(msg).process();
                byte[]  packet = encryptor.entry_take(reply);

                DatagramPacket outPacket = new DatagramPacket(packet, packet.length, inPacket.getAddress(), inPacket.getPort());
                socket.send(outPacket);

            } catch (Exception e) {
                System.out.println("[UDP Server] Bad packet, dropping: " + e.getMessage());
            }
        }
    }
}