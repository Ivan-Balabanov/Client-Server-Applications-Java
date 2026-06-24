package practice4;

import com.fasterxml.jackson.databind.ObjectMapper;
import homework2.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practice1.Message;
import practice3.Processor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessorIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int TEST_USER_ID = 42;

    @BeforeEach
    void setUp() throws Exception {
        // Очищаємо або перевипускаємо таблицю перед кожним тестом,
        // щоб забезпечити ізоляцію тестових даних.
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:warehouse_db;DB_CLOSE_DELAY=-1", "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS products;");
            stmt.execute("CREATE TABLE products (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "category VARCHAR(255), " +
                    "price DOUBLE, " +
                    "quantity INT" +
                    ");");
        }
    }

    @Test
    void testCreateAndReadProductWorkflow() throws Exception {
        // 1. Тестуємо CREATE (cmd = 101)
        Product laptop = new Product(0, "Asus ROG", "Electronics", 1500.0, 5);
        String createPayload = MAPPER.writeValueAsString(laptop);
        Message createMsg = Message.fromString(101, TEST_USER_ID, createPayload);

        Processor createProcessor = new Processor(createMsg);
        Message createResponse = createProcessor.process();

        assertEquals(101, createResponse.getCommandType());
        Product createdProduct = MAPPER.readValue(createResponse.getPayloadAsString(), Product.class);
        assertTrue(createdProduct.getId() > 0, "ID має бути згенерований базою даних");
        assertEquals("Asus ROG", createdProduct.getName());

        // 2. Тестуємо READ (cmd = 102) за допомогою отриманого ID
        Message readMsg = Message.fromString(102, TEST_USER_ID, String.valueOf(createdProduct.getId()));
        Processor readProcessor = new Processor(readMsg);
        Message readResponse = readProcessor.process();

        Product fetchedProduct = MAPPER.readValue(readResponse.getPayloadAsString(), Product.class);
        assertEquals(createdProduct.getId(), fetchedProduct.getId());
        assertEquals("Electronics", fetchedProduct.getCategory());
    }

    @Test
    void testUpdateProductWorkflow() throws Exception {
        // Спочатку створимо продукт
        Product chair = new Product(0, "Office Chair", "Furniture", 120.0, 10);
        String createPayload = MAPPER.writeValueAsString(chair);
        Message createResponse = new Processor(Message.fromString(101, TEST_USER_ID, createPayload)).process();
        Product created = MAPPER.readValue(createResponse.getPayloadAsString(), Product.class);

        // Тестуємо UPDATE (cmd = 103)
        created.setPrice(145.0);
        created.setQuantity(8);
        String updatePayload = MAPPER.writeValueAsString(created);

        Message updateResponse = new Processor(Message.fromString(103, TEST_USER_ID, updatePayload)).process();
        Product updated = MAPPER.readValue(updateResponse.getPayloadAsString(), Product.class);

        assertEquals(145.0, updated.getPrice());
        assertEquals(8, updated.getQuantity());
    }

    @Test
    void testDeleteProductWorkflow() throws Exception {
        // Створюємо продукт
        Product item = new Product(0, "To Delete", "Temp", 10.0, 1);
        String createPayload = MAPPER.writeValueAsString(item);
        Message createResponse = new Processor(Message.fromString(101, TEST_USER_ID, createPayload)).process();
        Product created = MAPPER.readValue(createResponse.getPayloadAsString(), Product.class);

        // Тестуємо DELETE (cmd = 104)
        Message deleteMsg = Message.fromString(104, TEST_USER_ID, String.valueOf(created.getId()));
        Message deleteResponse = new Processor(deleteMsg).process();

        assertTrue(deleteResponse.getPayloadAsString().contains("\"deleted\":true"));

        // Перевіряємо, що його дійсно немає в БД (READ поверне помилку)
        Message readResponse = new Processor(Message.fromString(102, TEST_USER_ID, String.valueOf(created.getId()))).process();
        assertTrue(readResponse.getPayloadAsString().contains("Product not found"));
    }

    @Test
    void testDynamicSearchAndPaginationWorkflow() throws Exception {
        // Заповнюємо базу даних тестовими товарами
        Product[] products = {
                new Product(0, "Gaming Laptop", "Electronics", 2000.0, 15),
                new Product(0, "iPhone 15", "Electronics", 1000.0, 30),
                new Product(0, "Kitchen Blender", "Appliances", 150.0, 8),
                new Product(0, "Coffee Maker", "Appliances", 250.0, 4),
                new Product(0, "Study Desk", "Furniture", 300.0, 12)
        };

        for (Product p : products) {
            String payload = MAPPER.writeValueAsString(p);
            new Processor(Message.fromString(101, TEST_USER_ID, payload)).process();
        }

        // 1. ТЕСТ ДИНАМІЧНОГО ФІЛЬТРУ: Шукаємо "Electronics" з ціною від 1200
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setCategory("Electronics");
        searchRequest.setMinPrice(1200.0);

        String searchPayload = MAPPER.writeValueAsString(searchRequest);
        Message searchMsg = Message.fromString(105, TEST_USER_ID, searchPayload);
        Message searchResponse = new Processor(searchMsg).process();

        // Перетворюємо JSON-відповідь на список (List) продуктів
        List<Product> results = MAPPER.readValue(
                searchResponse.getPayloadAsString(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, Product.class)
        );

        assertEquals(1, results.size());
        assertEquals("Gaming Laptop", results.get(0).getName());

        // 2. ТЕСТ ПАГІНАЦІЇ: Отримуємо першу сторінку (page=0) розміром у 2 елементи
        SearchRequest pageRequest = new SearchRequest();
        pageRequest.setPage(0);
        pageRequest.setSize(2);

        String pagePayload = MAPPER.writeValueAsString(pageRequest);
        Message pageResponse = new Processor(Message.fromString(105, TEST_USER_ID, pagePayload)).process();

        List<Product> pageResults = MAPPER.readValue(
                pageResponse.getPayloadAsString(),
                MAPPER.getTypeFactory().constructCollectionType(List.class, Product.class)
        );

        assertEquals(2, pageResults.size(), "Має повернути рівно 2 елементи для цієї сторінки");
    }
}