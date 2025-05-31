package core.dao;

import core.models.StockEntry;
import core.repositories.StockEntryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockEntryDAO implements StockEntryRepository {
    private final Connection conn;

    public StockEntryDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(StockEntry entry) throws SQLException {
        String sql = "INSERT INTO stock_entries (item_code, quantity, entry_date, expiry_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entry.getItemCode());
            stmt.setInt(2, entry.getQuantity());
            stmt.setDate(3, new java.sql.Date(entry.getEntryDate().getTime()));
            stmt.setDate(4, new java.sql.Date(entry.getExpiryDate().getTime()));
            stmt.executeUpdate();

            // Set the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entry.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(StockEntry entry) throws SQLException {
        String sql = "UPDATE stock_entries SET quantity = ?, entry_date = ?, expiry_date = ? WHERE stock_entry_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entry.getQuantity());
            stmt.setDate(2, new java.sql.Date(entry.getEntryDate().getTime()));
            stmt.setDate(3, new java.sql.Date(entry.getExpiryDate().getTime()));
            stmt.setInt(4, entry.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(StockEntry entry) throws SQLException {
        String sql = "DELETE FROM stock_entries WHERE stock_entry_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entry.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<StockEntry> findAll() throws SQLException {
        String sql = "SELECT * FROM stock_entries";
        List<StockEntry> entries = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                entries.add(new StockEntry(
                        rs.getInt("stock_entry_id"), // Corrected column name
                        rs.getString("item_code"),
                        rs.getInt("quantity"),
                        rs.getDate("entry_date"),
                        rs.getDate("expiry_date")
                ));
            }
        }
        return entries;
    }

    @Override
    public List<StockEntry> findAvailableByItemCode(String itemCode) throws SQLException {
        String sql = "SELECT * FROM stock_entries WHERE item_code = ? AND quantity > 0";
        List<StockEntry> entries = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entries.add(new StockEntry(
                        rs.getInt("stock_entry_id"), // Corrected column name
                        rs.getString("item_code"),
                        rs.getInt("quantity"),
                        rs.getDate("entry_date"),
                        rs.getDate("expiry_date")
                ));
            }
        }

        return entries;
    }

    @Override
    public void reduceQuantity(StockEntry entry, int reduceQty) throws SQLException {
        String sql = "UPDATE stock_entries SET quantity = quantity - ? WHERE stock_entry_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reduceQty);
            stmt.setInt(2, entry.getId());
            stmt.executeUpdate();
        }
    }
}
