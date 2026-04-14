package erp.link_tech_erp.inventory;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class InventoryTableModel extends AbstractTableModel {

    private final String[] columns = {"", "ID", "Product Name", "Category", "Stock", "Unit Price", "Supplier", ""};
    private List<Product> products;
    private List<Product> allProducts;
    private final Set<Integer> selected = new HashSet<>();
    
    // Pagination
    private int currentPage = 1;
    private int rowsPerPage = 8;

    public InventoryTableModel(List<Product> products) {
        this.allProducts = new ArrayList<>(products);
        this.products    = new ArrayList<>(products);
    }

    @Override 
    public int getRowCount() { 
        // Return only rows for current page
        int totalRows = products.size();
        int startRow = (currentPage - 1) * rowsPerPage;
        int endRow = Math.min(startRow + rowsPerPage, totalRows);
        return Math.max(0, endRow - startRow);
    }
    
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int col) { return columns[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        // Adjust row index to account for pagination
        int actualRow = (currentPage - 1) * rowsPerPage + row;
        if (actualRow < 0 || actualRow >= products.size()) return null;
        
        Product p = products.get(actualRow);
        switch (col) {
            case 0:
                return selected.contains(actualRow);
            case 1:
                return String.format("#%03d", p.getId());
            case 2:
                return p.getName();
            case 3:
                return p.getCategory();
            case 4:
                return p.getStock();
            case 5:
                return p.getUnitPrice();
            case 6:
                return p.getSupplierName();
            case 7:
                return "actions";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return Boolean.class;
            case 4:
                return Integer.class;
            case 5:
                return Double.class;
            default:
                return String.class;
        }
    }

    @Override public boolean isCellEditable(int row, int col) { return col == 0 || col == 7; }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            int actualRow = (currentPage - 1) * rowsPerPage + row;
            if ((Boolean) value) selected.add(actualRow);
            else selected.remove(actualRow);
            fireTableCellUpdated(row, col);
        }
    }

    public Product getProduct(int row) { 
        int actualRow = toActualRow(row);
        if (actualRow < 0 || actualRow >= products.size()) return null;
        return products.get(actualRow); 
    }

    private int toActualRow(int row) {
        return (currentPage - 1) * rowsPerPage + row;
    }

    public void addProduct(Product p) {
        allProducts.add(p);
        products.add(p);
        fireTableRowsInserted(products.size()-1, products.size()-1);
    }

    public void updateProduct(int row, Product p) {
        int actualRow = toActualRow(row);
        if (actualRow < 0 || actualRow >= products.size()) return;

        int allIdx = allProducts.indexOf(products.get(actualRow));
        if (allIdx >= 0) allProducts.set(allIdx, p);
        products.set(actualRow, p);
        fireTableDataChanged();
    }

    public void removeProduct(int row) {
        int actualRow = row;
        if (actualRow < 0 || actualRow >= products.size()) {
            actualRow = toActualRow(row);
        }
        if (actualRow < 0 || actualRow >= products.size()) return;

        Product p = products.get(actualRow);
        allProducts.remove(p);
        products.remove(actualRow);
        fireTableDataChanged();
    }

    public void filter(String query) {
        filter(query, "All Categories");
    }

    public void filter(String query, String categoryFilter) {
        String q = query.trim().toLowerCase();
        String selectedCategory = categoryFilter == null ? "All Categories" : categoryFilter.trim();
        boolean allCategories = selectedCategory.isEmpty() || "All Categories".equalsIgnoreCase(selectedCategory);

        products = allProducts.stream()
            .filter(p -> allCategories || p.getCategory().equalsIgnoreCase(selectedCategory))
            .filter(p -> q.isEmpty()
                || p.getName().toLowerCase().contains(q)
                || p.getCategory().toLowerCase().contains(q)
                || p.getSupplierName().toLowerCase().contains(q))
            .collect(java.util.stream.Collectors.toList());

        selected.clear();
        currentPage = 1; // Reset to page 1 on filter
        fireTableDataChanged();
    }

    public List<Product> getAllFilteredProducts() {
        return new ArrayList<>(products);
    }

    public List<Product> getLowStock(int threshold) {
        return allProducts.stream()
            .filter(p -> p.getStock() <= threshold)
            .collect(java.util.stream.Collectors.toList());
    }

    public int getTotalProducts()  { return allProducts.size(); }
    public double getTotalValue()  { return allProducts.stream().mapToDouble(p -> p.getStock() * p.getUnitPrice()).sum(); }

    public int getSelectedRow() {
        return selected.isEmpty() ? -1 : selected.iterator().next();
    }
    
    // Pagination methods
    public int getFilteredRowCount() {
        return products.size();
    }
    
    public void setPaginationParams(int page, int rows) {
        this.currentPage = Math.max(1, page);
        this.rowsPerPage = Math.max(1, rows);
        fireTableDataChanged();
    }

    public void replaceProducts(List<Product> newProducts) {
        this.allProducts = new ArrayList<>(newProducts);
        this.products = new ArrayList<>(newProducts);
        this.selected.clear();
        this.currentPage = 1;
        fireTableDataChanged();
    }
}

