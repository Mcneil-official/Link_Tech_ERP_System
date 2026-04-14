package erp.link_tech_erp.inventory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

public class InventoryPanel extends JPanel {

    private InventoryTableModel model;
    private JTable table;
    private int lowStockThreshold = 20;
    private final ProductRepository productRepository = new ProductRepository();
    private final SupplierRepository supplierRepository = new SupplierRepository();

    // Pagination
    private int rowsPerPage = 8;
    private int currentPage = 1;
    private int totalPages = 1;
    private JLabel lblPageInfo;
    private JButton btnPrevPage, btnNextPage;
    private java.util.List<JButton> pageButtons = new ArrayList<>();

    // Form fields
    private JTextField tfName, tfStock, tfPrice, tfSearch;
    private JComboBox<String> cbCategory, cbSupplier, cbCategoryFilter;

    // Stat labels
    private JLabel lblTotalProducts, lblLowStock, lblTotalValue;
    private JLabel lblActionStatus;
    private javax.swing.Timer actionStatusTimer;

    public InventoryPanel() {
        setBackground(AppColors.PAGE_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        model = new InventoryTableModel(new ArrayList<>());
        model.setPaginationParams(1, rowsPerPage);
        loadProductsFromDatabase();
        
        // Listen to model changes and auto-refresh stats
        model.addTableModelListener(e -> refreshStats());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildStatsRow());
        body.add(Box.createVerticalStrut(16));
        body.add(buildMainPanel());

        JScrollPane sp = new JScrollPane(body);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        updatePaginationControls();
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Inventory Management");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel sub = new JLabel("Create, edit, and monitor product stock levels.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        sub.setForeground(AppColors.TEXT_SECONDARY);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(3));
        left.add(sub);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ── Stats row ──────────────────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        lblTotalProducts = new JLabel(String.valueOf(model.getTotalProducts()));
        lblLowStock      = new JLabel(String.valueOf(model.getLowStock(lowStockThreshold).size()));
        lblTotalValue    = new JLabel(String.format("$%.1fk", model.getTotalValue() / 1000));

        row.add(makeStatCard("Total Products", lblTotalProducts, "In stock", AppColors.BADGE_SUCCESS_BG, AppColors.BADGE_SUCCESS_FG));
        row.add(makeStatCard("Low Stock Items", lblLowStock, "Needs reorder", AppColors.BADGE_DANGER_BG, AppColors.BADGE_DANGER_FG));
        row.add(makeStatCard("Total Value", lblTotalValue, "+12% MTD", AppColors.BADGE_SUCCESS_BG, AppColors.BADGE_SUCCESS_FG));
        row.add(makeStatCard("Suppliers", new JLabel("12"), "2 pending", AppColors.BADGE_WARNING_BG, AppColors.BADGE_WARNING_FG));

        return row;
    }

    private JPanel makeStatCard(String label, JLabel valueLbl, String badge, Color badgeBg, Color badgeFg) {
        UIComponents.RoundedPanel card = new UIComponents.RoundedPanel(12);
        card.setBackground(AppColors.CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
        valueLbl.setForeground(AppColors.TEXT_PRIMARY);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        UIComponents.PillLabel badgeLbl = new UIComponents.PillLabel(badge, badgeBg, badgeFg);
        badgeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(lbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(valueLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(badgeLbl);
        card.add(inner);
        return card;
    }

    // ── Main panel (form + table) ──────────────────────────────────────────────
    private JPanel buildMainPanel() {
        UIComponents.RoundedPanel panel = new UIComponents.RoundedPanel(12);
        panel.setBackground(AppColors.CARD_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        panel.add(buildForm());
        panel.add(buildDivider());
        panel.add(buildActionBar());
        panel.add(buildDivider());
        panel.add(buildTableHeader());
        panel.add(buildTable());
        panel.add(buildDivider());
        panel.add(buildPaginationControls());
        return panel;
    }

    // ── Form ───────────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel p = new JPanel(new GridLayout(2, 3, 12, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        tfName     = UIComponents.createTextField("e.g. 24-inch LED Monitor");
        cbCategory = UIComponents.createComboBox(new String[]{"Monitor","Laptop","Phone","Tablet","Accessory"});
        tfStock    = UIComponents.createTextField("0");
        tfPrice    = UIComponents.createTextField("0.00");
        cbSupplier = UIComponents.createComboBox(getSupplierChoices());
        JTextField tfIdDisplay = UIComponents.createTextField("Auto-generated");
        tfIdDisplay.setEditable(false);
        tfIdDisplay.setForeground(AppColors.TEXT_SECONDARY);

        p.add(labeledField("Product Name", tfName));
        p.add(labeledField("Category", cbCategory));
        p.add(labeledField("Stock Quantity", tfStock));
        p.add(labeledField("Unit Price ($)", tfPrice));
        p.add(labeledField("Supplier", cbSupplier));
        p.add(labeledField("Product ID", tfIdDisplay));

        return p;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(AppColors.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // ── Action bar ─────────────────────────────────────────────────────────────
    private JPanel buildActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 12, 0, 12));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JButton btnAdd    = UIComponents.createPrimaryButton("+ Add Product");
        JButton btnUpdate = UIComponents.createSecondaryButton("Update Selected");
        JButton btnDelete = UIComponents.createDangerButton("Delete Selected");
        JButton btnClear  = UIComponents.createSecondaryButton("Clear");
        JButton btnRefresh = UIComponents.createSecondaryButton("Refresh");

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> refreshTable());

        p.add(btnAdd);
        p.add(btnUpdate);
        p.add(btnDelete);
        p.add(btnClear);

        // Threshold
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
        right.setOpaque(false);
        JLabel threshLbl = new JLabel("Low-stock threshold:");
        threshLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        threshLbl.setForeground(AppColors.TEXT_SECONDARY);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(lowStockThreshold, 1, 9999, 1));
        spinner.setPreferredSize(new Dimension(80, 34));
        spinner.addChangeListener(e -> {
            lowStockThreshold = (int) spinner.getValue();
            table.repaint();
            refreshStats();
        });
        JButton btnAlert = UIComponents.createSecondaryButton("Low-Stock Alert");
        btnAlert.addActionListener(e -> showLowStockAlert());

        right.add(threshLbl);
        right.add(spinner);
        right.add(btnAlert);
        right.add(btnRefresh);

        lblActionStatus = new JLabel(" ");
        lblActionStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblActionStatus.setForeground(new Color(22, 163, 74));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        center.setOpaque(false);
        center.add(lblActionStatus);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        bar.add(p, BorderLayout.WEST);
        bar.add(center, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Table toolbar ──────────────────────────────────────────────────────────
    private JPanel buildTableHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 20, 12, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JLabel title = new JLabel("Products");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolbar.setOpaque(false);

        tfSearch = UIComponents.createTextField("Search products...");
        tfSearch.setPreferredSize(new Dimension(260, 38));
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyTableFilters(); }
            public void removeUpdate(DocumentEvent e)  { applyTableFilters(); }
            public void changedUpdate(DocumentEvent e) { applyTableFilters(); }
        });

        cbCategoryFilter = UIComponents.createComboBox(new String[]{"All Categories", "Monitor", "Laptop", "Phone", "Tablet", "Accessory"});
        cbCategoryFilter.setPreferredSize(new Dimension(190, 38));
        cbCategoryFilter.addActionListener(e -> applyTableFilters());

        JButton btnFilter = UIComponents.createSecondaryButton("Filter");
        btnFilter.addActionListener(e -> applyTableFilters());
        JButton btnClearFilters = UIComponents.createSecondaryButton("✕");
        btnClearFilters.setPreferredSize(new Dimension(38, 38));
        btnClearFilters.setToolTipText("Clear filters");
        btnClearFilters.addActionListener(e -> clearFilters());
        JButton btnExport = UIComponents.createSecondaryButton("Export");

        toolbar.add(tfSearch);
        toolbar.add(cbCategoryFilter);
        toolbar.add(btnFilter);
        toolbar.add(btnClearFilters);
        toolbar.add(btnExport);

        p.add(title, BorderLayout.WEST);
        p.add(toolbar, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildDivider(), BorderLayout.NORTH);
        wrapper.add(p, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Table ──────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        table = new JTable(model);
        table.setBackground(AppColors.CARD_BG);
        table.setGridColor(AppColors.BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(AppColors.TEXT_PRIMARY);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColors.TABLE_HEADER_BG);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));
        header.setReorderingAllowed(false);

        // Column widths (used as weighting hints for auto-resize)
        int[] widths = {36, 84, 320, 140, 120, 130, 220, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(1).setMaxWidth(90);

        // Renderers
        table.getColumnModel().getColumn(1).setCellRenderer(new TableRenderers.IdRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new TableRenderers.NameRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new TableRenderers.CategoryRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new TableRenderers.StockRenderer(lowStockThreshold));
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.PriceRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new TableRenderers.SupplierRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new TableRenderers.ActionsRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(
            new TableRenderers.ActionsEditor(new JCheckBox(),
                row -> {
                    int displayRow = Integer.parseInt(row);
                    loadRowIntoForm(displayRow);
                },
                row -> {
                    int displayRow = Integer.parseInt(row);
                    int actualRow = (currentPage - 1) * rowsPerPage + displayRow;
                    confirmDeleteRow(actualRow);
                })
        );

        // Row hover
        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) { table.repaint(); }
        });

        // Row selection -> populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                loadRowIntoForm(table.getSelectedRow());
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);

        int visibleRows = Math.max(1, rowsPerPage);
        int rowHeight = Math.max(1, table.getRowHeight());
        int headerHeight = Math.max(32, table.getTableHeader().getPreferredSize().height);
        int tableHeight = (visibleRows * rowHeight) + headerHeight + 2;

        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(0, tableHeight));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, tableHeight));
        sp.setMinimumSize(new Dimension(0, tableHeight));
        return sp;
    }

    // ── Divider ────────────────────────────────────────────────────────────────
    private JPanel buildDivider() {
        JPanel d = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(AppColors.BORDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setPreferredSize(new Dimension(0, 1));
        return d;
    }

    // ── Pagination controls ────────────────────────────────────────────────────
    private JPanel buildPaginationControls() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 20, 12, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        // Left side: page info
        lblPageInfo = new JLabel("Page 1 of 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblPageInfo.setForeground(AppColors.TEXT_SECONDARY);
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(lblPageInfo);

        // Center: pagination buttons (Prev, 1, 2, 3, Next)
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pagination.setOpaque(false);

        btnPrevPage = UIComponents.createSecondaryButton("← Prev");
        btnPrevPage.setPreferredSize(new Dimension(80, 34));
        btnPrevPage.addActionListener(e -> goToPreviousPage());
        pagination.add(btnPrevPage);

        // Number buttons will be dynamically created
        pagination.add(Box.createHorizontalStrut(6));

        btnNextPage = UIComponents.createSecondaryButton("Next →");
        btnNextPage.setPreferredSize(new Dimension(80, 34));
        btnNextPage.addActionListener(e -> goToNextPage());
        pagination.add(btnNextPage);

        p.add(infoPanel, BorderLayout.WEST);
        p.add(pagination, BorderLayout.CENTER);

        return p;
    }

    private void updatePaginationControls() {
        // Calculate total pages based on filtered model data
        int totalRows = model.getFilteredRowCount();
        totalPages = Math.max(1, (totalRows + rowsPerPage - 1) / rowsPerPage);

        // Clamp current page
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        // Update page info label
        if (lblPageInfo != null) {
            lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        }

        // Update prev/next button states
        if (btnPrevPage != null) btnPrevPage.setEnabled(currentPage > 1);
        if (btnNextPage != null) btnNextPage.setEnabled(currentPage < totalPages);

        // Update table to show current page data
        if (model != null) {
            model.setPaginationParams(currentPage, rowsPerPage);
            table.revalidate();
            table.repaint();
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePaginationControls();
        }
    }

    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePaginationControls();
        }
    }

    // ── Form logic ──────────────────────────────────────────────────────────────
    private void addProduct() {
        if (tfName.getText().trim().isEmpty()) {
            showError("Product name is required.");
            return;
        }
        try {
            int stock    = Integer.parseInt(tfStock.getText().trim());
            double price = Double.parseDouble(tfPrice.getText().trim());
            String supplier = (String) cbSupplier.getSelectedItem();
            Product p = new Product(0, tfName.getText().trim(),
                (String) cbCategory.getSelectedItem(), stock, price, 1, supplier);
            productRepository.insert(p);
            loadProductsFromDatabase();
            currentPage = 1; // Reset to first page
            applyTableFilters();
            clearForm();
            refreshStats();
            showActionStatus("Product added successfully.", new Color(22, 163, 74));
        } catch (NumberFormatException ex) {
            showError("Stock and price must be valid numbers.");
        } catch (RuntimeException ex) {
            showError("Could not save product to Supabase: " + ex.getMessage());
        }
    }

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select a row to update."); return; }
        try {
            int stock    = Integer.parseInt(tfStock.getText().trim());
            double price = Double.parseDouble(tfPrice.getText().trim());
            Product existing = model.getProduct(row);
            Product updated  = new Product(existing.getId(), tfName.getText().trim(),
                (String) cbCategory.getSelectedItem(), stock, price,
                existing.getSupplierId(), (String) cbSupplier.getSelectedItem());
            productRepository.update(updated);
            loadProductsFromDatabase();
            applyTableFilters();
            refreshStats();
            showActionStatus("Product updated successfully.", new Color(22, 163, 74));
        } catch (NumberFormatException ex) {
            showError("Stock and price must be valid numbers.");
        } catch (RuntimeException ex) {
            showError("Could not update product in Supabase: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Select a row to delete."); return; }
        // Convert display row to actual model row
        int actualRow = (currentPage - 1) * rowsPerPage + row;
        confirmDeleteRow(actualRow);
    }

    private void confirmDeleteRow(int row) {
        if (row < 0 || row >= model.getFilteredRowCount()) {
            showError("Invalid row.");
            return;
        }
        // Get the product using internal access
        List<Product> allFiltered = model.getAllFilteredProducts();
        if (row >= allFiltered.size()) return;
        
        Product p = allFiltered.get(row);
        int res = JOptionPane.showConfirmDialog(this,
            "Delete \"" + p.getName() + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            try {
                productRepository.deleteById(p.getId());
                loadProductsFromDatabase();
            } catch (RuntimeException ex) {
                showError("Could not delete product from Supabase: " + ex.getMessage());
                return;
            }
            if (currentPage > 1) {
                int totalPages = Math.max(1, (model.getFilteredRowCount() + rowsPerPage - 1) / rowsPerPage);
                if (currentPage > totalPages) currentPage = totalPages;
            }
            applyTableFilters();
            clearForm();
            refreshStats();
        }
    }

    private void loadRowIntoForm(int row) {
        if (row < 0 || row >= model.getRowCount()) return;
        Product p = model.getProduct(row);
        tfName.setText(p.getName());
        tfStock.setText(String.valueOf(p.getStock()));
        tfPrice.setText(String.format("%.2f", p.getUnitPrice()));
        cbCategory.setSelectedItem(p.getCategory());
        cbSupplier.setSelectedItem(p.getSupplierName());
    }

    private void clearForm() {
        tfName.setText(""); tfStock.setText(""); tfPrice.setText("");
        cbCategory.setSelectedIndex(0); cbSupplier.setSelectedIndex(0);
        table.clearSelection();
    }

    private void refreshTable() {
        tfSearch.setText("");
        if (cbCategoryFilter != null) cbCategoryFilter.setSelectedIndex(0);
        applyTableFilters();
    }

    private void applyTableFilters() {
        String query = tfSearch == null ? "" : tfSearch.getText();
        String category = cbCategoryFilter == null ? "All Categories" : (String) cbCategoryFilter.getSelectedItem();
        model.filter(query, category);
        currentPage = 1; // Reset to first page on filter
        updatePaginationControls();
    }

    private void clearFilters() {
        if (tfSearch != null) tfSearch.setText("");
        if (cbCategoryFilter != null) cbCategoryFilter.setSelectedIndex(0);
        applyTableFilters();
    }

    private void refreshStats() {
        lblTotalProducts.setText(String.valueOf(model.getTotalProducts()));
        lblLowStock.setText(String.valueOf(model.getLowStock(lowStockThreshold).size()));
        lblTotalValue.setText(String.format("$%.1fk", model.getTotalValue() / 1000));
    }

    private void showLowStockAlert() {
        List<Product> low = model.getLowStock(lowStockThreshold);
        if (low.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products below threshold " + lowStockThreshold + ".",
                "Low Stock Alert", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder("Products below threshold (" + lowStockThreshold + "):\n\n");
            for (Product p : low) sb.append("• ").append(p.getName()).append(" — ").append(p.getStock()).append(" units\n");
            JOptionPane.showMessageDialog(this, sb.toString(), "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showError(String msg) {
        showActionStatus(msg, new Color(220, 38, 38));
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showActionStatus(String message, Color color) {
        if (lblActionStatus == null) {
            return;
        }

        lblActionStatus.setText(message);
        lblActionStatus.setForeground(color);

        if (actionStatusTimer != null) {
            actionStatusTimer.stop();
        }

        actionStatusTimer = new javax.swing.Timer(2500, e -> lblActionStatus.setText(" "));
        actionStatusTimer.setRepeats(false);
        actionStatusTimer.start();
    }

    private void loadProductsFromDatabase() {
        try {
            List<Product> dbProducts = productRepository.findAll();
            model.replaceProducts(dbProducts);
        } catch (Exception exception) {
            if (model.getTotalProducts() == 0) {
                model.replaceProducts(sampleData());
            }
            showActionStatus("Using local fallback data. Supabase unavailable.", new Color(220, 38, 38));
        }
    }

    private String[] getSupplierChoices() {
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            if (suppliers.isEmpty()) {
                return new String[]{"TechSource Distribution", "Digital Hub Components", "GlobalTech Supply"};
            }
            return suppliers.stream().map(Supplier::getName).toArray(String[]::new);
        } catch (Exception exception) {
            return new String[]{"TechSource Distribution", "Digital Hub Components", "GlobalTech Supply"};
        }
    }

    // ── Sample data ────────────────────────────────────────────────────────────
    private List<Product> sampleData() {
        return new ArrayList<>(Arrays.asList(
            new Product(1, "24-inch LED Monitor",     "Monitor",   50,  145.00, 1, "TechSource Distribution"),
            new Product(2, "Business Laptop 14\"",    "Laptop",    20,  799.00, 2, "Digital Hub Components"),
            new Product(3, "Wireless Mouse Pro",       "Accessory", 134,  39.00, 1, "TechSource Distribution"),
            new Product(4, "Mechanical Keyboard",      "Accessory",  15,  89.00, 1, "TechSource Distribution"),
            new Product(5, "iPhone 15 Pro",            "Phone",      8, 1099.00, 2, "Digital Hub Components"),
            new Product(6, "iPad Air 5",               "Tablet",    22,  749.00, 3, "GlobalTech Supply"),
            new Product(7, "USB-C Hub 7-in-1",         "Accessory", 200,  45.00, 1, "TechSource Distribution"),
            new Product(8, "27-inch 4K Monitor",       "Monitor",    5,  499.00, 2, "Digital Hub Components"),
            new Product(9, "Gaming Laptop 17\"",       "Laptop",    12, 1299.00, 3, "GlobalTech Supply"),
            new Product(10, "Android Flagship X",       "Phone",     18,  899.00, 4, "Nexus Wholesale Partners"),
            new Product(11, "Bluetooth Speaker Mini",   "Accessory", 76,   59.00, 5, "Vertex Electronics Hub"),
            new Product(12, "Portable SSD 1TB",         "Accessory", 48,  129.00, 6, "Prime Components Group"),
            new Product(13, "Ultrabook 13\"",           "Laptop",    16, 1099.00, 2, "Digital Hub Components"),
            new Product(14, "Tablet Pro 11",            "Tablet",    14,  899.00, 3, "GlobalTech Supply"),
            new Product(15, "USB-C Fast Charger 65W",   "Accessory", 220,  35.00, 1, "TechSource Distribution"),
            new Product(16, "Noise Cancelling Headset", "Accessory", 58,  199.00, 7, "Quantum Device Traders"),
            new Product(17, "Curved Monitor 34\"",      "Monitor",    9,  799.00, 4, "Nexus Wholesale Partners"),
            new Product(18, "Budget Laptop 15\"",       "Laptop",    27,  649.00, 8, "Titan Industrial Supplies"),
            new Product(19, "Smartphone Lite",          "Phone",     42,  499.00, 5, "Vertex Electronics Hub"),
            new Product(20, "Graphic Tablet",           "Tablet",    11,  329.00, 3, "GlobalTech Supply"),
            new Product(21, "Wireless Router AX",       "Accessory", 36,  149.00, 6, "Prime Components Group"),
            new Product(22, "Docking Station Pro",      "Accessory", 33,  179.00, 1, "TechSource Distribution"),
            new Product(23, "OLED Monitor 32\"",        "Monitor",    6,  999.00, 2, "Digital Hub Components"),
            new Product(24, "Enterprise Laptop 16\"",   "Laptop",    13, 1499.00, 7, "Quantum Device Traders"),
            new Product(25, "Rugged Tablet",            "Tablet",     7, 1199.00, 8, "Titan Industrial Supplies"),
            new Product(26, "Conference Speakerphone",  "Accessory", 24,  249.00, 4, "Nexus Wholesale Partners"),
            new Product(27, "Wireless Keyboard Slim",   "Accessory", 96,   49.00, 1, "TechSource Distribution"),
            new Product(28, "4K Webcam",                "Accessory", 63,   89.00, 5, "Vertex Electronics Hub"),
            new Product(29, "Phone Stand MagSafe",      "Accessory", 154,  29.00, 6, "Prime Components Group"),
            new Product(30, "Dual Monitor Arm",         "Accessory", 41,  119.00, 8, "Titan Industrial Supplies")
        ));
    }
}
