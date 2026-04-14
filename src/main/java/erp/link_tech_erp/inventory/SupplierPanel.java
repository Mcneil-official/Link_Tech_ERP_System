package erp.link_tech_erp.inventory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class SupplierPanel extends JPanel {

    private DefaultTableModel supplierModel;
    private JTable supplierTable;
    private JTextField tfSupplierName, tfEmail, tfPhone, tfAddress, tfSearch;
    private JComboBox<String> cbCategory, cbCategoryFilter, cbRating;
    private List<Supplier> suppliers;
    private List<Supplier> filteredSuppliers;
    private final SupplierRepository supplierRepository = new SupplierRepository();

    public SupplierPanel() {
        setBackground(AppColors.PAGE_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        suppliers = loadSuppliersFromDatabase();
        filteredSuppliers = new ArrayList<>(suppliers);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
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

        JLabel title = new JLabel("Supplier Management");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JLabel sub = new JLabel("Manage supplier information, ratings, and contact details.");
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

        tfSupplierName = UIComponents.createTextField("e.g. TechSource Distribution");
        tfEmail = UIComponents.createTextField("email@supplier.com");
        tfPhone = UIComponents.createTextField("+1 (555) 123-4567");
        tfAddress = UIComponents.createTextField("123 Supply Lane, Tech City");
        cbCategory = UIComponents.createComboBox(new String[]{"Distributor", "Manufacturer", "Reseller", "Logistics", "Service Provider"});
        cbRating = UIComponents.createComboBox(new String[]{"Excellent (5/5)", "Very Good (4/5)", "Good (3/5)", "Fair (2/5)", "Poor (1/5)"});
        JTextField tfIdDisplay = UIComponents.createTextField("Auto-generated");
        tfIdDisplay.setEditable(false);
        tfIdDisplay.setForeground(AppColors.TEXT_SECONDARY);

        p.add(labeledField("Supplier Name", tfSupplierName));
        p.add(labeledField("Email", tfEmail));
        p.add(labeledField("Phone", tfPhone));
        p.add(labeledField("Address", tfAddress));
        p.add(labeledField("Category", cbCategory));
        p.add(labeledField("Rating", cbRating));
        p.add(labeledField("Supplier ID", tfIdDisplay));

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

        JButton btnAdd = UIComponents.createPrimaryButton("+ Add Supplier");
        JButton btnUpdate = UIComponents.createSecondaryButton("Update Selected");
        JButton btnDelete = UIComponents.createDangerButton("Delete Selected");
        JButton btnClear = UIComponents.createSecondaryButton("Clear");

        btnAdd.addActionListener(e -> addSupplier());
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

        JLabel title = new JLabel("Suppliers");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        title.setForeground(AppColors.TEXT_PRIMARY);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        toolbar.setOpaque(false);

        tfSearch = UIComponents.createTextField("Search suppliers...");
        tfSearch.setPreferredSize(new Dimension(220, 38));
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });

        cbCategoryFilter = UIComponents.createComboBox(new String[]{"All Categories", "Distributor", "Manufacturer", "Reseller", "Logistics", "Service Provider"});
        cbCategoryFilter.setPreferredSize(new Dimension(180, 38));
        cbCategoryFilter.addActionListener(e -> refreshTable());

        JButton btnExport = UIComponents.createSecondaryButton("Export");
        toolbar.add(tfSearch);
        toolbar.add(cbCategoryFilter);
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
        supplierModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Email", "Phone", "Address", "Rating"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        supplierTable = new JTable(supplierModel);
        supplierTable.setBackground(AppColors.CARD_BG);
        supplierTable.setGridColor(AppColors.BORDER);
        supplierTable.setShowVerticalLines(false);
        supplierTable.setShowHorizontalLines(true);
        supplierTable.setIntercellSpacing(new Dimension(0, 0));
        supplierTable.setFocusable(false);
        supplierTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        supplierTable.setSelectionBackground(new Color(232, 240, 254));
        supplierTable.setSelectionForeground(AppColors.TEXT_PRIMARY);

        JTableHeader header = supplierTable.getTableHeader();
        header.setBackground(AppColors.TABLE_HEADER_BG);
        header.setForeground(AppColors.TEXT_SECONDARY);
        header.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppColors.BORDER));
        header.setReorderingAllowed(false);

        int[] widths = {72, 210, 140, 180, 140, 260, 160};
        for (int i = 0; i < widths.length; i++) {
            supplierTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && supplierTable.getSelectedRow() >= 0) {
                loadRowIntoForm(supplierTable.getSelectedRow());
            }
        });

        refreshTable();

        JScrollPane sp = new JScrollPane(supplierTable);
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

    private void addSupplier() {
        if (tfSupplierName.getText().trim().isEmpty()) {
            showError("Supplier name is required.");
            return;
        }
        try {
            Supplier s = new Supplier(0, tfSupplierName.getText().trim(),
                (String) cbCategory.getSelectedItem(),
                tfEmail.getText().trim(), tfPhone.getText().trim(), tfAddress.getText().trim(),
                (String) cbRating.getSelectedItem());
            supplierRepository.insert(s);
            suppliers = loadSuppliersFromDatabase();
            clearForm();
            refreshTable();
        } catch (RuntimeException ex) {
            showError("Could not save supplier to Supabase: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Invalid input.");
        }
    }

    private void updateSelected() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) { showError("Select a row to update."); return; }
        try {
            if (row >= filteredSuppliers.size()) { showError("Invalid selection."); return; }
            Supplier s = filteredSuppliers.get(row);
            s.setName(tfSupplierName.getText().trim());
            s.setCategory((String) cbCategory.getSelectedItem());
            s.setEmail(tfEmail.getText().trim());
            s.setPhone(tfPhone.getText().trim());
            s.setAddress(tfAddress.getText().trim());
            s.setRating((String) cbRating.getSelectedItem());
            supplierRepository.update(s);
            suppliers = loadSuppliersFromDatabase();
            refreshTable();
            clearForm();
        } catch (RuntimeException ex) {
            showError("Could not update supplier in Supabase: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Invalid input.");
        }
    }

    private void deleteSelected() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) { showError("Select a row to delete."); return; }
        if (row >= filteredSuppliers.size()) { showError("Invalid selection."); return; }
        Supplier s = filteredSuppliers.get(row);
        int res = JOptionPane.showConfirmDialog(this,
            "Delete \"" + s.getName() + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            try {
                supplierRepository.deleteById(s.getId());
                suppliers = loadSuppliersFromDatabase();
                clearForm();
                refreshTable();
            } catch (RuntimeException ex) {
                showError("Could not delete supplier from Supabase: " + ex.getMessage());
            }
        }
    }

    private void loadRowIntoForm(int row) {
        if (row < 0 || row >= filteredSuppliers.size()) return;
        Supplier s = filteredSuppliers.get(row);
        tfSupplierName.setText(s.getName());
        cbCategory.setSelectedItem(s.getCategory());
        tfEmail.setText(s.getEmail());
        tfPhone.setText(s.getPhone());
        tfAddress.setText(s.getAddress());
        cbRating.setSelectedItem(s.getRating());
    }

    private void clearForm() {
        tfSupplierName.setText("");
        tfEmail.setText("");
        tfPhone.setText("");
        tfAddress.setText("");
        cbCategory.setSelectedIndex(0);
        cbRating.setSelectedIndex(0);
        supplierTable.clearSelection();
    }

    private void refreshTable() {
        supplierModel.setRowCount(0);
        String query = tfSearch == null ? "" : tfSearch.getText().toLowerCase();
        String category = cbCategoryFilter == null ? "All Categories" : (String) cbCategoryFilter.getSelectedItem();
        boolean allCategories = category == null || "All Categories".equalsIgnoreCase(category);

        filteredSuppliers = new ArrayList<>();
        for (Supplier s : suppliers) {
            boolean matchesCategory = allCategories || s.getCategory().equalsIgnoreCase(category);
            boolean matchesQuery = query.isEmpty()
                || s.getName().toLowerCase().contains(query)
                || s.getEmail().toLowerCase().contains(query)
                || s.getCategory().toLowerCase().contains(query);

            if (matchesCategory && matchesQuery) {
                filteredSuppliers.add(s);
                supplierModel.addRow(new Object[]{s.getId(), s.getName(), s.getCategory(), s.getEmail(), s.getPhone(), s.getAddress(), s.getRating()});
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private List<Supplier> loadSuppliersFromDatabase() {
        try {
            return supplierRepository.findAll();
        } catch (Exception exception) {
            return sampleSuppliers();
        }
    }

    private List<Supplier> sampleSuppliers() {
        List<Supplier> list = new ArrayList<>();
        list.add(new Supplier(1, "TechSource Distribution", "Distributor", "contact@techsource.com", "+1 (555) 123-4567", "123 Tech Lane, Silicon Valley, CA", "Excellent (5/5)"));
        list.add(new Supplier(2, "Digital Hub Components", "Manufacturer", "sales@digitalhub.com", "+1 (555) 234-5678", "456 Digital Ave, Tech City, NY", "Very Good (4/5)"));
        list.add(new Supplier(3, "GlobalTech Supply", "Distributor", "info@globaltech.com", "+1 (555) 345-6789", "789 Global Blvd, International Plaza, TX", "Excellent (5/5)"));
        list.add(new Supplier(4, "Nexus Wholesale Partners", "Reseller", "hello@nexuswholesale.com", "+1 (555) 412-9901", "1100 Commerce Park, Austin, TX", "Very Good (4/5)"));
        list.add(new Supplier(5, "Vertex Electronics Hub", "Service Provider", "support@vertexhub.com", "+1 (555) 678-2211", "88 Bay Street, San Diego, CA", "Very Good (4/5)"));
        list.add(new Supplier(6, "Prime Components Group", "Manufacturer", "orders@primecomponents.com", "+1 (555) 701-3304", "2200 Midtown Ave, Chicago, IL", "Excellent (5/5)"));
        list.add(new Supplier(7, "Quantum Device Traders", "Distributor", "sales@quantumtraders.com", "+1 (555) 810-4488", "54 Innovation Drive, Seattle, WA", "Excellent (5/5)"));
        list.add(new Supplier(8, "Titan Industrial Supplies", "Logistics", "contact@titanindustrial.com", "+1 (555) 902-7745", "700 Enterprise Loop, Phoenix, AZ", "Good (3/5)"));
        list.add(new Supplier(9, "Aurora Hardware Networks", "Reseller", "procurement@auroranet.com", "+1 (555) 602-9910", "390 Riverfront Plaza, Denver, CO", "Very Good (4/5)"));
        list.add(new Supplier(10, "BluePeak Distribution", "Distributor", "team@bluepeakdist.com", "+1 (555) 556-7812", "17 Logistics Way, Columbus, OH", "Good (3/5)"));
        list.add(new Supplier(11, "CoreWave Technologies", "Manufacturer", "info@corewave.tech", "+1 (555) 448-1432", "910 Central Park Blvd, Atlanta, GA", "Excellent (5/5)"));
        list.add(new Supplier(12, "EverGrid Supply Co.", "Service Provider", "service@evergrid.co", "+1 (555) 997-2403", "2650 Market Square, Miami, FL", "Very Good (4/5)"));
        return list;
    }
}

// Simple Supplier data class
class Supplier {
    private int id;
    private String name, category, email, phone, address, rating;

    public Supplier(int id, String name, String category, String email, String phone, String address, String rating) {
        this.id = id; this.name = name; this.category = category; this.email = email; this.phone = phone; this.address = address; this.rating = rating;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
}
