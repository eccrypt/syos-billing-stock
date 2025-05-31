package core.utils;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class SerialNumberGenerator {
    private static final String FILE_PATH = "data/serial_number.txt";
    private static AtomicInteger currentSerial = new AtomicInteger(1);
    private static LocalDate lastDate = LocalDate.now();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

    static {
        loadSerialAndDate();
    }

    private static void loadSerialAndDate() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                ensureFileExists(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String dateLine = reader.readLine();
            String serialLine = reader.readLine();

            if (dateLine != null && serialLine != null) {
                LocalDate fileDate = LocalDate.parse(dateLine, formatter);
                int fileSerial = Integer.parseInt(serialLine);

                LocalDate today = LocalDate.now();

                if (fileDate.equals(today)) {
                    // same day, continue serial
                    lastDate = fileDate;
                    currentSerial.set(fileSerial);
                } else {
                    // different day, reset serial
                    lastDate = today;
                    currentSerial.set(1);
                    saveSerialAndDate();
                }
            } else {
                // file malformed - reset
                lastDate = LocalDate.now();
                currentSerial.set(1);
                saveSerialAndDate();
            }
        } catch (Exception e) {
            // error reading - reset
            lastDate = LocalDate.now();
            currentSerial.set(1);
            saveSerialAndDate();
        }
    }

    private static void saveSerialAndDate() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(lastDate.format(formatter));
            writer.newLine();
            writer.write(String.valueOf(currentSerial.get()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized int getNextSerial() {
        int next = currentSerial.getAndIncrement();
        saveSerialAndDate();
        return next;
    }

    private static void ensureFileExists(File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // initialize with today's date and serial 1
                writer.write(LocalDate.now().format(formatter));
                writer.newLine();
                writer.write("1");
            }
        }
    }
}
