package erp.link_tech_erp.sales;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import erp.link_tech_erp.integration.HrmSalesDeliveryPerformanceSyncService;
import erp.link_tech_erp.integration.InventorySalesOrderService;
import erp.link_tech_erp.integration.InventorySalesOrderService.QuotedOrder;
import erp.link_tech_erp.integration.SalesFinanceRevenueSyncService;

/**
 * Main GUI Application for Admin Sales and Delivery System
 * Features: Record Sales, View Sales History, Update Orders, Track Delivery
 */
public class MainApp extends JFrame {
    
    // Sales data repository (Supabase REST)
    private final SalesOrderRepository orderRepository;
    private final InventorySalesOrderService inventorySalesOrderService;
    private final SalesFinanceRevenueSyncService salesFinanceRevenueSyncService;
    private final HrmSalesDeliveryPerformanceSyncService hrmSalesDeliveryPerformanceSyncService;
    
    // Formatter for currency with commas
    private final DecimalFormat currencyFormatter = new DecimalFormat("₱#,##0.00");
    
    private JPanel dashboardPanel;
    private JLabel lblDashboardRevenue, lblDashboardDelivered, lblDashboardPending, lblDashboardTotalOrders;
    private JLabel lblDashboardDateTime;
    private JButton btnTopLogout;
    private Timer dashboardClockTimer;
    
    // Feature 1: Record Sales Components
    private JPanel recordSalesPanel;
    private JTextField txtCustomerName, txtProduct, txtQuantity, txtTotalPrice;
    private JComboBox<String> cmbPaymentStatus, cmbDeliveryStatus;
    private JButton btnAddSale, btnClearFields;
    
    // Feature 2: View Sales History Components
    private JPanel viewHistoryPanel;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JButton btnRefreshHistory, btnSearchHistory, btnDeleteFromHistory;
    private JTextField txtSearchHistory;
    private JComboBox<String> cmbSearchFilter;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Feature 3: Update Orders Components
    private JPanel updateOrdersPanel;
    private JTable updateTable;
    private DefaultTableModel updateTableModel;
    private JButton btnMarkDelivered, btnMarkPaid, btnDeleteFromUpdate;
    private JLabel lblSelectedOrderInfo;
    
    // Feature 4: Track Delivery Components
    private JPanel trackDeliveryPanel;
    private JTable trackTable;
    private DefaultTableModel trackTableModel;
    private JButton btnTrackAll, btnTrackPending, btnTrackDelivered, btnDeleteFromTrack;
    private JTextField txtTrackingSearch;
    private JTextArea txtDeliveryStats;
    private JProgressBar deliveryProgressBar;
    
    /**
     * Constructor - initializes the GUI and database connection
     */
    public MainApp() {
        // Set up the main window
        setTitle("Admin Sales and Delivery System - Complete Management Solution");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1100, 750);
        
        // Initialize Supabase REST repository
        try {
            orderRepository = new SalesOrderRepository();
            inventorySalesOrderService = new InventorySalesOrderService();
            salesFinanceRevenueSyncService = SalesFinanceRevenueSyncService.createDefault();
            hrmSalesDeliveryPerformanceSyncService = HrmSalesDeliveryPerformanceSyncService.createDefault();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                this,
                "Unable to connect to Supabase REST: " + e.getMessage(),
                "Sales Configuration Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw new IllegalStateException("Failed to initialize Sales repository", e);
        }
        
        // Initialize all feature panels
        createRecordSalesPanel();
        createViewHistoryPanel();
        createUpdateOrdersPanel();
        createTrackDeliveryPanel();
        createDashboardPanel();

        // Add fixed top app bar with always-visible logout
        createTopBar();
        
        // Show dashboard directly (without outer tab strip)
        add(dashboardPanel, BorderLayout.CENTER);
        
        // Add status bar
        createStatusBar();
        
        // Center the window on screen
        setLocationRelativeTo(null);
        
        // Load initial data for all tabs
        loadInitialData();
    }

    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout(12, 12));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dashboardPanel.setBackground(new Color(237, 232, 249));

        JPanel topSection = new JPanel(new BorderLayout(8, 8));
        topSection.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Dashboard Overview");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(new Color(49, 46, 129));

        lblDashboardDateTime = new JLabel();
        lblDashboardDateTime.setFont(new Font("Arial", Font.BOLD, 14));
        lblDashboardDateTime.setForeground(new Color(79, 70, 229));

        JPanel headerRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRightPanel.setOpaque(false);
        headerRightPanel.add(lblDashboardDateTime);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(headerRightPanel, BorderLayout.EAST);
        topSection.add(headerPanel, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsPanel.setOpaque(false);

        lblDashboardRevenue = new JLabel(currencyFormatter.format(0.0));
        lblDashboardDelivered = new JLabel("0");
        lblDashboardPending = new JLabel("0");
        lblDashboardTotalOrders = new JLabel("0");

        cardsPanel.add(createScoreCard("Total Revenue", new Color(217, 119, 6), lblDashboardRevenue));
        cardsPanel.add(createScoreCard("Delivered", new Color(13, 148, 136), lblDashboardDelivered));
        cardsPanel.add(createScoreCard("Pending", new Color(219, 39, 119), lblDashboardPending));
        cardsPanel.add(createScoreCard("Total Orders", new Color(79, 70, 229), lblDashboardTotalOrders));

        topSection.add(cardsPanel, BorderLayout.CENTER);

        JTabbedPane featureTabs = new JTabbedPane();
        featureTabs.setFont(new Font("Arial", Font.BOLD, 13));
        featureTabs.addTab("📝 Record Sales", recordSalesPanel);
        featureTabs.addTab("📊 View Sales History", viewHistoryPanel);
        featureTabs.addTab("🔄 Update Orders", updateOrdersPanel);
        featureTabs.addTab("🚚 Track Delivery", trackDeliveryPanel);

        dashboardPanel.add(topSection, BorderLayout.NORTH);
        dashboardPanel.add(featureTabs, BorderLayout.CENTER);

        startDashboardClock();
    }

    private void createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topBar.setBackground(new Color(255, 255, 255));

        JLabel appTitle = new JLabel("Admin Sales and Delivery System");
        appTitle.setFont(new Font("Arial", Font.BOLD, 14));
        appTitle.setForeground(new Color(0, 0, 0));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);

        btnTopLogout = new JButton("Logout");
        btnTopLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnTopLogout.setBackground(new Color(255, 193, 7));
        btnTopLogout.setForeground(Color.BLACK);
        btnTopLogout.setFocusPainted(false);
        btnTopLogout.setPreferredSize(new Dimension(100, 32));
        btnTopLogout.addActionListener(e -> logout());

        rightPanel.add(btnTopLogout);

        topBar.add(appTitle, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
    }

    private void startDashboardClock() {
        updateDashboardDateTime();

        if (dashboardClockTimer != null) {
            dashboardClockTimer.stop();
        }

        dashboardClockTimer = new Timer(1000, e -> updateDashboardDateTime());
        dashboardClockTimer.start();
    }

    private void updateDashboardDateTime() {
        String now = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a").format(new java.util.Date());
        lblDashboardDateTime.setText("Date & Time: " + now);
    }

    private void logout() {
        if (dashboardClockTimer != null) {
            dashboardClockTimer.stop();
        }

        dispose();
        erp.link_tech_erp.integration.GlobalLoginFrame.launch();
    }

    private JPanel createScoreCard(String title, Color accentColor, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(196, 181, 253), 1));

        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(0, 6));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(255, 255, 255));
        body.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(67, 56, 202));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 30));
        valueLabel.setForeground(accentColor);

        body.add(titleLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(valueLabel);

        card.add(topBar, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void refreshDashboardCards() {
        try {
            List<SalesOrder> orders = orderRepository.listOrders();
            int delivered = 0;
            int pending = 0;
            double revenue = 0.0;

            for (SalesOrder order : orders) {
                if ("Delivered".equals(order.getDeliveryStatus())) {
                    delivered++;
                } else if ("Pending".equals(order.getDeliveryStatus())) {
                    pending++;
                }
                revenue += order.getTotalPrice();
            }

            lblDashboardRevenue.setText(currencyFormatter.format(revenue));
            lblDashboardDelivered.setText(String.valueOf(delivered));
            lblDashboardPending.setText(String.valueOf(pending));
            lblDashboardTotalOrders.setText(String.valueOf(orders.size()));
        } catch (RuntimeException e) {
            showError("Error loading dashboard cards: " + e.getMessage());
        }
    }
    
    /**
     * Creates the Record Sales panel (Feature 1)
     */
    private void createRecordSalesPanel() {
        recordSalesPanel = new JPanel(new BorderLayout(10, 10));
        recordSalesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            "Enter Sale Details",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(76, 175, 80)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Customer Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        txtCustomerName = new JTextField(20);
        txtCustomerName.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(txtCustomerName, gbc);
        
        // Product
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        txtProduct = new JTextField(20);
        txtProduct.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(txtProduct, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        txtQuantity = new JTextField(20);
        txtQuantity.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(txtQuantity, gbc);
        
        // Total Price
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Total Price (₱):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        txtTotalPrice = new JTextField(20);
        txtTotalPrice.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTotalPrice.setEditable(false);
        inputPanel.add(txtTotalPrice, gbc);

        DocumentListener priceQuoteListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refreshQuotedTotalPrice();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refreshQuotedTotalPrice();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refreshQuotedTotalPrice();
            }
        };
        txtProduct.getDocument().addDocumentListener(priceQuoteListener);
        txtQuantity.getDocument().addDocumentListener(priceQuoteListener);
        
        // Payment Status
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Payment Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        cmbPaymentStatus = new JComboBox<>(new String[]{"Unpaid", "Paid"});
        cmbPaymentStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(cmbPaymentStatus, gbc);
        
        // Delivery Status
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Delivery Status:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        cmbDeliveryStatus = new JComboBox<>(new String[]{"Pending", "Delivered"});
        cmbDeliveryStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(cmbDeliveryStatus, gbc);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        btnAddSale = new JButton("💰 Record Sale");
        btnAddSale.setFont(new Font("Arial", Font.BOLD, 16));
        btnAddSale.setBackground(new Color(76, 175, 80));
        btnAddSale.setForeground(Color.BLACK);
        btnAddSale.setPreferredSize(new Dimension(150, 40));
        btnAddSale.addActionListener(e -> recordSale());
        
        btnClearFields = new JButton("🗑️ Clear Fields");
        btnClearFields.setFont(new Font("Arial", Font.BOLD, 16));
        btnClearFields.setBackground(new Color(255, 193, 7));
        btnClearFields.setForeground(Color.BLACK);
        btnClearFields.setPreferredSize(new Dimension(150, 40));
        btnClearFields.addActionListener(e -> clearInputFields());
        
        buttonPanel.add(btnAddSale);
        buttonPanel.add(btnClearFields);
        
        // Add panels to main panel
        recordSalesPanel.add(inputPanel, BorderLayout.CENTER);
        recordSalesPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add recent sales preview
        JPanel previewPanel = createRecentSalesPreview();
        recordSalesPanel.add(previewPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates recent sales preview panel
     */
    private JPanel createRecentSalesPreview() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Sales Preview"));
        
        String[] columns = {"Order ID", "Customer", "Product", "Total", "Payment", "Delivery"};
        DefaultTableModel previewModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable previewTable = new JTable(previewModel);
        previewTable.setFont(new Font("Arial", Font.PLAIN, 12));
        previewTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setPreferredSize(new Dimension(950, 120));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add Refresh button to Recent Sales Preview
        JButton btnRefreshPreview = new JButton("🔄 Refresh Preview");
        btnRefreshPreview.setBackground(new Color(255, 255, 255));
        btnRefreshPreview.setForeground(Color.BLACK);
        btnRefreshPreview.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefreshPreview.addActionListener(e -> loadRecentSales(previewModel));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnRefreshPreview);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Load recent 5 sales
        loadRecentSales(previewModel);
        
        return panel;
    }
    
    /**
     * Creates the View Sales History panel (Feature 2) with Delete button
     */
    private void createViewHistoryPanel() {
        viewHistoryPanel = new JPanel(new BorderLayout(10, 10));
        viewHistoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        
        searchPanel.add(new JLabel("Search by:"));
        
        cmbSearchFilter = new JComboBox<>(new String[]{
            "All Fields", "Customer Name", "Product", "Order ID"
        });
        searchPanel.add(cmbSearchFilter);
        
        txtSearchHistory = new JTextField(20);
        txtSearchHistory.setToolTipText("Enter search term...");
        searchPanel.add(txtSearchHistory);
        
        btnSearchHistory = new JButton("🔍 Search");
        btnSearchHistory.setBackground(new Color(33, 150, 243));
        btnSearchHistory.setForeground(Color.BLACK);
        btnSearchHistory.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearchHistory.addActionListener(e -> searchHistory());
        searchPanel.add(btnSearchHistory);
        
        btnRefreshHistory = new JButton("🔄 Refresh");
        btnRefreshHistory.setBackground(new Color(76, 175, 80));
        btnRefreshHistory.setForeground(Color.BLACK);
        btnRefreshHistory.setFont(new Font("Arial", Font.BOLD, 12));
        btnRefreshHistory.addActionListener(e -> {
            txtSearchHistory.setText("");
            cmbSearchFilter.setSelectedIndex(0);
            loadHistoryData();
        });
        searchPanel.add(btnRefreshHistory);
        
        // Delete button for history tab
        btnDeleteFromHistory = new JButton("🗑️ Delete Selected");
        btnDeleteFromHistory.setBackground(new Color(244, 67, 54));
        btnDeleteFromHistory.setForeground(Color.BLACK);
        btnDeleteFromHistory.setFont(new Font("Arial", Font.BOLD, 12));
        btnDeleteFromHistory.addActionListener(e -> deleteSelectedOrder(historyTable, "history"));
        searchPanel.add(btnDeleteFromHistory);
        
        // Table Panel
        String[] columns = {"Order ID", "Customer Name", "Product", "Quantity", 
                   "Total Price (₱)", "Payment Status", "Delivery Status", "Order Date"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Integer.class;
                if (columnIndex == 4) return Double.class;
                return String.class;
            }
        };
        
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 12));
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add sorter for search functionality
        sorter = new TableRowSorter<>(historyTableModel);
        historyTable.setRowSorter(sorter);
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Sales History"));
        
        // Stats Panel
        JPanel statsPanel = createHistoryStatsPanel();
        
        viewHistoryPanel.add(searchPanel, BorderLayout.NORTH);
        viewHistoryPanel.add(scrollPane, BorderLayout.CENTER);
        viewHistoryPanel.add(statsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates statistics panel for history view
     */
    private JPanel createHistoryStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        
        JLabel totalSales = new JLabel("Total Sales: 0");
        JLabel totalRevenue = new JLabel("Total Revenue: " + currencyFormatter.format(0.0));
        JLabel paidOrders = new JLabel("Paid Orders: 0");
        JLabel unpaidOrders = new JLabel("Unpaid Orders: 0");
        JLabel deliveredOrders = new JLabel("Delivered: 0");
        
        totalSales.setFont(new Font("Arial", Font.BOLD, 12));
        totalRevenue.setFont(new Font("Arial", Font.BOLD, 12));
        paidOrders.setFont(new Font("Arial", Font.BOLD, 12));
        unpaidOrders.setFont(new Font("Arial", Font.BOLD, 12));
        deliveredOrders.setFont(new Font("Arial", Font.BOLD, 12));
        
        panel.add(totalSales);
        panel.add(totalRevenue);
        panel.add(paidOrders);
        panel.add(unpaidOrders);
        panel.add(deliveredOrders);
        
        // Update stats
        updateHistoryStats(totalSales, totalRevenue, paidOrders, unpaidOrders, deliveredOrders);
        
        return panel;
    }
    
    /**
     * Creates the Update Orders panel (Feature 3) with Delete button
     */
    private void createUpdateOrdersPanel() {
        updateOrdersPanel = new JPanel(new BorderLayout(10, 10));
        updateOrdersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Order Management"));
        
        btnMarkDelivered = new JButton("✅ Mark as Delivered");
        btnMarkDelivered.setBackground(new Color(76, 175, 80));
        btnMarkDelivered.setForeground(Color.BLACK);
        btnMarkDelivered.setFont(new Font("Arial", Font.BOLD, 12));
        btnMarkDelivered.addActionListener(e -> markAsDelivered());
        
        btnMarkPaid = new JButton("💰 Mark as Paid");
        btnMarkPaid.setBackground(new Color(33, 150, 243));
        btnMarkPaid.setForeground(Color.BLACK);
        btnMarkPaid.setFont(new Font("Arial", Font.BOLD, 12));
        btnMarkPaid.addActionListener(e -> markAsPaid());
        
        // Delete button for update tab
        btnDeleteFromUpdate = new JButton("🗑️ Delete Selected");
        btnDeleteFromUpdate.setBackground(new Color(244, 67, 54));
        btnDeleteFromUpdate.setForeground(Color.BLACK);
        btnDeleteFromUpdate.setFont(new Font("Arial", Font.BOLD, 12));
        btnDeleteFromUpdate.addActionListener(e -> deleteSelectedOrder(updateTable, "update"));
        
        controlPanel.add(btnMarkDelivered);
        controlPanel.add(btnMarkPaid);
        controlPanel.add(btnDeleteFromUpdate);
        
        // Selected Order Info
        lblSelectedOrderInfo = new JLabel("No order selected");
        lblSelectedOrderInfo.setFont(new Font("Arial", Font.ITALIC, 12));
        controlPanel.add(lblSelectedOrderInfo);
        
        // Table Panel
        String[] columns = {"Order ID", "Customer", "Product", "Quantity", 
                           "Total", "Payment", "Delivery", "Order Date"};
        updateTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        updateTable = new JTable(updateTableModel);
        updateTable.setFont(new Font("Arial", Font.PLAIN, 12));
        updateTable.setRowHeight(25);
        updateTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = updateTable.getSelectedRow();
            if (selectedRow != -1) {
                updateSelectedOrderInfo(selectedRow);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(updateTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Orders List"));
        
        updateOrdersPanel.add(controlPanel, BorderLayout.NORTH);
        updateOrdersPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the Track Delivery panel (Feature 4) with Delete button
     */
    private void createTrackDeliveryPanel() {
        trackDeliveryPanel = new JPanel(new BorderLayout(10, 10));
        trackDeliveryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top Panel with Controls
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Filter Buttons
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Delivery Filters"));
        
        btnTrackAll = new JButton("📦 All Orders");
        btnTrackAll.setBackground(new Color(255, 255, 255));
        btnTrackAll.setForeground(Color.BLACK);
        btnTrackAll.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrackAll.addActionListener(e -> loadTrackData("ALL"));
        
        btnTrackPending = new JButton("⏳ Pending Delivery");
        btnTrackPending.setBackground(new Color(255, 193, 7));
        btnTrackPending.setForeground(Color.BLACK);
        btnTrackPending.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrackPending.addActionListener(e -> loadTrackData("PENDING"));
        
        btnTrackDelivered = new JButton("✅ Delivered");
        btnTrackDelivered.setBackground(new Color(76, 175, 80));
        btnTrackDelivered.setForeground(Color.BLACK);
        btnTrackDelivered.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrackDelivered.addActionListener(e -> loadTrackData("DELIVERED"));
        
        // Delete button for track tab
        btnDeleteFromTrack = new JButton("🗑️ Delete Selected");
        btnDeleteFromTrack.setBackground(new Color(244, 67, 54));
        btnDeleteFromTrack.setForeground(Color.BLACK);
        btnDeleteFromTrack.setFont(new Font("Arial", Font.BOLD, 12));
        btnDeleteFromTrack.addActionListener(e -> deleteSelectedOrder(trackTable, "track"));
        
        filterPanel.add(btnTrackAll);
        filterPanel.add(btnTrackPending);
        filterPanel.add(btnTrackDelivered);
        filterPanel.add(btnDeleteFromTrack);
        
        // Search Field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search Order:"));
        txtTrackingSearch = new JTextField(15);
        txtTrackingSearch.addActionListener(e -> searchDelivery());
        searchPanel.add(txtTrackingSearch);
        
        topPanel.add(filterPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        // Center Panel - Table and Stats
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Delivery Table
        String[] columns = {"Order ID", "Customer", "Product", "Delivery Status", 
                           "Payment Status", "Order Date", "Days Pending"};
        trackTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        trackTable = new JTable(trackTableModel);
        trackTable.setFont(new Font("Arial", Font.PLAIN, 12));
        trackTable.setRowHeight(25);
        trackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trackTable.setDefaultRenderer(Object.class, new DeliveryStatusRenderer());
        
        JScrollPane tableScroll = new JScrollPane(trackTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Delivery Tracking"));
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBorder(BorderFactory.createTitledBorder("Delivery Statistics"));
        
        txtDeliveryStats = new JTextArea();
        txtDeliveryStats.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDeliveryStats.setEditable(false);
        txtDeliveryStats.setBackground(new Color(240, 240, 240));
        
        // Progress Bar
        deliveryProgressBar = new JProgressBar(0, 100);
        deliveryProgressBar.setStringPainted(true);
        deliveryProgressBar.setFont(new Font("Arial", Font.BOLD, 12));
        
        JPanel statsInnerPanel = new JPanel(new BorderLayout());
        statsInnerPanel.add(new JScrollPane(txtDeliveryStats), BorderLayout.CENTER);
        statsInnerPanel.add(deliveryProgressBar, BorderLayout.SOUTH);
        
        centerPanel.add(tableScroll);
        centerPanel.add(statsInnerPanel);
        
        trackDeliveryPanel.add(topPanel, BorderLayout.NORTH);
        trackDeliveryPanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates status bar at bottom of window
     */
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(new Color(240, 240, 240));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JLabel statusLabel = new JLabel("✅ System Ready | Connected to Supabase REST");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        leftPanel.add(statusLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        rightPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        rightPanel.add(dateLabel);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * FEATURE 1: Record a new sale
     */
    private void recordSale() {
        // Validate input
        if (!validateSaleInput()) return;

        String paymentStatus = cmbPaymentStatus.getSelectedItem().toString();
        String deliveryStatus = cmbDeliveryStatus.getSelectedItem().toString();
        
        // Business rule validation: Cannot have Delivered if Unpaid
        if ("Delivered".equals(deliveryStatus) && "Unpaid".equals(paymentStatus)) {
            showError("Cannot mark order as Delivered when payment is Unpaid!\nPlease set payment status to Paid first.");
            return;
        }

        try {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            QuotedOrder quotedOrder = inventorySalesOrderService.quoteOrder(txtProduct.getText().trim(), quantity);
            txtTotalPrice.setText(currencyFormatter.format(quotedOrder.totalPrice()));

            orderRepository.createOrder(
                txtCustomerName.getText().trim(),
                quotedOrder.productName(),
                quotedOrder.quantity(),
                quotedOrder.totalPrice(),
                paymentStatus,
                deliveryStatus
            );

            JOptionPane.showMessageDialog(this,
                "✅ Sale recorded successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            clearInputFields();
            loadInitialData();
        } catch (RuntimeException e) {
            showError("Error recording sale: " + e.getMessage());
        }
    }
    
    /**
     * Validate sale input fields
     */
    private boolean validateSaleInput() {
        if (txtCustomerName.getText().trim().isEmpty() ||
            txtProduct.getText().trim().isEmpty() ||
            txtQuantity.getText().trim().isEmpty()) {

            showError("Please fill in all fields!");
            return false;
        }

        try {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity <= 0) {
                showError("Quantity must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for Quantity!");
            return false;
        }

        return true;
    }

    private void refreshQuotedTotalPrice() {
        String productName = txtProduct.getText().trim();
        String quantityText = txtQuantity.getText().trim();

        if (productName.isEmpty() || quantityText.isEmpty()) {
            txtTotalPrice.setText("");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            QuotedOrder quotedOrder = inventorySalesOrderService.quoteOrder(productName, quantity);
            txtTotalPrice.setText(currencyFormatter.format(quotedOrder.totalPrice()));
        } catch (RuntimeException exception) {
            txtTotalPrice.setText("");
        }
    }
    
    /**
     * FEATURE 2: Search sales history
     */
    private void searchHistory() {
        String searchTerm = txtSearchHistory.getText().trim();
        String filter = cmbSearchFilter.getSelectedItem().toString();
        
        if (searchTerm.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        
        try {
            switch (filter) {
                case "Customer Name":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm, 1));
                    break;
                case "Product":
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm, 2));
                    break;
                case "Order ID":
                    try {
                        int orderId = Integer.parseInt(searchTerm);
                        sorter.setRowFilter(RowFilter.regexFilter("^" + orderId + "$", 0));
                    } catch (NumberFormatException e) {
                        showError("Please enter a valid Order ID number!");
                    }
                    break;
                default:
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchTerm));
            }
        } catch (Exception e) {
            showError("Invalid search pattern!");
        }
    }
    
    /**
     * FEATURE 3: Mark selected order as delivered (with business rule check)
     */
    private void markAsDelivered() {
        int selectedRow = updateTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an order to update!");
            return;
        }
        
        int orderId = (int) updateTableModel.getValueAt(selectedRow, 0);
        String currentDeliveryStatus = (String) updateTableModel.getValueAt(selectedRow, 6);
        String paymentStatus = (String) updateTableModel.getValueAt(selectedRow, 5);
        
        if ("Delivered".equals(currentDeliveryStatus)) {
            showError("Order #" + orderId + " is already delivered!");
            return;
        }
        
        // Business rule: Cannot mark as delivered if unpaid
        if (!"Paid".equals(paymentStatus)) {
            showError("❌ Cannot mark Order #" + orderId + " as Delivered!\nReason: Payment status is '" + paymentStatus + "'.\nPlease mark as Paid first.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark Order #" + orderId + " as Delivered?\n\n" +
            "Customer: " + updateTableModel.getValueAt(selectedRow, 1) + "\n" +
            "Product: " + updateTableModel.getValueAt(selectedRow, 2) + "\n" +
            "Payment Status: " + paymentStatus + " ✓",
            "Confirm Delivery Update",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            updateDeliveryStatus(orderId, "Delivered");
        }
    }
    
    /**
     * FEATURE 3: Mark selected order as paid
     */
    private void markAsPaid() {
        int selectedRow = updateTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an order to update!");
            return;
        }
        
        int orderId = (int) updateTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) updateTableModel.getValueAt(selectedRow, 5);
        String deliveryStatus = (String) updateTableModel.getValueAt(selectedRow, 6);
        
        if ("Paid".equals(currentStatus)) {
            showError("Order #" + orderId + " is already paid!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark Order #" + orderId + " as Paid?\n\n" +
            "Customer: " + updateTableModel.getValueAt(selectedRow, 1) + "\n" +
            "Product: " + updateTableModel.getValueAt(selectedRow, 2) + "\n" +
            "Current Delivery Status: " + deliveryStatus,
            "Confirm Payment Update",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            updatePaymentStatus(orderId, "Paid");
        }
    }
    
    /**
     * Delete selected order (unified delete method for all tabs)
     */
    private void deleteSelectedOrder(JTable table, String source) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an order to delete!");
            return;
        }
        
        // Convert view row index to model row index (in case of sorting/filtering)
        int modelRow = table.convertRowIndexToModel(selectedRow);
        
        int orderId;
        String customerName;
        String productName;
        
        // Get data based on which table is being used
        if (table == historyTable) {
            orderId = (int) historyTableModel.getValueAt(modelRow, 0);
            customerName = (String) historyTableModel.getValueAt(modelRow, 1);
            productName = (String) historyTableModel.getValueAt(modelRow, 2);
        } else if (table == updateTable) {
            orderId = (int) updateTableModel.getValueAt(modelRow, 0);
            customerName = (String) updateTableModel.getValueAt(modelRow, 1);
            productName = (String) updateTableModel.getValueAt(modelRow, 2);
        } else {
            orderId = (int) trackTableModel.getValueAt(modelRow, 0);
            customerName = (String) trackTableModel.getValueAt(modelRow, 1);
            productName = (String) trackTableModel.getValueAt(modelRow, 2);
        }
        
        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ Are you sure you want to delete this order?\n\n" +
            "Order ID: " + orderId + "\n" +
            "Customer: " + customerName + "\n" +
            "Product: " + productName + "\n\n" +
            "This action cannot be undone!",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteOrderFromDatabase(orderId);
        }
    }
    
    /**
     * Delete order from database
     */
    private void deleteOrderFromDatabase(int orderId) {
        try {
            boolean deleted = orderRepository.deleteOrder(orderId);
            if (deleted) {
                JOptionPane.showMessageDialog(this,
                    "✅ Order #" + orderId + " deleted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadInitialData();
            }
        } catch (RuntimeException e) {
            showError("Error deleting order: " + e.getMessage());
        }
    }
    
    /**
     * Update delivery status in database with business rule check
     */
    private void updateDeliveryStatus(int orderId, String status) {
        try {
            String paymentStatus = orderRepository.getPaymentStatus(orderId);
            if (!"Paid".equals(paymentStatus)) {
                showError("Cannot mark order as Delivered!\nPayment status is '" + paymentStatus + "'.");
                return;
            }

            if (orderRepository.updateDeliveryStatus(orderId, status)) {
                // Sync delivery outcome to HRM if marked as Delivered
                if ("Delivered".equalsIgnoreCase(status)) {
                    try {
                        // For now, sync with default employee and on-time tracking
                        // In a full implementation, this would come from UI selection
                        hrmSalesDeliveryPerformanceSyncService.syncDeliveredOutcome(
                            orderId, "delivery-staff-default", true, 4.5);
                    } catch (RuntimeException syncException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Delivery was marked, but HRM sync failed:\n" + syncException.getMessage(),
                            "HRM Sync Warning",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                }

                JOptionPane.showMessageDialog(this,
                    "✅ Order #" + orderId + " marked as " + status + "!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                loadPendingOrders();
                loadTrackData("ALL");
                loadHistoryData(); // Also refresh history
                refreshDashboardCards();
            }
        } catch (RuntimeException e) {
            showError("Data error: " + e.getMessage());
        }
    }
    
    /**
     * Update payment status in database
     */
    private void updatePaymentStatus(int orderId, String status) {
        try {
            if (orderRepository.updatePaymentStatus(orderId, status)) {
                if ("Paid".equalsIgnoreCase(status)) {
                    try {
                        salesFinanceRevenueSyncService.syncPaidOrderRevenue(orderId);
                    } catch (RuntimeException syncException) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Payment was updated, but finance sync failed:\n" + syncException.getMessage(),
                            "Finance Sync Warning",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                }

                JOptionPane.showMessageDialog(this,
                    "✅ Order #" + orderId + " marked as " + status + "!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                loadPendingOrders();
                loadHistoryData(); // Refresh history
                refreshDashboardCards();
                
                // Check if this order can now be delivered (if user wants to)
                int option = JOptionPane.showConfirmDialog(this,
                    "Order is now Paid. Would you like to mark it as Delivered?",
                    "Complete Order",
                    JOptionPane.YES_NO_OPTION);
                
                if (option == JOptionPane.YES_OPTION) {
                    updateDeliveryStatus(orderId, "Delivered");
                }
            }
        } catch (RuntimeException e) {
            showError("Data error: " + e.getMessage());
        }
    }
    
    /**
     * FEATURE 4: Search delivery orders
     */
    private void searchDelivery() {
        String searchTerm = txtTrackingSearch.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadTrackData("ALL");
            return;
        }
        
        try {
            trackTableModel.setRowCount(0);
            int pending = 0, delivered = 0;
            double totalRevenue = 0;
            
            List<SalesOrder> orders = orderRepository.listOrders();
            for (SalesOrder order : orders) {
                String orderIdText = String.valueOf(order.getOrderId());
                if (!order.getCustomerName().toLowerCase().contains(searchTerm)
                        && !order.getProductName().toLowerCase().contains(searchTerm)
                        && !orderIdText.contains(searchTerm)) {
                    continue;
                }
            
                String deliveryStatus = order.getDeliveryStatus();
                String paymentStatus = order.getPaymentStatus();
                
                Object[] row = {
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getProductName(),
                    deliveryStatus,
                    paymentStatus,
                    order.getOrderDate(),
                    calculateDaysPending(order.getOrderDate(), deliveryStatus)
                };
                trackTableModel.addRow(row);
                
                if ("Delivered".equals(deliveryStatus)) {
                    delivered++;
                    totalRevenue += order.getTotalPrice();
                } else {
                    pending++;
                }
            }
            
            updateDeliveryStats(pending, delivered, totalRevenue);
        } catch (RuntimeException e) {
            showError("Search error: " + e.getMessage());
        }
    }
    
    /**
     * Load tracking data based on filter
     */
    private void loadTrackData(String filter) {
        try {
            List<SalesOrder> orders;
            if ("ALL".equals(filter)) {
                orders = orderRepository.listOrders();
            } else if ("PENDING".equals(filter)) {
                orders = orderRepository.listOrdersByDeliveryStatus("Pending");
            } else {
                orders = orderRepository.listOrdersByDeliveryStatus("Delivered");
            }

            trackTableModel.setRowCount(0);
            int pending = 0, delivered = 0;
            double totalRevenue = 0;
            
            for (SalesOrder order : orders) {
                String deliveryStatus = order.getDeliveryStatus();
                String paymentStatus = order.getPaymentStatus();
                
                Object[] row = {
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getProductName(),
                    deliveryStatus,
                    paymentStatus,
                    order.getOrderDate(),
                    calculateDaysPending(order.getOrderDate(), deliveryStatus)
                };
                trackTableModel.addRow(row);
                
                if ("Delivered".equals(deliveryStatus)) {
                    delivered++;
                    totalRevenue += order.getTotalPrice();
                } else {
                    pending++;
                }
            }
            
            updateDeliveryStats(pending, delivered, totalRevenue);

        } catch (RuntimeException e) {
            showError("Error loading tracking data: " + e.getMessage());
        }
    }
    
    /**
     * Calculate days pending for delivery
     */
    private int calculateDaysPending(Timestamp orderDate, String status) {
        if ("Delivered".equals(status)) return 0;
        
        long diff = System.currentTimeMillis() - orderDate.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
    
    /**
     * Update delivery statistics
     */
    private void updateDeliveryStats(int pending, int delivered, double totalRevenue) {
        int total = pending + delivered;
        int progress = total > 0 ? (delivered * 100 / total) : 0;
        
        StringBuilder stats = new StringBuilder();
        stats.append("📊 DELIVERY STATISTICS\n");
        stats.append("═══════════════════════\n\n");
        stats.append(String.format("Total Orders:      %d\n", total));
        stats.append(String.format("Pending Delivery:  %d\n", pending));
        stats.append(String.format("Delivered:         %d\n", delivered));
        stats.append(String.format("Delivery Rate:     %d%%\n", progress));
        stats.append("Revenue (Delivered): ").append(currencyFormatter.format(totalRevenue)).append("\n\n");
        
        if (pending > 0) {
            stats.append("⏳ PENDING ORDERS NEED ATTENTION!\n");
            stats.append("   • Check 'Update Orders' tab\n");
            stats.append("   • Ensure payments are marked as Paid first");
        } else {
            stats.append("✅ All orders delivered successfully!");
        }
        
        txtDeliveryStats.setText(stats.toString());
        deliveryProgressBar.setValue(progress);
        
        // Color code progress bar
        if (progress < 50) {
            deliveryProgressBar.setForeground(new Color(244, 67, 54)); // Red
        } else if (progress < 80) {
            deliveryProgressBar.setForeground(new Color(255, 152, 0)); // Orange
        } else {
            deliveryProgressBar.setForeground(new Color(76, 175, 80)); // Green
        }
    }
    
    /**
     * Load pending orders for update panel
     */
    private void loadPendingOrders() {
        try {
            updateTableModel.setRowCount(0);
            List<SalesOrder> orders = orderRepository.listOrdersByDeliveryStatus("Pending");
            
            for (SalesOrder order : orders) {
                Object[] row = {
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getProductName(),
                    order.getQuantity(),
                    order.getTotalPrice(),
                    order.getPaymentStatus(),
                    order.getDeliveryStatus(),
                    order.getOrderDate()
                };
                updateTableModel.addRow(row);
            }

        } catch (RuntimeException e) {
            showError("Error loading pending orders: " + e.getMessage());
        }
    }
    
    /**
     * Update selected order info label
     */
    private void updateSelectedOrderInfo(int row) {
        int orderId = (int) updateTableModel.getValueAt(row, 0);
        String customer = (String) updateTableModel.getValueAt(row, 1);
        String product = (String) updateTableModel.getValueAt(row, 2);
        String paymentStatus = (String) updateTableModel.getValueAt(row, 5);
        String deliveryStatus = (String) updateTableModel.getValueAt(row, 6);
        
        String statusColor = "Paid".equals(paymentStatus) ? "✓" : "⚠️";
        
        lblSelectedOrderInfo.setText(String.format(
            "<html>Selected: Order #%d | %s | %s | Payment: %s %s | Delivery: %s</html>",
            orderId, customer, product, paymentStatus, statusColor, deliveryStatus
        ));
    }
    
    /**
     * Load all initial data
     */
    private void loadInitialData() {
        loadHistoryData();
        loadPendingOrders();
        loadTrackData("ALL");
        refreshDashboardCards();
    }
    
    /**
     * Load sales history data
     */
    private void loadHistoryData() {
        try {
            historyTableModel.setRowCount(0);
            List<SalesOrder> orders = orderRepository.listOrders();

            for (SalesOrder order : orders) {
                Object[] row = {
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getProductName(),
                    order.getQuantity(),
                    order.getTotalPrice(),
                    order.getPaymentStatus(),
                    order.getDeliveryStatus(),
                    order.getOrderDate()
                };
                historyTableModel.addRow(row);
            }

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Load recent sales for preview
     */
    private void loadRecentSales(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            List<SalesOrder> recentOrders = orderRepository.listRecentOrders(5);
            
            for (SalesOrder order : recentOrders) {
                Object[] row = {
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getProductName(),
                    currencyFormatter.format(order.getTotalPrice()),
                    order.getPaymentStatus(),
                    order.getDeliveryStatus()
                };
                model.addRow(row);
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update history statistics
     */
    private void updateHistoryStats(JLabel totalSales, JLabel totalRevenue, 
                                   JLabel paidOrders, JLabel unpaidOrders, 
                                   JLabel deliveredOrders) {
        try {
            List<SalesOrder> orders = orderRepository.listOrders();
            int paid = 0;
            int unpaid = 0;
            int delivered = 0;
            double revenue = 0.0;

            for (SalesOrder order : orders) {
                revenue += order.getTotalPrice();
                if ("Paid".equals(order.getPaymentStatus())) {
                    paid++;
                } else if ("Unpaid".equals(order.getPaymentStatus())) {
                    unpaid++;
                }

                if ("Delivered".equals(order.getDeliveryStatus())) {
                    delivered++;
                }
            }

            totalSales.setText("Total Sales: " + orders.size());
            totalRevenue.setText("Total Revenue: " + currencyFormatter.format(revenue));
            paidOrders.setText("Paid Orders: " + paid);
            unpaidOrders.setText("Unpaid Orders: " + unpaid);
            deliveredOrders.setText("Delivered: " + delivered);

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Clear all input fields
     */
    private void clearInputFields() {
        txtCustomerName.setText("");
        txtProduct.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        cmbPaymentStatus.setSelectedIndex(0); // Unpaid by default
        cmbDeliveryStatus.setSelectedIndex(0); // Pending by default
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        erp.link_tech_erp.integration.GlobalLoginFrame.launch();
    }
}

/**
 * Custom renderer for delivery status in table
 */
class DeliveryStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (column == 3) { // Delivery Status column
            String status = value.toString();
            if ("Delivered".equals(status)) {
                c.setForeground(new Color(0, 100, 0)); // Dark Green for better visibility
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                c.setForeground(new Color(180, 0, 0)); // Dark Red for better visibility
                setFont(getFont().deriveFont(Font.BOLD));
            }
        } else {
            c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
        }
        
        return c;
    }
}
