package practice1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class Message {

    private final int commandType;
    private final int userId;
    private final byte[] payload;  // raw bytes — works for any type

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Message(int commandType, int userId, byte[] payload) {
        this.commandType = commandType;
        this.userId = userId;
        this.payload = payload;
    }

    public static Message fromString(int commandType, int userId, String text) {
        return new Message(commandType, userId,
                text.getBytes(StandardCharsets.UTF_8));
    }

    public static Message fromInt(int commandType, int userId, int value) {
        byte[] bytes = ByteBuffer.allocate(4)
                .order(ByteOrder.BIG_ENDIAN)
                .putInt(value)
                .array();
        return new Message(commandType, userId, bytes);
    }

    public static Message fromLong(int commandType, int userId, long value) {
        byte[] bytes = ByteBuffer.allocate(8)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(value)
                .array();
        return new Message(commandType, userId, bytes);
    }

    public static Message fromObject(int commandType, int userId, Object obj) throws Exception {
        byte[] json = MAPPER.writeValueAsBytes(obj);  // serializes to JSON bytes
        return new Message(commandType, userId, json);
    }

    public String getPayloadAsString() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public int getPayloadAsInt() {
        return ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public long getPayloadAsLong() {
        return ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    public <T> T getPayloadAsObject(Class<T> type) throws Exception {
        return MAPPER.readValue(payload, type);
    }


    public int getCommandType() { return commandType; }
    public int getUserId()      { return userId; }
    public byte[] getPayload()  { return payload; }
}