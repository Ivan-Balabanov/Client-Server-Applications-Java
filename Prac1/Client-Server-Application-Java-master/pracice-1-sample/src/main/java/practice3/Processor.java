package practice3;

import practice1.Message;

public class Processor extends Thread {

    private final Message incoming;

    public Processor(Message incoming) {
        this.incoming = incoming;
    }

    public Message process() {
        String response = "{\"ok\":true,\"echo\":\""
                + incoming.getPayloadAsString() + "\"}";

        System.out.println("[Processor] Processing cmd= "
                + incoming.getCommandType()
                + " -> " + response);

        return Message.fromString(incoming.getCommandType(), incoming.getUserId(), response);
    }
}