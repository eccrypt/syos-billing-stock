package tests.core.dao;

import core.billing.BasicBill;
import core.dao.BillDAO;
import core.models.Bill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BillDAOTest {

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    private BillDAO billDAO;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        billDAO = new BillDAO(conn);
    }

    @Test
    void testSaveBill() throws SQLException {
        Bill bill = new BasicBill(100.0, 10.0, 90.0, 10.0, Arrays.asList(), 1);

        // Mocking the PreparedStatement behavior
        when(conn.prepareStatement(any(String.class), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1); // Simulate successful insertion
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1); // Simulate generated key for the bill

        int generatedId = billDAO.saveBill(bill);

        verify(stmt, times(1)).setInt(1, bill.getSerialNumber());
        verify(stmt, times(1)).setDouble(2, bill.getTotal());
        verify(stmt, times(1)).setDouble(3, bill.getDiscount());
        verify(stmt, times(1)).setDouble(4, bill.getCashTendered());
        verify(stmt, times(1)).setDouble(5, bill.getChangeDue());
        verify(stmt, times(1)).executeUpdate();

        assertEquals(1, generatedId); // Verify the generated ID
    }

    @Test
    void testGetAllBills() throws SQLException {
        Bill bill1 = new BasicBill(100.0, 10.0, 90.0, 10.0, Arrays.asList(), 1);
        Bill bill2 = new BasicBill(150.0, 15.0, 135.0, 20.0, Arrays.asList(), 2);
        List<Bill> bills = Arrays.asList(bill1, bill2);

        // Mocking the ResultSet behavior
        when(conn.prepareStatement(any(String.class))).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false); // Simulate two bills
        when(rs.getDouble("total")).thenReturn(bill1.getTotal(), bill2.getTotal());
        when(rs.getDouble("discount")).thenReturn(bill1.getDiscount(), bill2.getDiscount());
        when(rs.getDouble("cash_tendered")).thenReturn(bill1.getCashTendered(), bill2.getCashTendered());
        when(rs.getDouble("change_due")).thenReturn(bill1.getChangeDue(), bill2.getChangeDue());
        when(rs.getInt("serial_number")).thenReturn(bill1.getSerialNumber(), bill2.getSerialNumber());

        List<Bill> result = billDAO.getAllBills();

        assertEquals(2, result.size()); // Ensure we have two bills
        assertEquals(bill1.getSerialNumber(), result.get(0).getSerialNumber());
        assertEquals(bill2.getSerialNumber(), result.get(1).getSerialNumber());
    }

    @Test
    void testGetBillsByDate() throws SQLException {
        LocalDate targetDate = LocalDate.of(2025, 5, 10);
        Bill bill1 = new BasicBill(100.0, 10.0, 90.0, 10.0, Arrays.asList(), 1);
        List<Bill> bills = Arrays.asList(bill1);

        // Mocking the PreparedStatement behavior
        when(conn.prepareStatement(any(String.class))).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false); // Simulate one bill
        when(rs.getDouble("total")).thenReturn(bill1.getTotal());
        when(rs.getDouble("discount")).thenReturn(bill1.getDiscount());
        when(rs.getDouble("cash_tendered")).thenReturn(bill1.getCashTendered());
        when(rs.getDouble("change_due")).thenReturn(bill1.getChangeDue());
        when(rs.getInt("serial_number")).thenReturn(bill1.getSerialNumber());

        List<Bill> result = billDAO.getBillsByDate(targetDate);

        assertEquals(1, result.size()); // Ensure we have one bill
        assertEquals(bill1.getSerialNumber(), result.get(0).getSerialNumber());
    }

    @Test
    void testGetBillById() throws SQLException {
        Bill bill = new BasicBill(100.0, 10.0, 90.0, 10.0, Arrays.asList(), 1);

        // Mocking the PreparedStatement behavior
        when(conn.prepareStatement(any(String.class))).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true).thenReturn(false); // Simulate one bill
        when(rs.getDouble("total")).thenReturn(bill.getTotal());
        when(rs.getDouble("discount")).thenReturn(bill.getDiscount());
        when(rs.getDouble("cash_tendered")).thenReturn(bill.getCashTendered());
        when(rs.getDouble("change_due")).thenReturn(bill.getChangeDue());
        when(rs.getInt("serial_number")).thenReturn(bill.getSerialNumber());

        Bill result = billDAO.getBillById(1);

        assertNotNull(result); // Ensure the result is not null
        assertEquals(bill.getSerialNumber(), result.getSerialNumber());
    }
}
