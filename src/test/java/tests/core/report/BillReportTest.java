package tests.core.report;

import core.dao.BillDAO;
import core.models.Bill;
import core.report.BillReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class BillReportTest {

    private BillReport billReport;
    private BillDAO billDAO;

    @BeforeEach
    void setUp() {
        billDAO = mock(BillDAO.class);
        billReport = new BillReport(billDAO, LocalDate.of(2025, 5, 10));
    }

    @Test
    void testGenerate_BillReport() throws Exception {
        // Mocking the BillDAO to return a list of Bills
        Bill bill1 = mock(Bill.class);
        Bill bill2 = mock(Bill.class);

        when(bill1.getSerialNumber()).thenReturn(1);
        when(bill1.getTotal()).thenReturn(100.0);
        when(bill1.getDiscount()).thenReturn(10.0);
        when(bill1.getCashTendered()).thenReturn(90.0);
        when(bill1.getChangeDue()).thenReturn(10.0);

        when(bill2.getSerialNumber()).thenReturn(2);
        when(bill2.getTotal()).thenReturn(150.0);
        when(bill2.getDiscount()).thenReturn(15.0);
        when(bill2.getCashTendered()).thenReturn(135.0);
        when(bill2.getChangeDue()).thenReturn(20.0);

        List<Bill> bills = Arrays.asList(bill1, bill2);

        when(billDAO.getBillsByDate(LocalDate.of(2025, 5, 10))).thenReturn(bills);

        // Running the report generation
        billReport.generate();

        // Verify that getBillsByDate is called once with the specified date
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 5, 10));

        // No assertions for printed output, but you can capture and check output if needed
    }

    @Test
    void testGenerate_EmptyBillList() throws Exception {
        // Mocking empty bills list
        when(billDAO.getBillsByDate(LocalDate.of(2025, 5, 10))).thenReturn(Arrays.asList());

        // Running the report generation
        billReport.generate();

        // Verify method call
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 5, 10));
    }

    @Test
    void testGenerate_ErrorFetchingBills() throws Exception {
        // Simulate error in DAO
        when(billDAO.getBillsByDate(LocalDate.of(2025, 5, 10))).thenThrow(new RuntimeException("Database error"));

        // Running the report generation
        billReport.generate();

        // Verify method call and exception handling
        verify(billDAO, times(1)).getBillsByDate(LocalDate.of(2025, 5, 10));
    }
}
