package homework2;

import practice1.Message;

import java.util.concurrent.*;
public class Pipeline {

    private final BlockingQueue<byte[]> rawQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<DomainMessage> domainQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> replyQueue = new LinkedBlockingQueue<>();

    private final Warehouse warehouse = new Warehouse();

    private final FakeReceiver receiver;
    private final DecryptorProcessor decryptor;
    private final Processor processor;
    private final EncryptorSender encryptorSender;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final boolean startReceiver; //added for tests to shut down fakes.

    public Pipeline() {
        receiver = new FakeReceiver(rawQueue);
        decryptor = new DecryptorProcessor(rawQueue, domainQueue);
        processor = new Processor(domainQueue, replyQueue, warehouse);
        encryptorSender = new EncryptorSender(replyQueue, new FakeSender());
        this.startReceiver = true;
    }



    public Pipeline(boolean startReceiver) {
        receiver        = new FakeReceiver(rawQueue);
        decryptor       = new DecryptorProcessor(rawQueue, domainQueue);
        processor       = new Processor(domainQueue, replyQueue, warehouse);
        encryptorSender = new EncryptorSender(replyQueue, new FakeSender());
        this.startReceiver = startReceiver;
    }



    public void start() {
        if (startReceiver) executor.submit(receiver);
        executor.submit(decryptor);
        executor.submit(processor);
        executor.submit(encryptorSender);
        System.out.println("[Pipeline] started.");
    }

    public void stop() throws InterruptedException {
        receiver.stop();
        decryptor.stop();
        processor.stop();
        encryptorSender.stop();

        executor.shutdown();
        System.out.println("[Pipeline] stopped.");
    }

    // Expose rawQueue so tests can inject packets directly
    public BlockingQueue<byte[]> getRawQueue(){
        return rawQueue;
    }
    public Warehouse getWarehouse(){
        return warehouse;
    }

    public static void main(String[] args) throws InterruptedException {
        Pipeline pipeline = new Pipeline();
        pipeline.start();
        Thread.sleep(3000); //buffer time might be added for a good measure
        pipeline.stop();
    }
}
