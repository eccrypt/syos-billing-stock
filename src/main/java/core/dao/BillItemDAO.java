package core.dao;

import core.models.BillItem;
import java.sql.*;
import java.util.List;

public class BillItemDAO {
    private final Connection conn;

    public BillItemDAO(Connection conn) {
        this.conn = conn;
    }

    public void saveBillItems(int billId, List<BillItem> items) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, item_code, quantity, total_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (BillItem item : items) {
                stmt.setInt(1, billId);
                stmt.setString(2, item.getItemCode());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getTotalPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}