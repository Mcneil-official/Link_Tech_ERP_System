package erp.link_tech_erp.inventory;

public class Product {
    private int id;
    private String name;
    private String category;
    private int stock;
    private double unitPrice;
    private int supplierId;
    private String supplierName;

    public Product(int id, String name, String category, int stock, double unitPrice, int supplierId, String supplierName) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getStock() { return stock; }
    public double getUnitPrice() { return unitPrice; }
    public int getSupplierId() { return supplierId; }
    public String getSupplierName() { return supplierName; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setStock(int stock) { this.stock = stock; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
}
