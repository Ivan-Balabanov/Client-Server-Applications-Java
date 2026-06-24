package practice3;

import com.fasterxml.jackson.databind.ObjectMapper;
import homework2.Product;
import practice1.Message;
import practice4.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class Processor extends Thread {

    private final Message incoming;

    private static Connection connection;
    private static ProductDao productDao;
    private static ProductService productService;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        try {
            // Ініціалізація In-Memory бази даних H2 (вимагає залежність com.h2database:h2 в pom.xml)
            connection = DriverManager.getConnection("jdbc:h2:mem:warehouse_db;DB_CLOSE_DELAY=-1", "sa", "");
            productDao = new ProductDao(connection);
            productService = new ProductService(productDao);
        } catch (Exception e) {
            System.err.println("[Processor] Failed to initialize JDBC connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Processor(Message incoming) {
        this.incoming = incoming;
    }

    public Message process() {
        int cmd = incoming.getCommandType();
        String payloadStr = incoming.getPayloadAsString();
        String responseJson;

        try {
            switch (cmd) {
                case 101: // CREATE
                    Product toCreate = MAPPER.readValue(payloadStr, Product.class);
                    Product created = productService.createProduct(toCreate);
                    responseJson = MAPPER.writeValueAsString(created);
                    break;

                case 102: // READ
                    int idToRead = Integer.parseInt(payloadStr.trim());
                    var productOpt = productService.getProduct(idToRead);
                    if (productOpt.isPresent()) {
                        responseJson = MAPPER.writeValueAsString(productOpt.get());
                    } else {
                        responseJson = "{\"error\":\"Product not found\"}";
                    }
                    break;

                case 103: // UPDATE
                    Product toUpdate = MAPPER.readValue(payloadStr, Product.class);
                    Product updated = productService.updateProduct(toUpdate.getId(), toUpdate);
                    if (updated != null) {
                        responseJson = MAPPER.writeValueAsString(updated);
                    } else {
                        responseJson = "{\"error\":\"Update failed, product not found\"}";
                    }
                    break;

                case 104: // DELETE
                    int idToDelete = Integer.parseInt(payloadStr.trim());
                    boolean isDeleted = productService.deleteProduct(idToDelete);
                    responseJson = "{\"deleted\":" + isDeleted + "}";
                    break;

                case 105: // SEARCH
                    SearchRequest searchRequest = MAPPER.readValue(payloadStr, SearchRequest.class);
                    List<Product> results = productService.searchProducts(searchRequest);
                    responseJson = MAPPER.writeValueAsString(results);
                    break;

                default: // Ехо-відповідь для зворотної сумісності (cmd з попередніх лаб)
                    responseJson = "{\"ok\":true,\"echo\":\"" + payloadStr + "\"}";
                    break;
            }
        } catch (Exception e) {
            responseJson = "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }

        System.out.println("[Processor] Processed cmd=" + cmd + " -> " + responseJson);
        return Message.fromString(cmd, incoming.getUserId(), responseJson);
    }
}