package erp.link_tech_erp.integration;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import erp.link_tech_erp.finance.repository.ApprovalRequestRepository;
import erp.link_tech_erp.finance.repository.AuditLogRepository;
import erp.link_tech_erp.finance.repository.BillRepository;
import erp.link_tech_erp.finance.repository.CustomerRepository;
import erp.link_tech_erp.finance.repository.FinancialRecordRepository;
import erp.link_tech_erp.finance.repository.InvoiceRepository;
import erp.link_tech_erp.finance.repository.PaymentRepository;
import erp.link_tech_erp.finance.repository.PeriodLockRepository;
import erp.link_tech_erp.finance.repository.SupabaseRestConfig;
import erp.link_tech_erp.finance.repository.UserRoleRepository;
import erp.link_tech_erp.finance.repository.VendorRepository;
import erp.link_tech_erp.finance.service.AccountsPayableService;
import erp.link_tech_erp.finance.service.AccountsReceivableService;
import erp.link_tech_erp.finance.service.ComplianceService;
import erp.link_tech_erp.finance.service.FinanceService;
import erp.link_tech_erp.finance.service.FinancialStatementsService;
import erp.link_tech_erp.finance.ui.FinanceDashboardFrame;
import erp.link_tech_erp.integration.auth.GlobalSessionContext;

public final class FinanceModuleLauncher {
    private FinanceModuleLauncher() {
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                SupabaseRestConfig restConfig = SupabaseRestConfig.fromEnvironment();

                UserRoleRepository userRoleRepository = new UserRoleRepository(restConfig);
                PeriodLockRepository periodLockRepository = new PeriodLockRepository(restConfig);
                ApprovalRequestRepository approvalRequestRepository = new ApprovalRequestRepository(restConfig);
                AuditLogRepository auditLogRepository = new AuditLogRepository(restConfig);
                FinancialRecordRepository financialRecordRepository = new FinancialRecordRepository(restConfig);
                InvoiceRepository invoiceRepository = new InvoiceRepository(restConfig);
                BillRepository billRepository = new BillRepository(restConfig);
                PaymentRepository paymentRepository = new PaymentRepository(restConfig);
                CustomerRepository customerRepository = new CustomerRepository(restConfig);
                VendorRepository vendorRepository = new VendorRepository(restConfig);

                ComplianceService complianceService = new ComplianceService(
                    userRoleRepository,
                    periodLockRepository,
                    approvalRequestRepository,
                    auditLogRepository
                );
                FinanceService financeService = new FinanceService(financialRecordRepository, complianceService);
                AccountsReceivableService accountsReceivableService =
                    new AccountsReceivableService(invoiceRepository, paymentRepository, complianceService);
                AccountsPayableService accountsPayableService =
                    new AccountsPayableService(billRepository, paymentRepository, complianceService);
                FinancialStatementsService statementsService =
                    new FinancialStatementsService(financeService, accountsReceivableService, accountsPayableService);
                FinanceDashboardFrame dashboard = new FinanceDashboardFrame(
                    financeService,
                    accountsReceivableService,
                    accountsPayableService,
                    statementsService,
                    complianceService,
                    customerRepository,
                    vendorRepository,
                    () -> {
                        GlobalSessionContext.clear();
                        GlobalLoginFrame.launch();
                    }
                );
                dashboard.setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(
                    null,
                    "Finance module failed to start. Check Supabase config env vars and try again.\n\n" + exception.getMessage(),
                    "Finance Launch Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
