package core.utils;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SerialNumberGenerator {
    private static final String FILE_PATH = "data/serial_number.txt";
    private static AtomicInteger currentSerial = new AtomicInteger(loadSerial());

    private static int loadSerial() {
        try {
            File file = new File(FILE_PATH);
            ensureFileExists(file);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                return line != null ? Integer.parseInt(line) : 1;
            }
        } catch (Exception e) {
            return 1; // fallback to 1 if anything goes wrong
        }
    }

    private static void saveSerial(int serial) {
        try {
            File file = new File(FILE_PATH);
            ensureFileExists(file);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(String.valueOf(serial));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ensureFileExists(File file) throws IOException {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Create the "data/" folder
        }

        if (!file.exists()) {
            file.createNewFile(); // Create the file if it doesn't exist
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("1"); // Start from 1
            }
        }
    }

    public static synchronized int getNextSerial() {
        int next = currentSerial.getAndIncrement();
        saveSerial(currentSerial.get());
        return next;
    }
}
