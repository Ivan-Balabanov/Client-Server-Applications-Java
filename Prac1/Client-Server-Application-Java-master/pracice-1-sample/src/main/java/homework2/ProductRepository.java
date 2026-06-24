package homework2;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProductRepository {

    private final ConcurrentHashMap<Integer, Product> store = new ConcurrentHashMap<>();
    private final AtomicInteger idSequence = new AtomicInteger(1);

    public Product findById(int id) {
        return store.get(id);
    }

    public boolean existsByName(String name) {
        return store.values().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }

    public Product create(String name, String category, double price, int quantity) {
        int id = idSequence.getAndIncrement();
        Product p = new Product(id, name, category, price, quantity);
        store.put(id, p);
        return p;
    }

    public boolean update(int id, String name, double price, int quantity) {
        Product p = store.get(id);
        if (p == null) return false;
        p.setName(name);
        p.setPrice(price);
        p.setQuantity(quantity);
        return true;
    }

    public boolean delete(int id) {
        return store.remove(id) != null;
    }

    public Collection<Product> findAll() {
        return store.values();
    }
}