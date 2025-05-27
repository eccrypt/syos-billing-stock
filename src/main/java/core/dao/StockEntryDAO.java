package core.dao;

import core.models.StockEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockEntryDAO {
    private final Connection conn;

    public StockEntryDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertStockEntry(StockEntry entry) throws SQLException {
        String sql = "INSERT INTO stock_entries (item_code, quantity, entry_date, expiry_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getItemCode());
            stmt.setInt(2, entry.getQuantity());
            stmt.setDate(3, new java.sql.Date(entry.getEntryDate().getTime()));
            stmt.setDate(4, new java.sql.Date(entry.getExpiryDate().getTime()));
            stmt.executeUpdate();
        }
    }

    public List<StockEntry> getAvailableStock(String itemCode) throws SQLException {
        String sql = "SELECT * FROM stock_entries WHERE item_code = ? AND quantity > 0";
        List<StockEntry> entries = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(new StockEntry(
                        rs.getString("item_code"),
                        rs.getInt("quantity"),
                        rs.getDate("entry_date"),
                        rs.getDate("expiry_date")
                ));
            }
        }

        return entries;
    }

    public void reduceStockEntry(StockEntry entry, int reduceQty) throws SQLException {
        String sql = "UPDATE stock_entries SET quantity = quantity - ? WHERE item_code = ? AND entry_date = ? AND expiry_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reduceQty);
            stmt.setString(2, entry.getItemCode());
            stmt.setDate(3, new java.sql.Date(entry.getEntryDate().getTime()));
            stmt.setDate(4, new java.sql.Date(entry.getExpiryDate().getTime()));
            stmt.executeUpdate();
        }
    }
}
