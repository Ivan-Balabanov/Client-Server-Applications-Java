package practice4;

import homework2.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {
    private final Connection connection;

    public ProductDao(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS products (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255), " +
                "category VARCHAR(255), " +
                "price DOUBLE, " +
                "quantity INT" +
                ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення таблиці", e);
        }
    }

    public int add(Product product) {
        String sql = "INSERT INTO products (name, category, price, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public Optional<Product> getById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void update(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, price = ?, quantity = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getCategory());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setInt(5, product.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean removeById(int id) {
        String sql = "SELECT COUNT(*) FROM products WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(sql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) return false;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }

        String deleteSql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Product> search(String name, String category, Integer minQty, Integer maxQty, Double minPrice, Double maxPrice, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND LOWER(category) = ?");
            params.add(category.toLowerCase());
        }
        if (minQty != null) {
            sql.append(" AND quantity >= ?");
            params.add(minQty);
        }
        if (maxQty != null) {
            sql.append(" AND quantity <= ?");
            params.add(maxQty);
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        List<Product> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("quantity")
        );
    }
}