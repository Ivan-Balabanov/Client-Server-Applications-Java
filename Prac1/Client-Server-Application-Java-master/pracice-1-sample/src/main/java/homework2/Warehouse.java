package homework2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse {

    // product name -> quantity
    private final ConcurrentHashMap<String, AtomicInteger> stock
            = new ConcurrentHashMap<>();

    // group name -> list of product names (comma-separated, simple)
    private final ConcurrentHashMap<String, String> groups
            = new ConcurrentHashMap<>();

    // product name -> price
    private final ConcurrentHashMap<String, Double> prices
            = new ConcurrentHashMap<>();

    public int getStock(String product) {
        AtomicInteger qty = stock.get(product);
        return qty == null ? 0 : qty.get();
    }

    public int deductStock(String product, int amount) {
        stock.putIfAbsent(product, new AtomicInteger(0));
        return stock.get(product).addAndGet(-amount);
    }

    public int addStock(String product, int amount) {
        stock.putIfAbsent(product, new AtomicInteger(0));
        return stock.get(product).addAndGet(amount);
    }

    public void addGroup(String groupName) {
        groups.putIfAbsent(groupName, "");
    }

    public void addProductToGroup(String groupName, String product) {
        groups.merge(groupName, product,
                (existing, newVal) -> existing.isEmpty()
                        ? newVal
                        : existing + "," + newVal);
        stock.putIfAbsent(product, new AtomicInteger(0));
    }

    public void setPrice(String product, double price) {
        prices.put(product, price);
    }

    public Double getPrice(String product) {
        return prices.get(product);
    }
}