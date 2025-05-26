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
}
