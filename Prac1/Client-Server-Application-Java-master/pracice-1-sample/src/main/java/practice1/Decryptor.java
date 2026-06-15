package practice1;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Decryptor {

    private static final byte[] KEY = "1234567890abcdef".getBytes();

    public Message decode(byte[] packet) throws Exception {


        byte[] header = new byte[14];
        System.arraycopy(packet, 0, header, 0, 14);

        short expectedHeaderCrc = (short)(
                ((packet[14] & 0xFF) << 8) |
                        (packet[15] & 0xFF)
        );
        short actualHeaderCrc = Crc16.calculateCrc(header);

        if (expectedHeaderCrc != actualHeaderCrc)
            throw new Exception("Header CRC mismatch: expected "
                    + expectedHeaderCrc + " got " + actualHeaderCrc);


        int wLen = ByteBuffer.wrap(packet, 10, 4)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();


        byte[] encrypted = new byte[wLen];
        System.arraycopy(packet, 16, encrypted, 0, wLen);


        short expectedBodyCrc = (short)(
                ((packet[16 + wLen]     & 0xFF) << 8) |
                        (packet[16 + wLen + 1] & 0xFF)
        );
        short actualBodyCrc = Crc16.calculateCrc(encrypted);

        if (expectedBodyCrc != actualBodyCrc)
            throw new Exception("Body CRC mismatch: expected "
                    + expectedBodyCrc + " got " + actualBodyCrc);

        byte[] decrypted = decrypt(encrypted);

        ByteBuffer buf = ByteBuffer.wrap(decrypted)
                .order(ByteOrder.BIG_ENDIAN);

        int commandType = buf.getInt();
        int userId      = buf.getInt();

        byte[] payload = new byte[decrypted.length - 8];
        buf.get(payload);

        return new Message(commandType, userId, payload);
    }

    private byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }
}