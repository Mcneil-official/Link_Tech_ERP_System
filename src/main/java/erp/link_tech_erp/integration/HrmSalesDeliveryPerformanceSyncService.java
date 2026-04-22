package erp.link_tech_erp.integration;

import java.time.LocalDate;

import erp.link_tech_erp.hrm.DatabaseConnection;
import erp.link_tech_erp.sales.SalesOrder;
import erp.link_tech_erp.sales.SalesOrderRepository;

public final class HrmSalesDeliveryPerformanceSyncService {

    private final SalesOrderRepository salesOrderRepository;

    private HrmSalesDeliveryPerformanceSyncService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    public static HrmSalesDeliveryPerformanceSyncService createDefault() {
        return new HrmSalesDeliveryPerformanceSyncService(new SalesOrderRepository());
    }

    public void assignOrderToEmployee(int orderId, String employeeId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id is required.");
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee id is required.");
        }

        // Validate employee exists in HRM
        String[] employee = DatabaseConnection.getEmployeeById(employeeId.trim());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found in HRM: " + employeeId.trim());
        }

        // Validate order exists in Sales
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        // Persist assignment (this will be implemented in a new DeliveryAssignmentRepository)
        // For now, this serves as a validation point
    }

    public void syncDeliveredOutcome(int orderId, String employeeId, boolean onTime, double rating) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id is required.");
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee id is required.");
        }
        if (rating < 0.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5.");
        }

        // Validate order exists and is actually delivered
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + orderId));

        if (!"Delivered".equalsIgnoreCase(order.getDeliveryStatus())) {
            throw new IllegalArgumentException("Order is not in Delivered status: " + orderId);
        }

        // Validate employee exists in HRM
        String[] employee = DatabaseConnection.getEmployeeById(employeeId.trim());
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found in HRM: " + employeeId.trim());
        }

        // Build description for audit trail
        LocalDate deliveryDate = order.getOrderDate() != null 
            ? order.getOrderDate().toLocalDateTime().toLocalDate()
            : LocalDate.now();
        String employeeName = employee.length > 1 ? employee[1] : employeeId;

        // Log to console for now (will be extended to persist metrics via repository)
        String onTimeStr = onTime ? "on-time" : "late";
        String ratingStr = String.format("%.1f/5.0", rating);
        String message = String.format(
            "Delivery completed for order #%d by %s on %s (%s, rating: %s)",
            orderId, employeeName, deliveryDate, onTimeStr, ratingStr
        );
        System.out.println("[HrmSalesDeliveryPerformanceSyncService] " + message);
    }
}
