package practice3;

import practice1.Encryptor;
import practice1.Message;

import java.io.DataOutputStream;
import java.io.IOException;

public class Sender {

    private final DataOutputStream out;
    private final Encryptor encryptor = new Encryptor();

    public Sender(DataOutputStream out) {
        this.out = out;
    }

    public void sendMessage(Message message) throws Exception {
        byte[] packet = encryptor.entry_take(message);
        out.write(packet);
        out.flush();
        System.out.println("[Sender] Sent " + packet.length + " bytes.");
    }
}