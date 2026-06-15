package homework2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import practice1.Message;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private final BlockingQueue<DomainMessage> inputQueue;
    private final BlockingQueue<Message> outputQueue;
    private final Warehouse warehouse;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile boolean running = true;

    public Processor(BlockingQueue<DomainMessage> inputQueue,
                     BlockingQueue<Message> outputQueue,
                     Warehouse warehouse) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.warehouse = warehouse;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                DomainMessage msg = inputQueue.poll(
                        200, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (msg == null) continue;

                String response = process(msg);
                System.out.println("[Processor] " + msg.getCommand()
                        + " -> " + response);

                Message reply = Message.fromString(
                        msg.getCommand().ordinal(),
                        msg.getUserId(),
                        response
                );
                outputQueue.put(reply);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Processor] Error: " + e.getMessage());
            }
        }
        System.out.println("[Processor] stopped.");
    }

    private String process(DomainMessage msg) throws Exception {
        JsonNode node = mapper.readTree(msg.getPayload());

        return switch (msg.getCommand()) {
            case GET_STOCK -> {
                String product = node.get("product").asText();
                int qty = warehouse.getStock(product);
                yield "{\"ok\":true,\"product\":\"" + product + "\",\"qty\":" + qty + "}";
            }
            case DEDUCT_STOCK -> {
                String product = node.get("product").asText();
                int amount = node.get("amount").asInt();
                int remaining = warehouse.deductStock(product, amount);
                yield "{\"ok\":true,\"remaining\":" + remaining + "}";
            }
            case ADD_STOCK -> {
                String product = node.get("product").asText();
                int amount = node.get("amount").asInt();
                int total = warehouse.addStock(product, amount);
                yield "{\"ok\":true,\"total\":" + total + "}";
            }
            case ADD_GROUP -> {
                String group = node.get("group").asText();
                warehouse.addGroup(group);
                yield "{\"ok\":true,\"group\":\"" + group + "\"}";
            }
            case ADD_PRODUCT_NAME -> {
                String group   = node.get("group").asText();
                String product = node.get("product").asText();
                warehouse.addProductToGroup(group, product);
                yield "{\"ok\":true}";
            }
            case SET_PRICE -> {
                String product = node.get("product").asText();
                double price   = node.get("price").asDouble();
                warehouse.setPrice(product, price);
                yield "{\"ok\":true,\"price\":" + price + "}";
            }
        };
    }
}