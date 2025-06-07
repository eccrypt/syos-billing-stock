// === PRODUCTION VERSION ===
package cli;

import cli.menus.StockCLIHandler;
import core.command.Command;
import core.command.GenerateReportCommand;
import core.command.ReportInvoker;
import core.dao.BillDAO;
import core.dao.ItemDAO;
import core.dao.ShelfDAO;
import core.models.User;
import core.observer.ReorderNotifier;
import core.report.*;
import core.services.ItemService;
import core.services.ShelfService;
import core.services.StockService;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Scanner;

public class ReportCLI {
    private final Scanner sc;
    private final Connection connection;
    private final BillDAO billDAO;
    private final StockService stockService;
    private final ItemService itemService;
    private final ShelfService shelfService;
    private final ReorderNotifier reorderNotifier;
    private final boolean isTest;

    // === Constructor for production ===
    public ReportCLI(Scanner sc, Connection connection) {
        this.sc = sc;
        this.connection = connection;
        this.shelfService = new ShelfService(new ShelfDAO(connection));
        this.itemService = new ItemService(new ItemDAO(connection), shelfService);
        this.reorderNotifier = new ReorderNotifier(itemService);
        this.stockService = new StockService(connection, itemService, shelfService);
        this.stockService.registerObserver(reorderNotifier);
        this.billDAO = new BillDAO(connection);
        this.isTest = false;
    }

    // === Constructor for testing ===
    public ReportCLI(Scanner sc, Connection connection,
                     BillDAO billDAO,
                     StockService stockService,
                     ItemService itemService,
                     ShelfService shelfService,
                     ReorderNotifier reorderNotifier) {
        this.sc = sc;
        this.connection = connection;
        this.billDAO = billDAO;
        this.stockService = stockService;
        this.itemService = itemService;
        this.shelfService = shelfService;
        this.reorderNotifier = reorderNotifier;
        this.isTest = true;
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

            if (isTest) break;
        }
    }

    private void generateReorderReport() {
        try {
            ReportTemplate report = new ReorderReport(reorderNotifier, itemService);
            new GenerateReportCommand(report).execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate reorder report: " + e.getMessage());
        }
    }

    private void generateDailySalesReport() {
        try {
            System.out.print("📅 Enter date for report (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());
            ReportTemplate report = new DailySalesReporter(billDAO, date);
            new GenerateReportCommand(report).execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate daily sales report: " + e.getMessage());
        }
    }

    private void generateStockReport() {
        try {
            ReportTemplate report = new StockReport(stockService);
            new GenerateReportCommand(report).execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate stock report: " + e.getMessage());
        }
    }

    private void generateBillReport() {
        try {
            System.out.print("📅 Enter date for bill report (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());
            ReportTemplate report = new BillReport(billDAO, date);
            new GenerateReportCommand(report).execute();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate bill report: " + e.getMessage());
        }
    }

    private void generateAllReports() {
        try {
            System.out.print("📅 Enter date for sales and bill reports (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(sc.nextLine());
            ReportInvoker invoker = new ReportInvoker();
            invoker.addCommand(new GenerateReportCommand(new ReorderReport(reorderNotifier, itemService)));
            invoker.addCommand(new GenerateReportCommand(new DailySalesReporter(billDAO, date)));
            invoker.addCommand(new GenerateReportCommand(new StockReport(stockService)));
            invoker.addCommand(new GenerateReportCommand(new BillReport(billDAO, date)));
            invoker.runCommands();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate all reports: " + e.getMessage());
        }
    }
}
