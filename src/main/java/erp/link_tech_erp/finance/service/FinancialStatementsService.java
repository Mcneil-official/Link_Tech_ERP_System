package erp.link_tech_erp.finance.service;

import java.time.LocalDate;
import java.util.List;

import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.RecordType;

public class FinancialStatementsService {
    private final FinanceService financeService;
    private final AccountsReceivableService accountsReceivableService;
    private final AccountsPayableService accountsPayableService;

    public FinancialStatementsService(FinanceService financeService,
                                      AccountsReceivableService accountsReceivableService,
                                      AccountsPayableService accountsPayableService) {
        this.financeService = financeService;
        this.accountsReceivableService = accountsReceivableService;
        this.accountsPayableService = accountsPayableService;
    }

    public ProfitAndLossReport generateProfitAndLoss(LocalDate fromDate, LocalDate toDate) {
        DateRange range = normalizeRange(fromDate, toDate);
        List<FinancialRecord> records = financeService.getAllRecords();

        double revenue = records.stream()
                .filter(record -> isWithinRange(record, range.fromDate(), range.toDate()))
                .filter(record -> record.getType() == RecordType.INCOME)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        double expense = records.stream()
                .filter(record -> isWithinRange(record, range.fromDate(), range.toDate()))
                .filter(record -> record.getType() == RecordType.EXPENSE)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        return new ProfitAndLossReport(revenue, expense);
    }

    public CashFlowReport generateCashFlow(LocalDate fromDate, LocalDate toDate) {
        DateRange range = normalizeRange(fromDate, toDate);
        List<FinancialRecord> records = financeService.getAllRecords();

        double openingCash = records.stream()
                .filter(record -> record.getDate() != null && record.getDate().isBefore(range.fromDate()))
                .mapToDouble(record -> record.getType() == RecordType.INCOME ? record.getAmount() : -record.getAmount())
                .sum();

        double inflows = records.stream()
                .filter(record -> isWithinRange(record, range.fromDate(), range.toDate()))
                .filter(record -> record.getType() == RecordType.INCOME)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        double outflows = records.stream()
                .filter(record -> isWithinRange(record, range.fromDate(), range.toDate()))
                .filter(record -> record.getType() == RecordType.EXPENSE)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();

        return new CashFlowReport(openingCash, inflows, outflows);
    }

    public BalanceSheetReport generateBalanceSheet(LocalDate asOfDate) {
        LocalDate effectiveDate = asOfDate == null ? LocalDate.now() : asOfDate;
        List<FinancialRecord> records = financeService.getAllRecords();

        double cash = records.stream()
                .filter(record -> record.getDate() != null && !record.getDate().isAfter(effectiveDate))
                .mapToDouble(record -> record.getType() == RecordType.INCOME ? record.getAmount() : -record.getAmount())
                .sum();

        double accountsReceivable = 0.0;
        double accountsPayable = 0.0;

        if (accountsReceivableService != null) {
            try {
                accountsReceivable = accountsReceivableService.getOpenReceivableTotal();
            } catch (IllegalStateException ignored) {
                accountsReceivable = 0.0;
            }
        }

        if (accountsPayableService != null) {
            try {
                accountsPayable = accountsPayableService.getOpenPayableTotal();
            } catch (IllegalStateException ignored) {
                accountsPayable = 0.0;
            }
        }

        return new BalanceSheetReport(cash, accountsReceivable, accountsPayable);
    }

    private DateRange normalizeRange(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(30) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        if (to.isBefore(from)) {
            return new DateRange(to, from);
        }
        return new DateRange(from, to);
    }

    private boolean isWithinRange(FinancialRecord record, LocalDate from, LocalDate to) {
        if (record.getDate() == null) {
            return false;
        }
        return !record.getDate().isBefore(from) && !record.getDate().isAfter(to);
    }

    private record DateRange(LocalDate fromDate, LocalDate toDate) {
    }
}
