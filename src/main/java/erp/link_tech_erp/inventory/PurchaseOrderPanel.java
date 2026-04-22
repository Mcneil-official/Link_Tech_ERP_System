package erp.link_tech_erp.inventory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import erp.link_tech_erp.integration.ProcurementFinanceExpenseSyncService;

public class PurchaseOrderPanel extends JPanel {

    private DefaultTableModel orderModel;
    private JTable orderTable;
    private JTextField tfOrderNum, tfQuantity, tfUnitPrice, tfSearch;
    private JComboBox<String> cbSupplier, cbStatus, cbStatusFilter;
    private JLabel lblTotalOrders, lblPendingValue;
    private List<PurchaseOrder> orders;
    private List<PurchaseOrder> filteredOrders;
    private final PurchaseOrderRepository orderRepository = new PurchaseOrderRepository();
    private final SupplierRepository supplierRepository = new SupplierRepository();
    private ProcurementFinanceExpenseSyncService procurementFinanceExpenseSyncService;

    public PurchaseOrderPanel() {
        setBackground(AppColors.PAGE_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        orders = loadOrdersFromDatabase();
        filteredOrders = new ArrayList<>(orders);

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
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Purchase Orders");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel sub = new JLabel("Create and track purchase orders from suppliers.");
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

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        lblTotalOrders = new JLabel(String.valueOf(orders.size()));
        lblPendingValue = new JLabel(String.format("$%.1fk", calculatePendingValue() / 1000));

        row.add(makeStatCard("Total Orders", lblTotalOrders, "Active", AppColors.BADGE_SUCCESS_BG, AppColors.BADGE_SUCCESS_FG));
        row.add(makeStatCard("Pending Value", lblPendingValue, "In transit", AppColors.BADGE_WARNING_BG, AppColors.BADGE_WARNING_FG));

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
        return panel;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridLayout(2, 3, 12, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        tfOrderNum = UIComponents.createTextField("Auto-generated");
        tfOrderNum.setEditable(false);
        tfOrderNum.setForeground(AppColors.TEXT_SECONDARY);
        cbSupplier = UIComponents.createComboBox(getSupplierChoices());
        tfQuantity = UIComponents.createTextField("0");
        tfUnitPrice = UIComponents.createTextField("0.00");
        cbStatus = UIComponents.createComboBox(new String[]{"Pending", "Confirmed", "Shipped", "Delivered"});
        JTextField tfDateDisplay = UIComponents.createTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        tfDateDisplay.setEditable(false);
        tfDateDisplay.setForeground(AppColors.TEXT_SECONDARY);

        p.add(labeledField("Order Number", tfOrderNum));
        p.add(labeledField("Supplier", cbSupplier));
        p.add(labeledField("Quantity", tfQuantity));
        p.add(labeledField("Unit Price ($)", tfUnitPrice));
        p.add(labeledField("Status", cbStatus));
        p.add(labeledField("Order Date", tfDateDisplay));

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

    private JPanel buildActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 12, 0, 12));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JButton btnAdd = UIComponents.createPrimaryButton("+ Create Order");
        JButton btnUpdate = UIComponents.createSecondaryButton("Update Selected");
        JButton btnDelete = UIComponents.createDangerButton("Cancel Order");
        JButton btnClear = UIComponents.createSecondaryButton("Clear");

        btnAdd.addActionListener(e -> addOrder());
        btnUpdate.addActionListener(e -> updateSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnClear.addActionListener(e -> clearForm());

        p.add(btnAdd);
        p.add(btnUpdate);
        p.add(btnDelete);
        p.add(btnClear);

        return p;
    }

    private JPanel buildTableHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 20, 12, 20));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JLabel title = new JLabel("Orders");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolbar.setOpaque(false);

        tfSearch = UIComponents.createTextField("Search orders...");
        tfSearch.setPreferredSize(new Dimension(220, 38));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });

        cbStatusFilter = UIComponents.createComboBox(new String[]{
            "All Statuses", "Pending", "Confirmed", "Shipped", "Delivered", "Completed"
        });
        cbStatusFilter.setPreferredSize(new Dimension(170, 38));
        cbStatusFilter.addActionListener(e -> refreshTable());

        JButton btnExport = UIComponents.createSecondaryButton("Export");
        toolbar.add(tfSearch);
        toolbar.add(cbStatusFilter);
        toolbar.add(btnExport);

        p.add(title, BorderLayout.WEST);
        p.add(toolbar, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildDivider(), BorderLayout.NORTH);
        wrapper.add(p, BorderLayout.CENTER);
        return wrapper;
    }

    private JScrollPane buildTable() {
        orderModel = new DefaultTableModel(new String[]{"Order #", "Supplier", "Qty", "Unit Price", "Total", "Status", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        orderTable = new JTable(orderModel);
        orderTable.setBackground(AppColors.CARD_BG);
        orderTable.setGridColor(AppColors.BORDER);
        orderTable.setShowVerticalLines(false);
        orderTable.setShowHorizontalLines(true);
        orderTable.setIntercellSpacing(new Dimension(0, 0));
        orderTable.setFocusable(false);
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        orderTable.setSelectionBackground(new Color(232, 240, 254));
        orderTable.setSelectionForeground(AppColors.TEXT_PRIMARY);

        JTableHeader header = orderTable.getTableHeader();
        header.setBackground(AppColors.TABLE_HEADER_BG);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));
        header.setReorderingAllowed(false);

        int[] widths = {100, 180, 80, 120, 120, 140, 140};
        for (int i = 0; i < widths.length; i++) {
            orderTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() >= 0) {
                loadRowIntoForm(orderTable.getSelectedRow());
            }
        });

        refreshTable();

        JScrollPane sp = new JScrollPane(orderTable);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return sp;
    }

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

    private void addOrder() {
        String supplier = cbSupplier == null ? null : (String) cbSupplier.getSelectedItem();
        if (supplier == null || supplier.trim().isEmpty() || "Select supplier...".equalsIgnoreCase(supplier)) {
            showError("Supplier is required.");
            return;
        }
        try {
            int qty = Integer.parseInt(tfQuantity.getText().trim());
            double price = Double.parseDouble(tfUnitPrice.getText().trim());
            PurchaseOrder o = new PurchaseOrder(0, supplier,
                qty, price, (String) cbStatus.getSelectedItem(), LocalDate.now());
            PurchaseOrder savedOrder = orderRepository.insert(o);
            syncConfirmedPurchaseOrderExpense(savedOrder);
            orders = loadOrdersFromDatabase();
            clearForm();
            refreshTable();
            refreshStats();
        } catch (NumberFormatException ex) {
            showError("Quantity and Price must be valid numbers.");
        } catch (RuntimeException ex) {
            showError("Could not create order in Supabase: " + ex.getMessage());
        }
    }

    private void updateSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { showError("Select a row to update."); return; }
        try {
            if (row >= filteredOrders.size()) { showError("Invalid selection."); return; }
            PurchaseOrder o = filteredOrders.get(row);
            String supplier = cbSupplier == null ? null : (String) cbSupplier.getSelectedItem();
            if (supplier == null || supplier.trim().isEmpty() || "Select supplier...".equalsIgnoreCase(supplier)) {
                showError("Supplier is required.");
                return;
            }
            o.setSupplier(supplier);
            o.setQuantity(Integer.parseInt(tfQuantity.getText().trim()));
            o.setUnitPrice(Double.parseDouble(tfUnitPrice.getText().trim()));
            o.setStatus((String) cbStatus.getSelectedItem());
            orderRepository.update(o);
            syncConfirmedPurchaseOrderExpense(o);
            orders = loadOrdersFromDatabase();
            refreshTable();
            clearForm();
            refreshStats();
        } catch (NumberFormatException ex) {
            showError("Invalid input.");
        } catch (RuntimeException ex) {
            showError("Could not update order in Supabase: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) { showError("Select a row to delete."); return; }
        if (row >= filteredOrders.size()) { showError("Invalid selection."); return; }
        PurchaseOrder o = filteredOrders.get(row);
        int res = JOptionPane.showConfirmDialog(this,
            "Cancel order #" + o.getOrderId() + "?", "Confirm Cancel",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            try {
                orderRepository.deleteById(o.getOrderId());
                orders = loadOrdersFromDatabase();
                clearForm();
                refreshTable();
                refreshStats();
            } catch (RuntimeException ex) {
                showError("Could not cancel order in Supabase: " + ex.getMessage());
            }
        }
    }

    private void loadRowIntoForm(int row) {
        if (row < 0 || row >= filteredOrders.size()) return;
        PurchaseOrder o = filteredOrders.get(row);
        tfOrderNum.setText("PO-" + String.format("%04d", o.getOrderId()));
        cbSupplier.setSelectedItem(o.getSupplier());
        tfQuantity.setText(String.valueOf(o.getQuantity()));
        tfUnitPrice.setText(String.format("%.2f", o.getUnitPrice()));
        cbStatus.setSelectedItem(o.getStatus());
    }

    private void clearForm() {
        tfOrderNum.setText("");
        cbSupplier.setSelectedIndex(0);
        tfQuantity.setText("");
        tfUnitPrice.setText("");
        cbStatus.setSelectedIndex(0);
        orderTable.clearSelection();
    }

    private void refreshTable() {
        orderModel.setRowCount(0);
        String query = tfSearch == null ? "" : tfSearch.getText().toLowerCase();
        String selectedStatus = cbStatusFilter == null ? "All Statuses" : (String) cbStatusFilter.getSelectedItem();
        boolean allStatuses = selectedStatus == null || "All Statuses".equalsIgnoreCase(selectedStatus);
        boolean completedAlias = "Completed".equalsIgnoreCase(selectedStatus);

        filteredOrders = new ArrayList<>();
        for (PurchaseOrder o : orders) {
            boolean matchesQuery = query.isEmpty()
                || o.getSupplier().toLowerCase().contains(query)
                || String.valueOf(o.getOrderId()).contains(query)
                || o.getStatus().toLowerCase().contains(query);

            boolean matchesStatus = allStatuses
                || (completedAlias && "Delivered".equalsIgnoreCase(o.getStatus()))
                || o.getStatus().equalsIgnoreCase(selectedStatus);

            if (matchesQuery && matchesStatus) {
                filteredOrders.add(o);
                double total = o.getQuantity() * o.getUnitPrice();
                orderModel.addRow(new Object[]{"PO-" + String.format("%04d", o.getOrderId()), o.getSupplier(), o.getQuantity(), 
                    String.format("$%.2f", o.getUnitPrice()), String.format("$%.2f", total), o.getStatus(), o.getOrderDate()});
            }
        }
    }

    private void refreshStats() {
        lblTotalOrders.setText(String.valueOf(orders.size()));
        lblPendingValue.setText(String.format("$%.1fk", calculatePendingValue() / 1000));
    }

    private double calculatePendingValue() {
        return orders.stream()
            .filter(o -> !o.getStatus().equals("Delivered"))
            .mapToDouble(o -> o.getQuantity() * o.getUnitPrice())
            .sum();
    }

    private void syncConfirmedPurchaseOrderExpense(PurchaseOrder order) {
        if (order == null || !"Confirmed".equalsIgnoreCase(order.getStatus())) {
            return;
        }

        try {
            getProcurementFinanceExpenseSyncService().syncConfirmedPurchaseOrder(
                order.getOrderId(),
                order.getSupplier(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getOrderDate()
            );
        } catch (RuntimeException ex) {
            showError("Order was saved, but finance expense sync failed: " + ex.getMessage());
        }
    }

    private ProcurementFinanceExpenseSyncService getProcurementFinanceExpenseSyncService() {
        if (procurementFinanceExpenseSyncService == null) {
            procurementFinanceExpenseSyncService = ProcurementFinanceExpenseSyncService.createDefault();
        }
        return procurementFinanceExpenseSyncService;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private List<PurchaseOrder> loadOrdersFromDatabase() {
        try {
            return orderRepository.findAll();
        } catch (Exception exception) {
            return sampleOrders();
        }
    }

    private String[] getSupplierChoices() {
        List<String> choices = new ArrayList<>();
        choices.add("Select supplier...");
        try {
            List<Supplier> suppliers = supplierRepository.findAll();
            if (!suppliers.isEmpty()) {
                for (Supplier supplier : suppliers) {
                    choices.add(supplier.getName());
                }
                return choices.toArray(new String[0]);
            }
        } catch (Exception ignored) {
        }

        choices.addAll(Arrays.asList(
            "TechSource Distribution",
            "Digital Hub Components",
            "GlobalTech Supply",
            "Nexus Wholesale Partners",
            "Vertex Electronics Hub",
            "Prime Components Group",
            "Quantum Device Traders",
            "Titan Industrial Supplies",
            "Aurora Hardware Networks",
            "BluePeak Distribution",
            "CoreWave Technologies",
            "EverGrid Supply Co."
        ));
        return choices.toArray(new String[0]);
    }

    private List<PurchaseOrder> sampleOrders() {
        List<PurchaseOrder> list = new ArrayList<>();
        list.add(new PurchaseOrder(1, "TechSource Distribution", 50, 145.00, "Delivered", LocalDate.now().minusDays(5)));
        list.add(new PurchaseOrder(2, "Digital Hub Components", 20, 799.00, "Shipped", LocalDate.now().minusDays(2)));
        list.add(new PurchaseOrder(3, "GlobalTech Supply", 100, 45.00, "Confirmed", LocalDate.now().minusDays(1)));
        list.add(new PurchaseOrder(4, "Nexus Wholesale Partners", 35, 499.00, "Pending", LocalDate.now().minusDays(9)));
        list.add(new PurchaseOrder(5, "Vertex Electronics Hub", 80, 39.00, "Delivered", LocalDate.now().minusDays(12)));
        list.add(new PurchaseOrder(6, "Prime Components Group", 60, 129.00, "Shipped", LocalDate.now().minusDays(4)));
        list.add(new PurchaseOrder(7, "Quantum Device Traders", 24, 1199.00, "Confirmed", LocalDate.now().minusDays(3)));
        list.add(new PurchaseOrder(8, "Titan Industrial Supplies", 150, 29.00, "Pending", LocalDate.now().minusDays(6)));
        list.add(new PurchaseOrder(9, "Aurora Hardware Networks", 45, 179.00, "Delivered", LocalDate.now().minusDays(14)));
        list.add(new PurchaseOrder(10, "BluePeak Distribution", 30, 249.00, "Shipped", LocalDate.now().minusDays(7)));
        list.add(new PurchaseOrder(11, "CoreWave Technologies", 18, 1499.00, "Confirmed", LocalDate.now().minusDays(2)));
        list.add(new PurchaseOrder(12, "EverGrid Supply Co.", 90, 59.00, "Pending", LocalDate.now().minusDays(5)));
        list.add(new PurchaseOrder(13, "TechSource Distribution", 110, 35.00, "Delivered", LocalDate.now().minusDays(18)));
        list.add(new PurchaseOrder(14, "Digital Hub Components", 26, 899.00, "Shipped", LocalDate.now().minusDays(10)));
        list.add(new PurchaseOrder(15, "GlobalTech Supply", 40, 329.00, "Confirmed", LocalDate.now().minusDays(8)));
        list.add(new PurchaseOrder(16, "Prime Components Group", 70, 89.00, "Pending", LocalDate.now().minusDays(11)));
        list.add(new PurchaseOrder(17, "Nexus Wholesale Partners", 22, 999.00, "Delivered", LocalDate.now().minusDays(20)));
        list.add(new PurchaseOrder(18, "Quantum Device Traders", 16, 1299.00, "Confirmed", LocalDate.now().minusDays(1)));
        return list;
    }
}

// Simple PurchaseOrder data class
class PurchaseOrder {
    private int orderId, quantity;
    private String supplier, status;
    private double unitPrice;
    private LocalDate orderDate;

    public PurchaseOrder(int orderId, String supplier, int quantity, double unitPrice, String status, LocalDate orderDate) {
        this.orderId = orderId; this.supplier = supplier; this.quantity = quantity; this.unitPrice = unitPrice; this.status = status; this.orderDate = orderDate;
    }

    public int getOrderId() { return orderId; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getOrderDate() { return orderDate; }
}
