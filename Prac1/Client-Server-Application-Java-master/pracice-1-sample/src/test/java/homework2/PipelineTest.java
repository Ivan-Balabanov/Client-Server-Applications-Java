package homework2;

import org.junit.jupiter.api.Test;
import practice1.Message;
import practice1.Encryptor;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class PipelineTest {

    @Test
    void multiThreadedAddStock() throws Exception {
        Pipeline pipeline = new Pipeline(false); // no FakeReceiver
        pipeline.start();

        Encryptor encryptor = new Encryptor();
        int threads = 10;
        int addEach = 10;

        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    String payload = "{\"product\":\"buckwheat\",\"amount\":" + addEach + "}";
                    Message msg    = Message.fromString(
                            Command.ADD_STOCK.ordinal(), 1, payload);
                    byte[] packet  = encryptor.entry_take(msg);
                    pipeline.getRawQueue().put(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        Thread.sleep(2000);

        int expected = threads * addEach; // 100
        int actual   = pipeline.getWarehouse().getStock("buckwheat");

        assertThat(actual).isEqualTo(expected);

        pipeline.stop();
    }
}