package homework2;

import practice1.Encryptor;
import practice1.Message;

import java.util.concurrent.BlockingQueue;

public class EncryptorSender implements Runnable {

    private final BlockingQueue<Message>  inputQueue;
    private final SenderInterface         sender;
    private final Encryptor encryptor   = new Encryptor();
    private volatile boolean running    = true;

    public EncryptorSender(BlockingQueue<Message> inputQueue,
                           SenderInterface sender) {
        this.inputQueue = inputQueue;
        this.sender     = sender;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                Message msg = inputQueue.poll(
                        200, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (msg == null) continue;

                byte[] packet = encryptor.entry_take(msg);
                sender.sendMessage(packet);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[EncryptorSender] Error: " + e.getMessage());
            }
        }
        System.out.println("[EncryptorSender] stopped.");
    }
}