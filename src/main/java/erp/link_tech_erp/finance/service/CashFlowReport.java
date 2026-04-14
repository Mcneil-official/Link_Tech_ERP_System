package erp.link_tech_erp.finance.service;

public class CashFlowReport {
    private final double openingCash;
    private final double totalInflows;
    private final double totalOutflows;
    private final double netCashFlow;
    private final double closingCash;

    public CashFlowReport(double openingCash, double totalInflows, double totalOutflows) {
        this.openingCash = openingCash;
        this.totalInflows = totalInflows;
        this.totalOutflows = totalOutflows;
        this.netCashFlow = totalInflows - totalOutflows;
        this.closingCash = openingCash + netCashFlow;
    }

    public double getOpeningCash() {
        return openingCash;
    }

    public double getTotalInflows() {
        return totalInflows;
    }

    public double getTotalOutflows() {
        return totalOutflows;
    }

    public double getNetCashFlow() {
        return netCashFlow;
    }

    public double getClosingCash() {
        return closingCash;
    }
}
