package practice1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {

    private static final byte[] KEY =
            "1234567890abcdef".getBytes();

    public byte[] entry_take(Message msg) throws Exception {


        byte[] payload = msg.getPayload();

        ByteBuffer messageBuffer =
                ByteBuffer.allocate(
                        4 + 4 + payload.length
                );

        messageBuffer.order(ByteOrder.BIG_ENDIAN);

        messageBuffer.putInt(msg.getCommandType());
        messageBuffer.putInt(msg.getUserId());
        messageBuffer.put(payload);

        byte[] encrypted =
                encrypt(messageBuffer.array());

        ByteBuffer byteBuffer =
                ByteBuffer.allocate(
                        1 + 1 + 8 + 4 + 2 + encrypted.length + 2
                    );

        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        // HEADER

        byteBuffer.put((byte) 0x13);
        byteBuffer.put((byte) 13);
        byteBuffer.putLong(130);
        byteBuffer.putInt(encrypted.length);


        byte[] header = new byte[14];

        System.arraycopy(
                byteBuffer.array(),
                0,
                header,
                0,
                14
        );

        byteBuffer.putShort(
                Crc16.calculateCrc(header)
        );


        byteBuffer.put(encrypted);


        byteBuffer.putShort(
                Crc16.calculateCrc(encrypted)
        );

        return byteBuffer.array();
    }

    private byte[] encrypt(byte[] data)
            throws Exception {

        Cipher cipher =
                Cipher.getInstance("AES");

        SecretKeySpec keySpec =
                new SecretKeySpec(KEY, "AES");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                keySpec
        );

        return cipher.doFinal(data);
    }
}