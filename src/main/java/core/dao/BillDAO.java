package core.dao;

import core.billing.BasicBill;
import core.models.Bill;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    private final Connection conn;

    public BillDAO(Connection conn) {
        this.conn = conn;
    }

    public int saveBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (serial_number, total, discount, cash_tendered, change_due, bill_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bill.getSerialNumber());
            stmt.setDouble(2, bill.getTotal());
            stmt.setDouble(3, bill.getDiscount());
            stmt.setDouble(4, bill.getCashTendered());
            stmt.setDouble(5, bill.getChangeDue());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public List<Bill> getAllBills() throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY bill_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }
        return bills;
    }

    public List<Bill> getBillsByDate(LocalDate date) throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE DATE(bill_date) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                bills.add(mapResultSetToBill(rs));
            }
        }
        return bills;
    }

    public Bill getBillById(int id) throws SQLException {
        String sql = "SELECT * FROM bills WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBill(rs);
            }
        }
        return null;
    }


    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        return new BasicBill(
                rs.getDouble("total"),
                rs.getDouble("discount"),
                rs.getDouble("cash_tendered"),
                rs.getDouble("change_due"),
                new ArrayList<>(), // You can load BillItems separately if needed
                rs.getInt("serial_number")
        );
    }
}
