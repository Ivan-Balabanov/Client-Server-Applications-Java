package practice5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import homework2.ProductRepository;
import homework2.Product;

import java.io.IOException;

public class ProductHandler implements HttpHandler {

    private final ProductRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductHandler(ProductRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath(); // e.g. /products/3

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "PUT" -> handlePut(exchange);
                case "POST" -> handlePost(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> LoginHandler.sendResponse(exchange, 405,
                        "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            LoginHandler.sendResponse(exchange, 500,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // GET /products/{id}
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        if (id < 0) {
            LoginHandler.sendResponse(exchange, 400, "{\"error\":\"Invalid id\"}");
            return;
        }
        Product p = repo.findById(id);
        if (p == null) {
            LoginHandler.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }
        LoginHandler.sendResponse(exchange, 200, toJson(p));
    }

    // PUT /products — create new product
    private void handlePut(HttpExchange exchange) throws IOException {
        JsonNode body = mapper.readTree(exchange.getRequestBody());
        String name   = body.get("name").asText();

        if (repo.existsByName(name)) {
            LoginHandler.sendResponse(exchange, 409,
                    "{\"error\":\"Product with this name already exists\"}");
            return;
        }

        double price    = body.has("price")    ? body.get("price").asDouble()  : 0.0;
        int    quantity = body.has("quantity") ? body.get("quantity").asInt()   : 0;

        Product created = repo.create(name, price, quantity);
        LoginHandler.sendResponse(exchange, 201, toJson(created));
    }

    // POST /products/{id} — update product
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        if (id < 0) {
            LoginHandler.sendResponse(exchange, 400, "{\"error\":\"Invalid id\"}");
            return;
        }

        JsonNode body = mapper.readTree(exchange.getRequestBody());
        String name = body.get("name").asText();
        double price = body.has("price") ? body.get("price").asDouble() : 0.0;
        int quantity = body.has("quantity") ? body.get("quantity").asInt() : 0;

        boolean updated = repo.update(id, name, price, quantity);
        if (!updated) {
            LoginHandler.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }
        LoginHandler.sendResponse(exchange, 200, toJson(repo.findById(id)));
    }

    // DELETE /products/{id}
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        int id = extractId(path);
        if (id < 0) {
            LoginHandler.sendResponse(exchange, 400, "{\"error\":\"Invalid id\"}");
            return;
        }
        boolean deleted = repo.delete(id);
        if (!deleted) {
            LoginHandler.sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }
        LoginHandler.sendResponse(exchange, 200, "{\"ok\":true}");
    }

    // /products/3 -> 3, failure -> -1
    private int extractId(String path) {
        try {
            String[] parts = path.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String toJson(Product p) {
        return "{\"id\":" + p.getId()
                + ",\"name\":\"" + p.getName() + "\""
                + ",\"price\":" + p.getPrice()
                + ",\"quantity\":" + p.getQuantity()
                + "}";
    }
}