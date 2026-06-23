package practice5;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import homework2.ProductRepository;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class StoreHttpServer {

    private final HttpServer server;
    private final ProductRepository repo = new ProductRepository();

    public StoreHttpServer(int port) throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Public endpoint — no auth
        server.createContext("/login", new LoginHandler());

        // Protected endpoints
        HttpContext productsCtx = server.createContext("/products", new ProductHandler(repo));
        productsCtx.setAuthenticator(new JwtAuthenticator());

        server.setExecutor(Executors.newFixedThreadPool(4));
    }

    public void start() {
        server.start();
        System.out.println("[HTTP Server] Started on port "
                + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
        System.out.println("[HTTP Server] Stopped.");
    }

    public ProductRepository getRepo() { return repo; } // for tests

    public static void main(String[] args) throws Exception {
        new StoreHttpServer(8181).start();
    }
}