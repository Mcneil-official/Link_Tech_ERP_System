package erp.link_tech_erp.finance.service;

public class BalanceSheetReport {
    private final double cash;
    private final double accountsReceivable;
    private final double totalAssets;
    private final double accountsPayable;
    private final double totalLiabilities;
    private final double totalEquity;

    public BalanceSheetReport(double cash, double accountsReceivable, double accountsPayable) {
        this.cash = cash;
        this.accountsReceivable = accountsReceivable;
        this.totalAssets = cash + accountsReceivable;
        this.accountsPayable = accountsPayable;
        this.totalLiabilities = accountsPayable;
        this.totalEquity = totalAssets - totalLiabilities;
    }

    public double getCash() {
        return cash;
    }

    public double getAccountsReceivable() {
        return accountsReceivable;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public double getAccountsPayable() {
        return accountsPayable;
    }

    public double getTotalLiabilities() {
        return totalLiabilities;
    }

    public double getTotalEquity() {
        return totalEquity;
    }
}
