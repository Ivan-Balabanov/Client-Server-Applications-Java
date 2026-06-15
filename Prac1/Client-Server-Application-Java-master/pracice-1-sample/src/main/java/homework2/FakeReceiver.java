package homework2;

import practice1.Encryptor;
import practice1.Message;

import java.util.concurrent.BlockingQueue; //recommended by AI after self-research.
import java.util.Random;

public class FakeReceiver implements ReceiverInterface, Runnable {

    private final BlockingQueue<byte[]> outputQueue;
    private final Encryptor encryptor = new Encryptor();
    private final Random random = new Random();
    private volatile boolean running = true;

    private static final String[] PRODUCTS = {"buckwheat", "rice", "oats"};
    private static final String[] GROUPS   = {"grains", "cereals"};

    public FakeReceiver(BlockingQueue<byte[]> outputQueue) {
        this.outputQueue = outputQueue;
    }

    public void stop() { running = false; }

    @Override
    public void receiveMessage() throws InterruptedException {
        int commandIndex = random.nextInt(Command.values().length);
        Command command = Command.values()[commandIndex];
        String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
        int userId = random.nextInt(100);
        int amount = random.nextInt(50) + 1;

        String payload = buildPayload(command, product, amount);
        Message msg = Message.fromString(commandIndex, userId, payload);

        try {
            byte[] packet = encryptor.entry_take(msg);
            outputQueue.put(packet);
        } catch (Exception e) {
            System.err.println("[FakeReceiver] Encrypt error: " + e.getMessage());
        }
    }

    private String buildPayload(Command command, String product, int amount) {
        return switch (command) {
            case GET_STOCK -> "{\"product\":\"" + product + "\"}";
            case DEDUCT_STOCK -> "{\"product\":\"" + product + "\",\"amount\":" + amount + "}";
            case ADD_STOCK -> "{\"product\":\"" + product + "\",\"amount\":" + amount + "}";
            case ADD_GROUP -> "{\"group\":\"" + GROUPS[random.nextInt(GROUPS.length)] + "\"}";
            case ADD_PRODUCT_NAME -> "{\"group\":\"grains\",\"product\":\"" + product + "\"}";
            case SET_PRICE -> "{\"product\":\"" + product + "\",\"price\":" + (amount * 1.5) + "}";
        };
    }

    @Override
    public void run() {
        while (running) {
            try {
                receiveMessage();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[FakeReceiver] stopped.");
    }
}