package core.dao;

import core.models.Bill;
import java.sql.*;

public class BillDAO {
    private final Connection conn;

    public BillDAO(Connection conn) {
        this.conn = conn;
    }

    public int saveBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (total, discount, cash_tendered, change_due) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, bill.getTotal());
            stmt.setDouble(2, bill.getDiscount());
            stmt.setDouble(3, bill.getCashTendered());
            stmt.setDouble(4, bill.getChangeDue());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }
    public int getNextBillSerialNumber() throws SQLException {
        String query = "SELECT COALESCE(MAX(serial_number), 0) + 1 AS next_serial FROM bills";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_serial");
            }
            return 1;
        }
    }
}
