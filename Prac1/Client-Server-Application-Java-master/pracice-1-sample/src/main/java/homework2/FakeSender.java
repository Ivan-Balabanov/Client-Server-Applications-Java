package homework2;

import org.apache.commons.codec.binary.Hex; //while it's "fake" decided to omit manual encryption.

public class FakeSender implements SenderInterface {

    @Override
    public void sendMessage(byte[] message) {
        System.out.println("[FakeSender] Sending packet ("
                + message.length + " bytes): "
                + Hex.encodeHexString(message));
    }
}