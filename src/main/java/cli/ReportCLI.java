package cli;

import core.command.Command;
import core.command.GenerateReportCommand;
import core.command.ReportInvoker;
import core.dao.BillDAO;
import core.dao.ItemDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.report.*;
import core.services.ItemService;
import core.services.StockService;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportCLI {
    private final Scanner sc;
    private final Connection connection;

    public ReportCLI(Scanner sc, Connection connection) {
        this.sc = sc;
        this.connection = connection;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println("\n=== Reports Menu ===");
            System.out.println("1. Reorder Level Report");
            System.out.println("2. Daily Sales Report");
            System.out.println("3. Stock Report");
            System.out.println("4. Bill Report");
            System.out.println("5. Generate All Reports");
            System.out.println("0. Back");

            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> generateReorderReport();
                case "2" -> generateDailySalesReport();
                case "3" -> generateStockReport();
                case "4" -> generateBillReport();
                case "5" -> generateAllReports();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void generateReorderReport() {
        try {
            ItemService itemService = new ItemService(new ItemDAO(connection));
            StockService stockService = new StockService(connection, itemService);

            // ReorderNotifier should have been updated during real stock activity
            ReorderNotifier notifier = new ReorderNotifier(itemService);
            stockService.registerObserver(notifier); // Simulate updates in this context

            ReportTemplate report = new ReorderReport(notifier, itemService);
            Command command = new GenerateReportCommand(report);
            command.execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate reorder report: " + e.getMessage());
        }
    }

    private void generateDailySalesReport() {
        try {
            BillDAO billDAO = new BillDAO(connection);
            System.out.print("📅 Enter date for report (YYYY-MM-DD): ");
            String dateInput = sc.nextLine();
            LocalDate date = LocalDate.parse(dateInput);

            ReportTemplate report = new DailySalesReporter(billDAO, date);
            Command command = new GenerateReportCommand(report);
            command.execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate daily sales report: " + e.getMessage());
        }
    }

    private void generateStockReport() {
        try {
            ItemService itemService = new ItemService(new ItemDAO(connection));
            StockService stockService = new StockService(connection, itemService);
            ReportTemplate report = new StockReport(stockService);
            Command command = new GenerateReportCommand(report);
            command.execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate stock report: " + e.getMessage());
        }
    }

    private void generateBillReport() {
        try {
            BillDAO billDAO = new BillDAO(connection);

            System.out.print("📅 Enter date for bill report (YYYY-MM-DD): ");
            String dateInput = sc.nextLine();
            LocalDate date = LocalDate.parse(dateInput);

            ReportTemplate report = new BillReport(billDAO, date);
            Command command = new GenerateReportCommand(report);
            command.execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate bill report: " + e.getMessage());
        }
    }

    private void generateAllReports() {
        try {
            ReportInvoker invoker = new ReportInvoker();

            // Reorder Report
            ItemService itemService = new ItemService(new ItemDAO(connection));
            StockService stockService = new StockService(connection, itemService);
            ReorderNotifier notifier = new ReorderNotifier(itemService);
            stockService.registerObserver(notifier);
            invoker.addCommand(new GenerateReportCommand(new ReorderReport(notifier, itemService)));

            // Daily Sales Report
            BillDAO billDAO = new BillDAO(connection);
            System.out.print("📅 Enter date for sales and bill reports (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());
            invoker.addCommand(new GenerateReportCommand(new DailySalesReporter(billDAO, date)));

            // Stock Report
            invoker.addCommand(new GenerateReportCommand(new StockReport(stockService)));

            // Bill Report
            invoker.addCommand(new GenerateReportCommand(new BillReport(billDAO, date)));

            // Execute all
            invoker.runCommands();

        } catch (Exception e) {
            System.out.println("❌ Failed to generate all reports: " + e.getMessage());
        }
    }

}
