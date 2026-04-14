package erp.link_tech_erp.finance.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import erp.link_tech_erp.finance.model.Bill;
import erp.link_tech_erp.finance.model.Customer;
import erp.link_tech_erp.finance.model.DocumentStatus;
import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.Invoice;
import erp.link_tech_erp.finance.model.Vendor;
import erp.link_tech_erp.finance.repository.CustomerRepository;
import erp.link_tech_erp.finance.repository.VendorRepository;
import erp.link_tech_erp.finance.service.AccountsPayableService;
import erp.link_tech_erp.finance.service.AccountsReceivableService;
import erp.link_tech_erp.finance.service.BalanceSheetReport;
import erp.link_tech_erp.finance.service.CashFlowReport;
import erp.link_tech_erp.finance.service.ComplianceService;
import erp.link_tech_erp.finance.service.ComplianceService.ApprovalRequestView;
import erp.link_tech_erp.finance.service.ComplianceService.AuditLogView;
import erp.link_tech_erp.finance.service.ComplianceService.PeriodLockView;
import erp.link_tech_erp.finance.service.FinanceReport;
import erp.link_tech_erp.finance.service.FinanceService;
import erp.link_tech_erp.finance.service.FinancialStatementsService;
import erp.link_tech_erp.finance.service.ProfitAndLossReport;

public class FinanceDashboardFrame extends JFrame {
    private static final Color BACKGROUND = new Color(245, 247, 252);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color PRIMARY = new Color(25, 55, 135);
    private static final Color SUCCESS = new Color(25, 55, 135);
    private static final Color DANGER = new Color(191, 44, 44);
    private static final Color TEXT_PRIMARY = new Color(35, 43, 67);
    private static final Color TEXT_SECONDARY = new Color(100, 113, 145);
    private static final Color BORDER_SOFT = new Color(224, 231, 244);
    private static final Color SURFACE_TINT = new Color(248, 251, 255);
    private static final Color ROW_ALT = new Color(250, 252, 255);
    private static final Color SIDEBAR_BG = new Color(18, 39, 95);
    private static final Color SIDEBAR_BG_ACTIVE = new Color(34, 62, 134);
    private static final Color SIDEBAR_TEXT = Color.WHITE;
    private static final Color SIDEBAR_TEXT_MUTED = new Color(219, 227, 247);

    private final FinanceService financeService;
    private final AccountsReceivableService accountsReceivableService;
    private final AccountsPayableService accountsPayableService;
    private final FinancialStatementsService statementsService;
    private final ComplianceService complianceService;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final Runnable onSignOut;
    private final FinancialRecordTableModel tableModel;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);
    private final JLabel topHeaderTitle = new JLabel("Dashboard");
    private final JTextField searchLedgerField = new JTextField(20);

    private final JLabel totalIncomeLabel = new JLabel("0.00", SwingConstants.RIGHT);
    private final JLabel totalExpenseLabel = new JLabel("0.00", SwingConstants.RIGHT);
    private final JLabel netBalanceLabel = new JLabel("0.00", SwingConstants.RIGHT);
    private final JLabel recordsLabel = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel openArLabel = new JLabel("0.00", SwingConstants.RIGHT);
    private final JLabel openApLabel = new JLabel("0.00", SwingConstants.RIGHT);
    private final JLabel overdueArLabel = new JLabel("0", SwingConstants.RIGHT);
    private final JLabel overdueApLabel = new JLabel("0", SwingConstants.RIGHT);
    private final JTextArea expenseByCategoryArea = new JTextArea(8, 24);

    private final JTextField invoiceNoField = new JTextField(12);
        private final JComboBox<LookupOption> invoiceCustomerComboBox = new JComboBox<>();
    private final JTextField invoiceIssueDateField = new JTextField(10);
    private final JTextField invoiceDueDateField = new JTextField(10);
    private final JTextField invoiceSubtotalField = new JTextField(10);
    private final JTextField invoiceTaxField = new JTextField(10);
    private final JTextField invoiceNotesField = new JTextField(20);
        private final DefaultTableModel invoiceTableModel = new DefaultTableModel(
            new String[] { "ID", "Invoice No", "Customer", "Status", "Due Date", "Balance" }, 0);
        private final JTable invoiceTable = new JTable(invoiceTableModel);

    private final JTextField billNoField = new JTextField(12);
        private final JComboBox<LookupOption> billVendorComboBox = new JComboBox<>();
    private final JTextField billIssueDateField = new JTextField(10);
    private final JTextField billDueDateField = new JTextField(10);
    private final JTextField billSubtotalField = new JTextField(10);
    private final JTextField billTaxField = new JTextField(10);
    private final JTextField billNotesField = new JTextField(20);
        private final DefaultTableModel billTableModel = new DefaultTableModel(
            new String[] { "ID", "Bill No", "Vendor", "Status", "Due Date", "Balance" }, 0);
        private final JTable billTable = new JTable(billTableModel);

    private final JComboBox<String> paymentTargetTypeComboBox = new JComboBox<>(new String[] { "Invoice", "Bill" });
    private final JTextField paymentTargetIdField = new JTextField(16);
    private final JTextField paymentAmountField = new JTextField(10);
    private final JTextField paymentMethodField = new JTextField(12);
    private final JTextField paymentReferenceField = new JTextField(12);
    private final JTextArea paymentHelpArea = new JTextArea(10, 40);

    private final JTextField statementsFromDateField = new JTextField(10);
    private final JTextField statementsToDateField = new JTextField(10);
    private final JTextArea statementsOutputArea = new JTextArea(20, 40);

        private final DefaultTableModel approvalTableModel = new DefaultTableModel(
            new String[] { "ID", "Type", "Source", "Requested By", "Status", "Requested At", "Remarks" }, 0);
        private final JTable approvalTable = new JTable(approvalTableModel);
        private final JTextField approvalRemarksField = new JTextField(24);

        private final JTextField lockStartDateField = new JTextField(10);
        private final JTextField lockEndDateField = new JTextField(10);
        private final JComboBox<String> lockStatusComboBox = new JComboBox<>(new String[] { "Lock", "Unlock" });
        private final JTextField lockRemarksField = new JTextField(20);
        private final DefaultTableModel periodLockTableModel = new DefaultTableModel(
            new String[] { "ID", "Start", "End", "Locked", "By", "At" }, 0);
        private final JTable periodLockTable = new JTable(periodLockTableModel);

        private final DefaultTableModel auditTableModel = new DefaultTableModel(
            new String[] { "At", "Entity", "Entity ID", "Action", "By", "Reason" }, 0);
        private final JTable auditTable = new JTable(auditTableModel);
        private final JLabel complianceRoleLabel = new JLabel("Role: -");

    private final JTable recordTable = new JTable();
    private final JComboBox<String> typeFilterComboBox = new JComboBox<>(new String[] { "All", "Income", "Expense" });
    private final JComboBox<String> categoryFilterComboBox = new JComboBox<>(new String[] { "All Categories" });
    private final java.util.List<JButton> menuButtons = new java.util.ArrayList<>();
    private JButton activeMenuButton;
    private TableRowSorter<FinancialRecordTableModel> tableSorter;
    private String selectedRecordId;

    public FinanceDashboardFrame(FinanceService financeService,
                                 AccountsReceivableService accountsReceivableService,
                                 AccountsPayableService accountsPayableService,
                                 FinancialStatementsService statementsService,
                                 ComplianceService complianceService,
                                 CustomerRepository customerRepository,
                                 VendorRepository vendorRepository,
                                 Runnable onSignOut) {
        this.financeService = financeService;
        this.accountsReceivableService = accountsReceivableService;
        this.accountsPayableService = accountsPayableService;
        this.statementsService = statementsService;
        this.complianceService = complianceService;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.onSignOut = onSignOut == null ? () -> System.exit(0) : onSignOut;
        this.tableModel = new FinancialRecordTableModel();

        setTitle("Link Tech Finance Workspace");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(1240, 760));
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);

        initializeLayout();
        loadRecords();
        refreshReport();
        refreshArApViews();
        initializeStatementsDefaults();
        refreshStatements();
        initializeComplianceDefaults();
        refreshComplianceViews();
    }

    public FinanceDashboardFrame(FinanceService financeService) {
        this(financeService, null, null, null, null, null, null, null);
    }

    private void initializeLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(BACKGROUND);

        rootPanel.add(buildSidebar(), BorderLayout.WEST);

        // Build Cards
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(buildDashboardCard(), "Dashboard");
        mainContentPanel.add(buildLedgerCard(), "Ledger");
        mainContentPanel.add(buildReportsCard(), "Reports"); // other tabs

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(24, 32, 24, 32));
        contentWrapper.add(buildTopHeader(), BorderLayout.NORTH); // For "Fiscal Overview / Dashboard" title and search bar
        contentWrapper.add(mainContentPanel, BorderLayout.CENTER);

        rootPanel.add(contentWrapper, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 14));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(14, 10, 14, 10));
        sidebar.setPreferredSize(new Dimension(198, 0));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);
        JLabel icon = new JLabel("\uD83C\uDFDB");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        icon.setForeground(SIDEBAR_TEXT);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel title = new JLabel("Link Tech");
        title.setFont(new Font("Inter", Font.BOLD, 16));
        title.setForeground(SIDEBAR_TEXT);
        JLabel sub = new JLabel("FINANCE PLATFORM");
        sub.setFont(new Font("Inter", Font.BOLD, 9));
        sub.setForeground(SIDEBAR_TEXT_MUTED);
        textPanel.add(title);
        textPanel.add(sub);
        brandPanel.add(icon);
        brandPanel.add(textPanel);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        JButton dashboardButton = createMenuButton("Dashboard", "Dashboard", "\u25A6");
        JButton ledgerButton = createMenuButton("Ledger", "Ledger", "\u2630");
        JButton reportsButton = createMenuButton("Reports", "Reports", "\u25A4");
        menuPanel.add(dashboardButton);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(ledgerButton);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(reportsButton);
        menuPanel.add(Box.createVerticalGlue());
        setActiveMenuButton(dashboardButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);

        JPanel supportPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        supportPanel.setOpaque(false);
        supportPanel.add(createSidebarQuietButton("?  Support"));
        supportPanel.add(createSidebarQuietButton("x  Sign Out"));

        bottomPanel.add(supportPanel, BorderLayout.NORTH);

        sidebar.add(brandPanel, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createMenuButton(String targetCard, String label, String icon) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(8, 10, 8, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, targetCard);
            topHeaderTitle.setText(label);
            setActiveMenuButton(btn);
        });
        menuButtons.add(btn);
        return btn;
    }

    private void setActiveMenuButton(JButton selectedButton) {
        activeMenuButton = selectedButton;
        for (JButton button : menuButtons) {
            boolean selected = button == activeMenuButton;
            button.setBackground(selected ? SIDEBAR_BG_ACTIVE : SIDEBAR_BG);
            button.setForeground(SIDEBAR_TEXT);
            button.setBorder(new CompoundBorder(
                    new LineBorder(selected ? new Color(95, 122, 192) : SIDEBAR_BG, 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
        }
    }

    private JButton createSidebarQuietButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Inter", Font.PLAIN, 13));
        btn.setForeground(SIDEBAR_TEXT_MUTED);
        btn.setBackground(SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        if (label.contains("Sign Out")) {
            btn.addActionListener(e -> {
                dispose();
                onSignOut.run();
            });
        }
        return btn;
    }

    private JButton createQuietButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Inter", Font.PLAIN, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SURFACE_TINT);
        btn.setFocusPainted(false);
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_SOFT, 1, true), new EmptyBorder(6, 10, 6, 10)));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        if (label.contains("Sign Out")) {
            btn.addActionListener(e -> {
                dispose();
                onSignOut.run();
            });
        }
        return btn;
    }

    private Border createCardBorder() {
        return new CompoundBorder(
                new CompoundBorder(new LineBorder(new Color(210, 220, 238), 1, true), new LineBorder(Color.WHITE, 1, true)),
                new EmptyBorder(16, 16, 16, 16));
    }

    private JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.putClientProperty("JComponent.roundRect", true);
        return button;
    }

    private JPanel buildTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel overTitle = new JLabel("Fiscal Overview");
        overTitle.setFont(new Font("Inter", Font.BOLD, 12));
        overTitle.setForeground(TEXT_SECONDARY);
        topHeaderTitle.setFont(new Font("Inter", Font.BOLD, 28));
        topHeaderTitle.setForeground(TEXT_PRIMARY);
        titlePanel.add(overTitle);
        titlePanel.add(topHeaderTitle);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JPanel buildDashboardCard() {
        JPanel dash = new JPanel(new BorderLayout(24, 24));
        dash.setOpaque(false);
        
        // Top KPI Cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(buildKpiCard("TOTAL ASSETS", totalIncomeLabel, "+12.4% vs last quarter"));
        kpiRow.add(buildKpiCard("REVENUE (YTD)", totalExpenseLabel, "+5.2% vs target"));
        kpiRow.add(buildKpiCard("NET INCOME", netBalanceLabel, "-2.1% operational drag"));
        kpiRow.add(buildKpiCard("RECORD COUNT", recordsLabel, ""));
        dash.add(kpiRow, BorderLayout.NORTH);

        JPanel mainRow = new JPanel(new BorderLayout(24, 24));
        mainRow.setOpaque(false);

        // Expense tracking placeholder
        JPanel chartPanel = new JPanel(new BorderLayout(0, 16));
        chartPanel.setBackground(CARD_BACKGROUND);
        chartPanel.setBorder(createCardBorder());
        JLabel chartTitle = new JLabel("Expense Tracking");
        chartTitle.setFont(new Font("Inter", Font.BOLD, 16));
        chartPanel.add(chartTitle, BorderLayout.NORTH);
        
        expenseByCategoryArea.setEditable(false);
        expenseByCategoryArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        expenseByCategoryArea.setBorder(BorderFactory.createEmptyBorder());
        chartPanel.add(new JScrollPane(expenseByCategoryArea), BorderLayout.CENTER);
        mainRow.add(chartPanel, BorderLayout.CENTER);

        // Recent Activity placeholder
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBackground(CARD_BACKGROUND);
        recentPanel.setBorder(createCardBorder());
        recentPanel.setPreferredSize(new Dimension(300, 0));
        JLabel recentTitle = new JLabel("Recent Activity");
        recentTitle.setFont(new Font("Inter", Font.BOLD, 16));
        recentPanel.add(recentTitle, BorderLayout.NORTH);
        mainRow.add(recentPanel, BorderLayout.EAST);

        dash.add(mainRow, BorderLayout.CENTER);
        return dash;
    }

    private JPanel buildLedgerCard() {
        JPanel ledger = new JPanel(new BorderLayout(16, 16));
        ledger.setOpaque(false);

        // Top KPI Cards for Ledger
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(buildKpiCard("TOTAL REVENUE", new JLabel("$142,500.00"), "\u2191 12.5% increase"));
        kpiRow.add(buildKpiCard("PENDING APPROVALS", new JLabel("24"), "Awaiting internal review"));
        kpiRow.add(buildKpiCard("Q3 FORECAST", new JLabel("$89,204.12"), ""));
        kpiRow.add(buildKpiCard("ACTIVE ENTITIES", new JLabel("+8"), "Global contributors"));
        ledger.add(kpiRow, BorderLayout.NORTH);

        JPanel tableArea = new JPanel(new BorderLayout(12, 12));
        tableArea.setOpaque(false);

        JPanel actionsArea = new JPanel(new BorderLayout());
        actionsArea.setOpaque(false);
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        leftActions.add(new JLabel("Type:"));
        typeFilterComboBox.setSelectedIndex(0);
        leftActions.add(typeFilterComboBox);

        leftActions.add(new JLabel("Category:"));
        categoryFilterComboBox.setSelectedIndex(0);
        leftActions.add(categoryFilterComboBox);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        JButton refreshBtn = createButton("Refresh", new Color(240, 242, 246), TEXT_PRIMARY);
        refreshBtn.addActionListener(e -> {
            loadRecords();
            refreshReport();
        });
        rightActions.add(refreshBtn);

        JButton exportBtn = createButton("Export", new Color(240, 242, 246), TEXT_PRIMARY);
        exportBtn.addActionListener(e -> handleExportLedger());
        rightActions.add(exportBtn);

        JButton addBtn = createButton("+ New Record", PRIMARY, Color.WHITE);
        addBtn.addActionListener(e -> handleCreate());
        rightActions.add(addBtn);

        actionsArea.add(leftActions, BorderLayout.WEST);
        actionsArea.add(rightActions, BorderLayout.EAST);
        tableArea.add(actionsArea, BorderLayout.NORTH);

        recordTable.setModel(tableModel);
        recordTable.setFont(new Font("Inter", Font.PLAIN, 13));
        recordTable.setRowHeight(40);
        applyModernTableStyle(recordTable, 40);
        tableSorter = new TableRowSorter<>(tableModel);
        tableSorter.setSortable(5, false);
        tableSorter.setSortable(6, false);
        recordTable.setRowSorter(tableSorter);
        typeFilterComboBox.addActionListener(e -> {
            updateCategoryFilterOptions();
            applyLedgerFilters();
        });
        categoryFilterComboBox.addActionListener(e -> applyLedgerFilters());
        recordTable.setSelectionBackground(new Color(240, 245, 250));
        recordTable.setSelectionForeground(TEXT_PRIMARY);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) handleSelectionChange();
        });
        configureLedgerActionColumns();

        recordTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int viewRow = recordTable.rowAtPoint(event.getPoint());
                int viewColumn = recordTable.columnAtPoint(event.getPoint());
                if (viewRow < 0 || viewColumn < 0) {
                    return;
                }

                int modelRow = recordTable.convertRowIndexToModel(viewRow);
                int modelColumn = recordTable.convertColumnIndexToModel(viewColumn);
                if (modelColumn == 5) {
                    handleUpdateForRow(modelRow);
                } else if (modelColumn == 6) {
                    handleDeleteForRow(modelRow);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(recordTable);
        tableScroll.setBorder(createCardBorder());
        tableScroll.getViewport().setBackground(CARD_BACKGROUND);
        tableArea.add(tableScroll, BorderLayout.CENTER);
        ledger.add(tableArea, BorderLayout.CENTER);

        return ledger;
    }

    private JPanel buildReportsCard() {
        JPanel reportsCard = new JPanel(new BorderLayout(16, 16));
        reportsCard.setOpaque(false);

        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(buildKpiCard("OPEN RECEIVABLE", openArLabel, "Total unpaid invoices"));
        kpiRow.add(buildKpiCard("OPEN PAYABLE", openApLabel, "Total outstanding bills"));
        kpiRow.add(buildKpiCard("OVERDUE INVOICES", overdueArLabel, "Past due receivables"));
        kpiRow.add(buildKpiCard("OVERDUE BILLS", overdueApLabel, "Past due payables"));
        reportsCard.add(kpiRow, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new BorderLayout(12, 12));
        contentArea.setOpaque(false);

        JPanel actionsArea = new JPanel(new BorderLayout());
        actionsArea.setOpaque(false);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        JButton refreshBtn = createQuietButton("Refresh");
        refreshBtn.addActionListener(e -> {
            refreshArApViews();
            refreshStatements();
            refreshComplianceViews();
            refreshArApKpis();
        });
        rightActions.add(refreshBtn);

        actionsArea.add(rightActions, BorderLayout.EAST);
        contentArea.add(actionsArea, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Inter", Font.PLAIN, 13));
        tabs.addTab("Invoices", buildInvoicesTab());
        tabs.addTab("Bills", buildBillsTab());
        tabs.addTab("Payments", buildPaymentsTab());
        tabs.addTab("Statements", buildStatementsTab());
        tabs.addTab("Compliance", buildComplianceTab());

        JPanel tabsContainer = new JPanel(new BorderLayout());
        tabsContainer.setBackground(CARD_BACKGROUND);
        tabsContainer.setBorder(createCardBorder());
        tabsContainer.add(tabs, BorderLayout.CENTER);

        contentArea.add(tabsContainer, BorderLayout.CENTER);
        reportsCard.add(contentArea, BorderLayout.CENTER);
        return reportsCard;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE_TINT);
        card.setBorder(createCardBorder());

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Inter", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 11));
            subtitleLabel.setForeground(TEXT_SECONDARY);
            subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(6));
            card.add(subtitleLabel);
        }

        return card;
    }

    private JPanel buildSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(SURFACE_TINT);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_SOFT, 1, true), new EmptyBorder(8, 10, 8, 10)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void configureLedgerActionColumns() {
        DefaultTableCellRenderer actionRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(value == null ? "" : value.toString());
                if (isSelected) {
                    setForeground(TEXT_PRIMARY);
                } else if ("Delete".equals(value)) {
                    setForeground(DANGER);
                } else {
                    setForeground(PRIMARY);
                }
                return this;
            }
        };

        recordTable.getColumnModel().getColumn(5).setPreferredWidth(92);
        recordTable.getColumnModel().getColumn(5).setMaxWidth(110);
        recordTable.getColumnModel().getColumn(6).setPreferredWidth(92);
        recordTable.getColumnModel().getColumn(6).setMaxWidth(110);
        recordTable.getColumnModel().getColumn(5).setCellRenderer(actionRenderer);
        recordTable.getColumnModel().getColumn(6).setCellRenderer(actionRenderer);
    }

    private void applyModernTableStyle(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 253));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer striped = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BACKGROUND : ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(striped);
        }
    }

    private void handleCreate() {
        RecordDialog dialog = new RecordDialog(this, "Create New Record", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            FinancialRecord record = new FinancialRecord();
            dialog.applyToRecord(record);
            try {
                financeService.createRecord(record);
                loadRecords();
                refreshReport();
                refreshComplianceViews();
            } catch (IllegalStateException exception) {
                showWarning(exception.getMessage());
            }
        }
    }

    private void handleUpdate() {
        int selectedViewRow = recordTable.getSelectedRow();
        if (selectedViewRow < 0) {
            showWarning("Select a record first before updating.");
            return;
        }
        handleUpdateForRow(recordTable.convertRowIndexToModel(selectedViewRow));
    }

    private void handleUpdateForRow(int modelRow) {
        FinancialRecord existingRecord = tableModel.getRecordAt(modelRow);
        selectedRecordId = existingRecord.getId();

        RecordDialog dialog = new RecordDialog(this, "Update Record", existingRecord);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            FinancialRecord updatedRecord = new FinancialRecord();
            dialog.applyToRecord(updatedRecord);
            updatedRecord.setId(selectedRecordId);
            try {
                boolean updated = financeService.updateRecord(updatedRecord);
                if (!updated) {
                    showWarning("The selected record no longer exists.");
                    return;
                }

                loadRecords();
                refreshReport();
                refreshComplianceViews();
                recordTable.clearSelection();
                selectedRecordId = null;
            } catch (IllegalStateException exception) {
                showWarning(exception.getMessage());
            }
        }
    }

    private void handleDelete() {
        int selectedViewRow = recordTable.getSelectedRow();
        if (selectedViewRow < 0) {
            showWarning("Select a record first before deleting.");
            return;
        }
        handleDeleteForRow(recordTable.convertRowIndexToModel(selectedViewRow));
    }

    private void handleDeleteForRow(int modelRow) {
        FinancialRecord record = tableModel.getRecordAt(modelRow);
        selectedRecordId = record.getId();

        try {
            boolean deleted = financeService.deleteRecordById(selectedRecordId);
            if (!deleted) {
                showWarning("The selected record no longer exists.");
                return;
            }

            loadRecords();
            refreshReport();
            refreshComplianceViews();
            clearForm();
        } catch (IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }



    private void handleSelectionChange() {
        int selectedViewRow = recordTable.getSelectedRow();
        if (selectedViewRow < 0) {
            selectedRecordId = null;
            return;
        }
        int modelRow = recordTable.convertRowIndexToModel(selectedViewRow);
        FinancialRecord record = tableModel.getRecordAt(modelRow);
        selectedRecordId = record.getId();
    }

    private void loadRecords() {
        try {
            tableModel.setRecords(financeService.getAllRecords());
            updateCategoryFilterOptions();
            applyLedgerFilters();
        } catch (IllegalStateException exception) {
            tableModel.setRecords(java.util.Collections.emptyList());
            updateCategoryFilterOptions();
            applyLedgerFilters();
            showWarning("Unable to load records. Check Supabase DB credentials and connection.\n" + exception.getMessage());
        }
    }

    private void updateCategoryFilterOptions() {
        String selectedType = String.valueOf(typeFilterComboBox.getSelectedItem());
        String previousCategory = String.valueOf(categoryFilterComboBox.getSelectedItem());

        Set<String> categories = new LinkedHashSet<>();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String rowType = String.valueOf(tableModel.getValueAt(row, 1));
            String rowCategory = String.valueOf(tableModel.getValueAt(row, 2));

            boolean typeMatches = "All".equalsIgnoreCase(selectedType)
                    || rowType.equalsIgnoreCase(selectedType);

            if (typeMatches && rowCategory != null && !rowCategory.isBlank()) {
                categories.add(rowCategory);
            }
        }

        categoryFilterComboBox.removeAllItems();
        categoryFilterComboBox.addItem("All Categories");
        for (String category : categories) {
            categoryFilterComboBox.addItem(category);
        }

        if (previousCategory != null) {
            for (int i = 0; i < categoryFilterComboBox.getItemCount(); i++) {
                if (previousCategory.equalsIgnoreCase(String.valueOf(categoryFilterComboBox.getItemAt(i)))) {
                    categoryFilterComboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        categoryFilterComboBox.setSelectedIndex(0);
    }

    private void applyLedgerFilters() {
        if (tableSorter == null) {
            return;
        }

        String selectedType = String.valueOf(typeFilterComboBox.getSelectedItem());
        String selectedCategory = String.valueOf(categoryFilterComboBox.getSelectedItem());

        boolean filterByType = selectedType != null && !"All".equalsIgnoreCase(selectedType);
        boolean filterByCategory = selectedCategory != null && !"All Categories".equalsIgnoreCase(selectedCategory);

        if (!filterByType && !filterByCategory) {
            tableSorter.setRowFilter(null);
            return;
        }

        tableSorter.setRowFilter(new RowFilter<FinancialRecordTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends FinancialRecordTableModel, ? extends Integer> entry) {
                String rowType = String.valueOf(entry.getValue(1));
                String rowCategory = String.valueOf(entry.getValue(2));

                boolean typeMatches = !filterByType || rowType.equalsIgnoreCase(selectedType);
                boolean categoryMatches = !filterByCategory || rowCategory.equalsIgnoreCase(selectedCategory);

                return typeMatches && categoryMatches;
            }
        });
    }

    private void handleExportLedger() {
        if (recordTable.getRowCount() == 0) {
            showWarning("No ledger rows available to export.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Ledger");
        fileChooser.setSelectedFile(new File("ledger_export.xlsx"));
        FileNameExtensionFilter xlsxFilter = new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.addChoosableFileFilter(xlsxFilter);
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(xlsxFilter);

        int choice = fileChooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String extension;
        if (fileChooser.getFileFilter() == csvFilter) {
            extension = ".csv";
        } else {
            extension = ".xlsx";
        }

        if (!selectedFile.getName().toLowerCase().endsWith(extension)) {
            selectedFile = new File(selectedFile.getAbsolutePath() + extension);
        }

        try {
            if (".csv".equals(extension)) {
                exportLedgerAsCsv(selectedFile);
            } else {
                exportLedgerAsXlsx(selectedFile);
            }

            JOptionPane.showMessageDialog(this,
                    "Ledger exported successfully to:\n" + selectedFile.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            showWarning("Unable to export ledger.\n" + exception.getMessage());
        }
    }

    private void exportLedgerAsCsv(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Date,Type,Category,Description,Amount");
            writer.newLine();

            for (int viewRow = 0; viewRow < recordTable.getRowCount(); viewRow++) {
                int modelRow = recordTable.convertRowIndexToModel(viewRow);

                String date = String.valueOf(tableModel.getValueAt(modelRow, 0));
                String type = String.valueOf(tableModel.getValueAt(modelRow, 1));
                String category = String.valueOf(tableModel.getValueAt(modelRow, 2));
                String description = String.valueOf(tableModel.getValueAt(modelRow, 3));
                String amount = String.valueOf(tableModel.getValueAt(modelRow, 4));

                writer.write(csvEscape(date) + ","
                        + csvEscape(type) + ","
                        + csvEscape(category) + ","
                        + csvEscape(description) + ","
                        + csvEscape(amount));
                writer.newLine();
            }
        }
    }

    private void exportLedgerAsXlsx(File file) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ledger");

            String[] headers = { "Date", "Type", "Category", "Description", "Amount" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            for (int viewRow = 0; viewRow < recordTable.getRowCount(); viewRow++) {
                int modelRow = recordTable.convertRowIndexToModel(viewRow);
                Row row = sheet.createRow(viewRow + 1);

                row.createCell(0).setCellValue(String.valueOf(tableModel.getValueAt(modelRow, 0)));
                row.createCell(1).setCellValue(String.valueOf(tableModel.getValueAt(modelRow, 1)));
                row.createCell(2).setCellValue(String.valueOf(tableModel.getValueAt(modelRow, 2)));
                row.createCell(3).setCellValue(String.valueOf(tableModel.getValueAt(modelRow, 3)));
                row.createCell(4).setCellValue(String.valueOf(tableModel.getValueAt(modelRow, 4)));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private void refreshReport() {
        try {
            FinanceReport report = financeService.generateReport();
            totalIncomeLabel.setText(String.format("%.2f", report.getTotalIncome()));
            totalExpenseLabel.setText(String.format("%.2f", report.getTotalExpense()));
            netBalanceLabel.setText(String.format("%.2f", report.getNetBalance()));
            recordsLabel.setText(String.valueOf(report.getRecordCount()));

            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Double> entry : report.getExpenseByCategory().entrySet()) {
                builder.append(entry.getKey())
                        .append(": ")
                        .append(String.format("%.2f", entry.getValue()))
                        .append(System.lineSeparator());
            }
            if (builder.isEmpty()) {
                builder.append("No expense entries yet.");
            }
            expenseByCategoryArea.setText(builder.toString());
            refreshArApKpis();
        } catch (IllegalStateException exception) {
            totalIncomeLabel.setText("0.00");
            totalExpenseLabel.setText("0.00");
            netBalanceLabel.setText("0.00");
            recordsLabel.setText("0");
            expenseByCategoryArea.setText("Unable to load report data.");
            openArLabel.setText("0.00");
            openApLabel.setText("0.00");
            overdueArLabel.setText("0");
            overdueApLabel.setText("0");
        }
    }

    private JPanel buildInvoicesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createCardBorder());
        panel.setBackground(CARD_BACKGROUND);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton createInvoiceButton = createButton("+ New Invoice", PRIMARY, Color.WHITE);
        JButton refreshButton = createButton("Refresh", new Color(233, 237, 247), TEXT_PRIMARY);
        createInvoiceButton.addActionListener(event -> openInvoiceFormDialog());
        refreshButton.addActionListener(event -> refreshArApViews());
        actions.add(createInvoiceButton);
        actions.add(refreshButton);

        invoiceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applyModernTableStyle(invoiceTable, 28);
        invoiceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoiceTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && invoiceTable.getSelectedRow() >= 0) {
                int selectedRow = invoiceTable.getSelectedRow();
                Object idValue = invoiceTableModel.getValueAt(selectedRow, 0);
                if (idValue != null) {
                    paymentTargetTypeComboBox.setSelectedItem("Invoice");
                    paymentTargetIdField.setText(idValue.toString());
                }
            }
        });

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBillsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createCardBorder());
        panel.setBackground(CARD_BACKGROUND);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton createBillButton = createButton("+ New Bill", PRIMARY, Color.WHITE);
        JButton refreshButton = createButton("Refresh", new Color(233, 237, 247), TEXT_PRIMARY);
        createBillButton.addActionListener(event -> openBillFormDialog());
        refreshButton.addActionListener(event -> refreshArApViews());
        actions.add(createBillButton);
        actions.add(refreshButton);

        billTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applyModernTableStyle(billTable, 28);
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && billTable.getSelectedRow() >= 0) {
                int selectedRow = billTable.getSelectedRow();
                Object idValue = billTableModel.getValueAt(selectedRow, 0);
                if (idValue != null) {
                    paymentTargetTypeComboBox.setSelectedItem("Bill");
                    paymentTargetIdField.setText(idValue.toString());
                }
            }
        });

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(billTable), BorderLayout.CENTER);
        return panel;
    }

    private void openInvoiceFormDialog() {
        if (accountsReceivableService == null) {
            showWarning("Accounts Receivable service is not initialized.");
            return;
        }

        refreshLookupDropdowns();

        JTextField invoiceNo = new JTextField(12);
        JComboBox<LookupOption> customerCombo = new JComboBox<>();
        for (int i = 0; i < invoiceCustomerComboBox.getItemCount(); i++) {
            customerCombo.addItem(invoiceCustomerComboBox.getItemAt(i));
        }
        JTextField issueDate = new JTextField(LocalDate.now().toString(), 10);
        JTextField dueDate = new JTextField(LocalDate.now().plusDays(30).toString(), 10);
        JTextField subtotal = new JTextField("0", 10);
        JTextField tax = new JTextField("0", 10);
        JTextField notes = new JTextField(20);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Invoice No:"), gbc);
        gbc.gridx = 1; form.add(invoiceNo, gbc);
        gbc.gridx = 2; form.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 3; form.add(customerCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Issue Date:"), gbc);
        gbc.gridx = 1; form.add(issueDate, gbc);
        gbc.gridx = 2; form.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 3; form.add(dueDate, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1; form.add(subtotal, gbc);
        gbc.gridx = 2; form.add(new JLabel("Tax:"), gbc);
        gbc.gridx = 3; form.add(tax, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(notes, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Create Invoice", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LookupOption selectedCustomer = (LookupOption) customerCombo.getSelectedItem();
            if (selectedCustomer == null || selectedCustomer.id == null || selectedCustomer.id.isBlank()) {
                showWarning("Select a customer before creating an invoice.");
                return;
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceNo.getText().trim());
            invoice.setCustomerId(selectedCustomer.id);
            invoice.setIssueDate(parseDateOrDefault(issueDate.getText().trim(), LocalDate.now()));
            invoice.setDueDate(parseDateOrDefault(dueDate.getText().trim(), invoice.getIssueDate().plusDays(30)));
            invoice.setCurrency("PHP");
            invoice.setSubtotal(parseDoubleOrDefault(subtotal.getText().trim(), 0.0));
            invoice.setTaxAmount(parseDoubleOrDefault(tax.getText().trim(), 0.0));
            invoice.setPaidAmount(0.0);
            invoice.setNotes(notes.getText().trim());
            accountsReceivableService.issueInvoice(invoice);
            refreshArApViews();
            refreshComplianceViews();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void openBillFormDialog() {
        if (accountsPayableService == null) {
            showWarning("Accounts Payable service is not initialized.");
            return;
        }

        refreshLookupDropdowns();

        JTextField billNo = new JTextField(12);
        JComboBox<LookupOption> vendorCombo = new JComboBox<>();
        for (int i = 0; i < billVendorComboBox.getItemCount(); i++) {
            vendorCombo.addItem(billVendorComboBox.getItemAt(i));
        }
        JTextField issueDate = new JTextField(LocalDate.now().toString(), 10);
        JTextField dueDate = new JTextField(LocalDate.now().plusDays(30).toString(), 10);
        JTextField subtotal = new JTextField("0", 10);
        JTextField tax = new JTextField("0", 10);
        JTextField notes = new JTextField(20);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Bill No:"), gbc);
        gbc.gridx = 1; form.add(billNo, gbc);
        gbc.gridx = 2; form.add(new JLabel("Vendor:"), gbc);
        gbc.gridx = 3; form.add(vendorCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Issue Date:"), gbc);
        gbc.gridx = 1; form.add(issueDate, gbc);
        gbc.gridx = 2; form.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 3; form.add(dueDate, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1; form.add(subtotal, gbc);
        gbc.gridx = 2; form.add(new JLabel("Tax:"), gbc);
        gbc.gridx = 3; form.add(tax, gbc);

        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(notes, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Create Bill", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LookupOption selectedVendor = (LookupOption) vendorCombo.getSelectedItem();
            if (selectedVendor == null || selectedVendor.id == null || selectedVendor.id.isBlank()) {
                showWarning("Select a vendor before creating a bill.");
                return;
            }

            Bill bill = new Bill();
            bill.setBillNo(billNo.getText().trim());
            bill.setVendorId(selectedVendor.id);
            bill.setIssueDate(parseDateOrDefault(issueDate.getText().trim(), LocalDate.now()));
            bill.setDueDate(parseDateOrDefault(dueDate.getText().trim(), bill.getIssueDate().plusDays(30)));
            bill.setCurrency("PHP");
            bill.setSubtotal(parseDoubleOrDefault(subtotal.getText().trim(), 0.0));
            bill.setTaxAmount(parseDoubleOrDefault(tax.getText().trim(), 0.0));
            bill.setPaidAmount(0.0);
            bill.setNotes(notes.getText().trim());
            accountsPayableService.issueBill(bill);
            refreshArApViews();
            refreshComplianceViews();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private JPanel buildPaymentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createCardBorder());
        panel.setBackground(CARD_BACKGROUND);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton postPaymentButton = createButton("+ Post Payment", SUCCESS, Color.WHITE);
        JButton refreshButton = createButton("Refresh", new Color(233, 237, 247), TEXT_PRIMARY);
        postPaymentButton.addActionListener(event -> openPaymentFormDialog());
        refreshButton.addActionListener(event -> refreshArApViews());
        actions.add(postPaymentButton);
        actions.add(refreshButton);

        paymentHelpArea.setEditable(false);
        paymentHelpArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        paymentHelpArea.setText("Use Invoice/Bill ID from tabs to post receipts/disbursements.\n");

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(paymentHelpArea), BorderLayout.CENTER);
        return panel;
    }

    private void openPaymentFormDialog() {
        if (accountsReceivableService == null || accountsPayableService == null) {
            showWarning("AR/AP services are not initialized.");
            return;
        }

        JComboBox<String> targetTypeCombo = new JComboBox<>(new String[] { "Invoice", "Bill" });
        targetTypeCombo.setSelectedItem(String.valueOf(paymentTargetTypeComboBox.getSelectedItem()));

        JTextField targetIdField = new JTextField(paymentTargetIdField.getText().trim(), 16);
        JTextField amountField = new JTextField(10);
        JTextField methodField = new JTextField(12);
        JTextField referenceField = new JTextField(12);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Target Type:"), gbc);
        gbc.gridx = 1; form.add(targetTypeCombo, gbc);
        gbc.gridx = 2; form.add(new JLabel("Target ID:"), gbc);
        gbc.gridx = 3; form.add(targetIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; form.add(amountField, gbc);
        gbc.gridx = 2; form.add(new JLabel("Method:"), gbc);
        gbc.gridx = 3; form.add(methodField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Reference:"), gbc);
        gbc.gridx = 1; form.add(referenceField, gbc);

        int result = JOptionPane.showConfirmDialog(this, form, "Post Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String targetType = String.valueOf(targetTypeCombo.getSelectedItem());
        String targetId = targetIdField.getText().trim();
        double amount = parseDoubleOrDefault(amountField.getText().trim(), -1.0);
        String method = methodField.getText().trim();
        String reference = referenceField.getText().trim();

        handlePostPayment(targetType, targetId, amount, method, reference);
    }

    private JPanel buildStatementsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createCardBorder());
        panel.setBackground(CARD_BACKGROUND);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.add(new JLabel("From (yyyy-MM-dd):"));
        controls.add(statementsFromDateField);
        controls.add(new JLabel("To (yyyy-MM-dd):"));
        controls.add(statementsToDateField);

        JButton generateButton = createButton("Generate Statements", PRIMARY, Color.WHITE);
        generateButton.addActionListener(event -> refreshStatements());
        controls.add(generateButton);

        statementsOutputArea.setEditable(false);
        statementsOutputArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(statementsOutputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildComplianceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(createCardBorder());
        panel.setBackground(CARD_BACKGROUND);

        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        topBar.setOpaque(false);
        complianceRoleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        complianceRoleLabel.setForeground(TEXT_PRIMARY);
        JButton refreshComplianceButton = createButton("Refresh Compliance", new Color(233, 237, 247), TEXT_PRIMARY);
        refreshComplianceButton.addActionListener(event -> refreshComplianceViews());
        topBar.add(complianceRoleLabel, BorderLayout.WEST);
        topBar.add(refreshComplianceButton, BorderLayout.EAST);

        JTabbedPane complianceTabs = new JTabbedPane();
        complianceTabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complianceTabs.addTab("Approvals", buildApprovalsPanel());
        complianceTabs.addTab("Period Locks", buildPeriodLocksPanel());
        complianceTabs.addTab("Audit Trail", buildAuditTrailPanel());

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(complianceTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        approvalTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applyModernTableStyle(approvalTable, 26);
        approvalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Remarks:"));
        actions.add(approvalRemarksField);

        JButton approveButton = createButton("Approve", SUCCESS, Color.WHITE);
        JButton rejectButton = createButton("Reject", DANGER, Color.WHITE);
        approveButton.addActionListener(event -> handleResolveApproval(true));
        rejectButton.addActionListener(event -> handleResolveApproval(false));
        actions.add(approveButton);
        actions.add(rejectButton);

        panel.add(new JScrollPane(approvalTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPeriodLocksPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        form.setOpaque(false);
        form.add(new JLabel("Start (yyyy-MM-dd):"));
        form.add(lockStartDateField);
        form.add(new JLabel("End (yyyy-MM-dd):"));
        form.add(lockEndDateField);
        form.add(new JLabel("Action:"));
        form.add(lockStatusComboBox);
        form.add(new JLabel("Remarks:"));
        form.add(lockRemarksField);

        JButton applyButton = createButton("Apply", PRIMARY, Color.WHITE);
        applyButton.addActionListener(event -> handleApplyPeriodLock());
        form.add(applyButton);

        periodLockTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applyModernTableStyle(periodLockTable, 26);
        periodLockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(periodLockTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAuditTrailPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        auditTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        applyModernTableStyle(auditTable, 26);
        auditTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(auditTable), BorderLayout.CENTER);
        return panel;
    }

    private void initializeComplianceDefaults() {
        if (lockStartDateField.getText().isBlank()) {
            LocalDate now = LocalDate.now();
            lockStartDateField.setText(now.withDayOfMonth(1).toString());
            lockEndDateField.setText(now.withDayOfMonth(now.lengthOfMonth()).toString());
        }
    }

    private void refreshComplianceViews() {
        if (complianceService == null) {
            complianceRoleLabel.setText("Role: not initialized");
            approvalTableModel.setRowCount(0);
            periodLockTableModel.setRowCount(0);
            auditTableModel.setRowCount(0);
            return;
        }

        try {
            complianceRoleLabel.setText("Role: " + complianceService.getCurrentRole() + " | User: " + complianceService.getCurrentUserEmail());

            approvalTableModel.setRowCount(0);
            for (ApprovalRequestView approval : complianceService.getPendingApprovals(100)) {
                approvalTableModel.addRow(new Object[] {
                        approval.id(),
                        approval.requestType(),
                        approval.sourceId(),
                        approval.requestedBy(),
                        approval.status(),
                        approval.requestedAt(),
                        approval.remarks()
                });
            }

            periodLockTableModel.setRowCount(0);
            for (PeriodLockView lock : complianceService.getRecentPeriodLocks(100)) {
                periodLockTableModel.addRow(new Object[] {
                        lock.id(),
                        lock.periodStart(),
                        lock.periodEnd(),
                        lock.locked(),
                        lock.lockedBy(),
                        lock.lockedAt()
                });
            }

            auditTableModel.setRowCount(0);
            for (AuditLogView log : complianceService.getRecentAuditLogs(150)) {
                auditTableModel.addRow(new Object[] {
                        log.changedAt(),
                        log.entityName(),
                        log.entityId(),
                        log.action(),
                        log.changedBy(),
                        log.reason()
                });
            }
        } catch (IllegalStateException exception) {
            complianceRoleLabel.setText("Role: unavailable");
            approvalTableModel.setRowCount(0);
            periodLockTableModel.setRowCount(0);
            auditTableModel.setRowCount(0);
            showWarning("Unable to load compliance data.\n" + exception.getMessage());
        }
    }

    private void handleResolveApproval(boolean approved) {
        if (complianceService == null) {
            showWarning("Compliance service is not initialized.");
            return;
        }

        int selectedRow = approvalTable.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Select a pending approval request first.");
            return;
        }

        String requestId = String.valueOf(approvalTableModel.getValueAt(selectedRow, 0));
        String remarks = approvalRemarksField.getText().trim();
        boolean updated = complianceService.resolveApprovalRequest(requestId, approved, remarks);
        if (!updated) {
            showWarning("The selected request could not be updated.");
            return;
        }

        approvalRemarksField.setText("");
        refreshComplianceViews();
    }

    private void handleApplyPeriodLock() {
        if (complianceService == null) {
            showWarning("Compliance service is not initialized.");
            return;
        }

        try {
            LocalDate startDate = parseDateOrDefault(lockStartDateField.getText().trim(), null);
            LocalDate endDate = parseDateOrDefault(lockEndDateField.getText().trim(), null);
            if (startDate == null || endDate == null) {
                showWarning("Period start and end dates are required.");
                return;
            }

            boolean shouldLock = "Lock".equalsIgnoreCase(String.valueOf(lockStatusComboBox.getSelectedItem()));
            String remarks = lockRemarksField.getText().trim();
            complianceService.setPeriodLock(startDate, endDate, shouldLock, remarks);
            lockRemarksField.setText("");
            refreshComplianceViews();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void refreshArApViews() {
        if (accountsReceivableService == null || accountsPayableService == null) {
            invoiceTableModel.setRowCount(0);
            billTableModel.setRowCount(0);
            paymentHelpArea.setText("AR/AP services are not initialized.");
            return;
        }

        try {
            refreshLookupDropdowns();

            List<Invoice> invoices = accountsReceivableService.getAllInvoices();
            invoiceTableModel.setRowCount(0);
            for (Invoice invoice : invoices) {
                invoiceTableModel.addRow(new Object[] {
                        invoice.getId(),
                        invoice.getInvoiceNo(),
                        resolveCustomerLabel(invoice.getCustomerId()),
                        invoice.getStatus(),
                        invoice.getDueDate(),
                        String.format("%.2f", Math.max(0.0, invoice.getTotalAmount() - invoice.getPaidAmount()))
                });
            }

            List<Bill> bills = accountsPayableService.getAllBills();
            billTableModel.setRowCount(0);
            for (Bill bill : bills) {
                billTableModel.addRow(new Object[] {
                        bill.getId(),
                        bill.getBillNo(),
                        resolveVendorLabel(bill.getVendorId()),
                        bill.getStatus(),
                        bill.getDueDate(),
                        String.format("%.2f", Math.max(0.0, bill.getTotalAmount() - bill.getPaidAmount()))
                });
            }

            paymentHelpArea.setText("Use Invoice/Bill ID from tabs to post receipts/disbursements.\n"
                    + "Invoices loaded: " + invoices.size() + "\n"
                    + "Bills loaded: " + bills.size());
            refreshArApKpis();
        } catch (IllegalStateException exception) {
            invoiceTableModel.setRowCount(0);
            billTableModel.setRowCount(0);
            paymentHelpArea.setText("Unable to load AR/AP data.\n" + exception.getMessage());
        }
    }

    private void refreshLookupDropdowns() {
        if (customerRepository != null) {
            LookupOption selectedCustomer = (LookupOption) invoiceCustomerComboBox.getSelectedItem();
            invoiceCustomerComboBox.removeAllItems();
            for (Customer customer : customerRepository.findAll()) {
                invoiceCustomerComboBox.addItem(new LookupOption(customer.getId(), customer.getName()));
            }
            restoreSelection(invoiceCustomerComboBox, selectedCustomer);
        }

        if (vendorRepository != null) {
            LookupOption selectedVendor = (LookupOption) billVendorComboBox.getSelectedItem();
            billVendorComboBox.removeAllItems();
            for (Vendor vendor : vendorRepository.findAll()) {
                billVendorComboBox.addItem(new LookupOption(vendor.getId(), vendor.getName()));
            }
            restoreSelection(billVendorComboBox, selectedVendor);
        }
    }

    private void restoreSelection(JComboBox<LookupOption> comboBox, LookupOption selected) {
        if (selected == null || selected.id == null) {
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
            return;
        }

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            LookupOption option = comboBox.getItemAt(i);
            if (Objects.equals(option.id, selected.id)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    private String resolveCustomerLabel(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return "";
        }
        for (int i = 0; i < invoiceCustomerComboBox.getItemCount(); i++) {
            LookupOption option = invoiceCustomerComboBox.getItemAt(i);
            if (Objects.equals(option.id, customerId)) {
                return option.label;
            }
        }
        return customerId;
    }

    private String resolveVendorLabel(String vendorId) {
        if (vendorId == null || vendorId.isBlank()) {
            return "";
        }
        for (int i = 0; i < billVendorComboBox.getItemCount(); i++) {
            LookupOption option = billVendorComboBox.getItemAt(i);
            if (Objects.equals(option.id, vendorId)) {
                return option.label;
            }
        }
        return vendorId;
    }

    private void refreshArApKpis() {
        if (accountsReceivableService == null || accountsPayableService == null) {
            openArLabel.setText("0.00");
            openApLabel.setText("0.00");
            overdueArLabel.setText("0");
            overdueApLabel.setText("0");
            return;
        }

        try {
            List<Invoice> invoices = accountsReceivableService.getAllInvoices();
            List<Bill> bills = accountsPayableService.getAllBills();

            openArLabel.setText(String.format("%.2f", accountsReceivableService.getOpenReceivableTotal()));
            openApLabel.setText(String.format("%.2f", accountsPayableService.getOpenPayableTotal()));

            long overdueInvoices = invoices.stream().filter(this::isOverdueInvoice).count();
            long overdueBills = bills.stream().filter(this::isOverdueBill).count();

            overdueArLabel.setText(String.valueOf(overdueInvoices));
            overdueApLabel.setText(String.valueOf(overdueBills));
        } catch (IllegalStateException exception) {
            openArLabel.setText("0.00");
            openApLabel.setText("0.00");
            overdueArLabel.setText("0");
            overdueApLabel.setText("0");
        }
    }

    private boolean isOverdueInvoice(Invoice invoice) {
        if (invoice.getStatus() == DocumentStatus.OVERDUE) {
            return true;
        }
        return invoice.getDueDate() != null
                && LocalDate.now().isAfter(invoice.getDueDate())
                && invoice.getPaidAmount() < invoice.getTotalAmount();
    }

    private boolean isOverdueBill(Bill bill) {
        if (bill.getStatus() == DocumentStatus.OVERDUE) {
            return true;
        }
        return bill.getDueDate() != null
                && LocalDate.now().isAfter(bill.getDueDate())
                && bill.getPaidAmount() < bill.getTotalAmount();
    }

    private void handleCreateInvoice() {
        if (accountsReceivableService == null) {
            showWarning("Accounts Receivable service is not initialized.");
            return;
        }

        try {
            LookupOption selectedCustomer = (LookupOption) invoiceCustomerComboBox.getSelectedItem();
            if (selectedCustomer == null || selectedCustomer.id == null || selectedCustomer.id.isBlank()) {
                showWarning("Select a customer before creating an invoice.");
                return;
            }

            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceNoField.getText().trim());
            invoice.setCustomerId(selectedCustomer.id);
            invoice.setIssueDate(parseDateOrDefault(invoiceIssueDateField.getText().trim(), LocalDate.now()));
            invoice.setDueDate(parseDateOrDefault(invoiceDueDateField.getText().trim(), invoice.getIssueDate().plusDays(30)));
            invoice.setCurrency("PHP");
            invoice.setSubtotal(parseDoubleOrDefault(invoiceSubtotalField.getText().trim(), 0.0));
            invoice.setTaxAmount(parseDoubleOrDefault(invoiceTaxField.getText().trim(), 0.0));
            invoice.setPaidAmount(0.0);
            invoice.setNotes(invoiceNotesField.getText().trim());
            accountsReceivableService.issueInvoice(invoice);
            refreshArApViews();
            refreshComplianceViews();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void handleCreateBill() {
        if (accountsPayableService == null) {
            showWarning("Accounts Payable service is not initialized.");
            return;
        }

        try {
            LookupOption selectedVendor = (LookupOption) billVendorComboBox.getSelectedItem();
            if (selectedVendor == null || selectedVendor.id == null || selectedVendor.id.isBlank()) {
                showWarning("Select a vendor before creating a bill.");
                return;
            }

            Bill bill = new Bill();
            bill.setBillNo(billNoField.getText().trim());
            bill.setVendorId(selectedVendor.id);
            bill.setIssueDate(parseDateOrDefault(billIssueDateField.getText().trim(), LocalDate.now()));
            bill.setDueDate(parseDateOrDefault(billDueDateField.getText().trim(), bill.getIssueDate().plusDays(30)));
            bill.setCurrency("PHP");
            bill.setSubtotal(parseDoubleOrDefault(billSubtotalField.getText().trim(), 0.0));
            bill.setTaxAmount(parseDoubleOrDefault(billTaxField.getText().trim(), 0.0));
            bill.setPaidAmount(0.0);
            bill.setNotes(billNotesField.getText().trim());
            accountsPayableService.issueBill(bill);
            refreshArApViews();
            refreshComplianceViews();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void handlePostPayment(String targetType, String targetId, double amount, String method, String reference) {
        if (accountsReceivableService == null || accountsPayableService == null) {
            showWarning("AR/AP services are not initialized.");
            return;
        }

        if (targetId.isBlank()) {
            showWarning("Target ID is required.");
            return;
        }

        if (amount <= 0.0) {
            showWarning("Payment amount must be greater than zero.");
            return;
        }

        try {
            boolean success;
            if ("Invoice".equalsIgnoreCase(targetType)) {
                success = accountsReceivableService.recordReceipt(targetId, amount, method, reference);
            } else {
                success = accountsPayableService.recordDisbursement(targetId, amount, method, reference);
            }

            if (!success) {
                showWarning("No matching document found for payment posting.");
                return;
            }

            refreshArApViews();
            refreshStatements();
            refreshComplianceViews();
        } catch (IllegalStateException exception) {
            showWarning(exception.getMessage());
        }
    }

    private void initializeStatementsDefaults() {
        if (statementsFromDateField.getText().isBlank()) {
            statementsFromDateField.setText(LocalDate.now().minusDays(30).toString());
        }
        if (statementsToDateField.getText().isBlank()) {
            statementsToDateField.setText(LocalDate.now().toString());
        }
    }

    private void refreshStatements() {
        if (statementsService == null) {
            statementsOutputArea.setText("Financial statements service is not initialized.");
            return;
        }

        try {
            LocalDate fromDate = parseDateOrDefault(statementsFromDateField.getText().trim(), LocalDate.now().minusDays(30));
            LocalDate toDate = parseDateOrDefault(statementsToDateField.getText().trim(), LocalDate.now());

            ProfitAndLossReport pnl = statementsService.generateProfitAndLoss(fromDate, toDate);
            CashFlowReport cashFlow = statementsService.generateCashFlow(fromDate, toDate);
            BalanceSheetReport balanceSheet = statementsService.generateBalanceSheet(toDate);

            StringBuilder output = new StringBuilder();
            output.append("PROFIT AND LOSS").append(System.lineSeparator())
                    .append("Period: ").append(fromDate).append(" to ").append(toDate).append(System.lineSeparator())
                    .append("Revenue: ").append(String.format("%.2f", pnl.getTotalRevenue())).append(System.lineSeparator())
                    .append("Expense: ").append(String.format("%.2f", pnl.getTotalExpense())).append(System.lineSeparator())
                    .append("Net Income: ").append(String.format("%.2f", pnl.getNetIncome())).append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("CASH FLOW").append(System.lineSeparator())
                    .append("Opening Cash: ").append(String.format("%.2f", cashFlow.getOpeningCash())).append(System.lineSeparator())
                    .append("Inflows: ").append(String.format("%.2f", cashFlow.getTotalInflows())).append(System.lineSeparator())
                    .append("Outflows: ").append(String.format("%.2f", cashFlow.getTotalOutflows())).append(System.lineSeparator())
                    .append("Net Cash Flow: ").append(String.format("%.2f", cashFlow.getNetCashFlow())).append(System.lineSeparator())
                    .append("Closing Cash: ").append(String.format("%.2f", cashFlow.getClosingCash())).append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("BALANCE SHEET").append(System.lineSeparator())
                    .append("As of: ").append(toDate).append(System.lineSeparator())
                    .append("Cash: ").append(String.format("%.2f", balanceSheet.getCash())).append(System.lineSeparator())
                    .append("Accounts Receivable: ").append(String.format("%.2f", balanceSheet.getAccountsReceivable())).append(System.lineSeparator())
                    .append("Total Assets: ").append(String.format("%.2f", balanceSheet.getTotalAssets())).append(System.lineSeparator())
                    .append("Accounts Payable: ").append(String.format("%.2f", balanceSheet.getAccountsPayable())).append(System.lineSeparator())
                    .append("Total Liabilities: ").append(String.format("%.2f", balanceSheet.getTotalLiabilities())).append(System.lineSeparator())
                    .append("Total Equity: ").append(String.format("%.2f", balanceSheet.getTotalEquity())).append(System.lineSeparator());

            statementsOutputArea.setText(output.toString());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            statementsOutputArea.setText("Unable to generate statements.\n" + exception.getMessage());
        }
    }

    private LocalDate parseDateOrDefault(String raw, LocalDate fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    private double parseDoubleOrDefault(String raw, double fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid numeric value: " + raw);
        }
    }

    private void clearForm() {
        selectedRecordId = null;
        recordTable.clearSelection();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            clearForm();
            setVisible(true);
        });
    }

    private static final class LookupOption {
        private final String id;
        private final String label;

        private LookupOption(String id, String label) {
            this.id = id;
            this.label = (label == null || label.isBlank()) ? id : label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
