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

    // Method to generate the product code (incremental approach)
    private String generateProductCode() throws SQLException {
        String sqlGetLastCode = "SELECT code FROM items ORDER BY items_id DESC LIMIT 1";
        String newCode = "ITEM-0001";  // Default starting value

        try (PreparedStatement stmt = conn.prepareStatement(sqlGetLastCode);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String lastCode = rs.getString("code");
                String lastNumber = lastCode.substring(lastCode.lastIndexOf("-") + 1);  // Extract number part
                int newNumber = Integer.parseInt(lastNumber) + 1;  // Increment it
                newCode = "ITEM-" + String.format("%04d", newNumber);  // Format with leading zeros
            }
        }
        return newCode;
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
        String newCode = generateProductCode();  // Generate the product code

        // Insert the item into the database with the generated code
        String sql = "INSERT INTO items (code, name, price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newCode);  // Use the generated code
            stmt.setString(2, item.getName());
            stmt.setDouble(3, item.getPrice());
            stmt.executeUpdate();
        }

        item.setCode(newCode);  // Set the auto-generated code on the item object
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
