package core.dao;

import core.models.Shelf;
import core.repositories.ShelfRepository;

import java.sql.*;

public class ShelfDAO implements ShelfRepository {
    private final Connection conn;

    public ShelfDAO(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void addShelf(Shelf shelf) throws SQLException {
        String sql = "INSERT INTO shelves (product_code, shelf_default, shelf_current) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shelf.getProductCode());
            stmt.setInt(2, shelf.getShelfDefault());
            stmt.setInt(3, shelf.getShelfCurrent());
            stmt.executeUpdate();
        }
    }

    @Override
    public Shelf getShelfByProductCode(String productCode) throws SQLException {
        String sql = "SELECT * FROM shelves WHERE product_code = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Shelf(
                        rs.getInt("shelf_id"),
                        rs.getString("product_code"),
                        rs.getInt("shelf_default"),
                        rs.getInt("shelf_current")
                );
            }
        }
        return null;  // No shelf found
    }

    @Override
    public void updateShelf(Shelf shelf) throws SQLException {
        String sql = "UPDATE shelves SET shelf_default = ?, shelf_current = ? WHERE product_code = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, shelf.getShelfDefault());
            stmt.setInt(2, shelf.getShelfCurrent());
            stmt.setString(3, shelf.getProductCode());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteShelf(String productCode) throws SQLException {
        String sql = "DELETE FROM shelves WHERE product_code = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode);
            stmt.executeUpdate();
        }
    }
}
