package erp.link_tech_erp.integration;

import java.text.DecimalFormat;

import erp.link_tech_erp.inventory.Product;
import erp.link_tech_erp.inventory.ProductRepository;

public class InventorySalesOrderService {

    private final ProductRepository productRepository;

    public InventorySalesOrderService() {
        this(new ProductRepository());
    }

    InventorySalesOrderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public QuotedOrder quoteOrder(String productName, int quantity) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }

        Product product = productRepository.findByName(productName.trim())
            .orElseThrow(() -> new IllegalArgumentException("Product not found in inventory: " + productName.trim()));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                "Insufficient inventory for " + product.getName() + ". Available: " + product.getStock() + ", requested: " + quantity);
        }

        double totalPrice = product.getUnitPrice() * quantity;
        return new QuotedOrder(product.getName(), product.getStock(), product.getUnitPrice(), quantity, totalPrice);
    }

    public record QuotedOrder(String productName, int availableStock, double unitPrice, int quantity, double totalPrice) {
        public String formattedTotalPrice() {
            return new DecimalFormat("₱#,##0.00").format(totalPrice);
        }
    }
}