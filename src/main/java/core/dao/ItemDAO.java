package core.dao;

import core.models.Item;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    private final Connection conn;
    public ItemDAO(Connection conn) {
        this.conn = conn;
    }
    public Item getItemByCode(String code) throws SQLException {
        String sql = "SELECT * FROM items WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Item(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                );
            }
        }
        return null;
    }
    public void updateItemQuantity(String code, int quantity) throws SQLException {
        String sql = "UPDATE items SET quantity = quantity - ? WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, code);
            stmt.executeUpdate();
        }
    }
}