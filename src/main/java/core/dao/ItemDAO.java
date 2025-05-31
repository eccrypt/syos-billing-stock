package core.dao;

import core.models.Item;
import core.repositories.ItemRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO implements ItemRepository {
    private final Connection conn;
    public ItemDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Item getItemByCode(String code) throws SQLException {
        String sql = "SELECT * FROM items WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Item(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price")
                );
            }
        }
        return null;
    }

    @Override
    public List<Item> getAllItems() throws SQLException {
        String sql = "SELECT * FROM items";
        List<Item> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                items.add(new Item(
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getDouble("price")
                ));
            }
        }
        return items;
    }

    @Override
    public void addItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (code, name, price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getCode());
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateItem(Item item) throws SQLException {
        String sql = "UPDATE items SET name = ?, price = ? WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getCode());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteItem(String code) throws SQLException {
        String sql = "DELETE FROM items WHERE code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.executeUpdate();
        }
    }
}
