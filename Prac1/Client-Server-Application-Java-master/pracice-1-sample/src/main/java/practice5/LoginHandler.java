package practice5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class LoginHandler implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    // Hardcoded users for demo — replace with DB lookup in real app
    private static final Map<String, String> USERS = Map.of(
            "admin", "password123",
            "ivan",  "secret"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            JsonNode body = mapper.readTree(exchange.getRequestBody());
            String login = body.get("login").asText();
            String password = body.get("password").asText();

            String storedPassword = USERS.get(login);
            if (storedPassword == null || !storedPassword.equals(password)) {
                sendResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                return;
            }

            String token   = JwtUtil.generateToken(login);
            String payload = "{\"token\":\"" + token + "\"}";
            sendResponse(exchange, 200, payload);

        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Bad request\"}");
        }
    }

    static void sendResponse(HttpExchange exchange, int status, String body)
            throws IOException {
        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}