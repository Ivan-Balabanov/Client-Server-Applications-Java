package practice4;

import homework2.Product;

import java.util.List;
import java.util.Optional;

public class ProductService {
    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    public Product createProduct(Product product) {
        int id = productDao.add(product);
        product.setId(id);
        return product;
    }

    public Optional<Product> getProduct(int id) {
        return productDao.getById(id);
    }

    public Product updateProduct(int id, Product updatedProduct) {
        if (productDao.getById(id).isPresent()) {
            updatedProduct.setId(id);
            productDao.update(updatedProduct);
            return updatedProduct;
        }
        return null;
    }

    public boolean deleteProduct(int id) {
        return productDao.removeById(id);
    }

    public List<Product> searchProducts(SearchRequest request) {
        return productDao.search(
                request.getName(),
                request.getCategory(),
                request.getMinQuantity(),
                request.getMaxQuantity(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getPage(),
                request.getSize()
        );
    }
}