package homework2;

import practice1.Decryptor;
import practice1.Message;

import java.util.concurrent.BlockingQueue;

public class DecryptorProcessor implements Runnable {

    private final BlockingQueue<byte[]> inputQueue;
    private final BlockingQueue<DomainMessage> outputQueue;
    private final Decryptor decryptor = new Decryptor();
    private volatile boolean running = true;

    public DecryptorProcessor(BlockingQueue<byte[]> inputQueue, BlockingQueue<DomainMessage> outputQueue) {
        this.inputQueue  = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                byte[] packet = inputQueue.poll(200, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (packet == null) continue;

                Message msg = decryptor.decode(packet);

                Command command = Command.values()[msg.getCommandType()];
                DomainMessage domain = new DomainMessage(
                        command,
                        msg.getUserId(),
                        msg.getPayloadAsString()
                );
                outputQueue.put(domain);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Decryptor] Error: " + e.getMessage());
            }
        }
        System.out.println("[DecryptorProcessor] stopped.");
    }
}