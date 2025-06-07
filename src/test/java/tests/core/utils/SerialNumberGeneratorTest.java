package tests.core.utils;

import core.utils.SerialNumberGenerator;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SerialNumberGeneratorTest {

    private static final String TEST_FILE_PATH = "data/serial_number.txt";

    @BeforeEach
    void resetSerialNumberFile() throws Exception {
        Path filePath = Paths.get(TEST_FILE_PATH);

        // Ensure clean file state
        Files.deleteIfExists(filePath);
        Files.createDirectories(filePath.getParent());

        // Write default values: today's date and serial 1
        Files.write(filePath, List.of(
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                "1"
        ));

        // Reset the static currentSerial field using reflection
        Field serialField = SerialNumberGenerator.class.getDeclaredField("currentSerial");
        serialField.setAccessible(true);
        serialField.set(null, new AtomicInteger(1));
    }

    @Test
    public void testFirstSerialNumberIsOne() {
        int serial = SerialNumberGenerator.getNextSerial();
        assertEquals(1, serial, "First serial number should be 1");
    }

    @Test
    public void testSecondSerialNumberIsTwo() {
        SerialNumberGenerator.getNextSerial(); // First
        int second = SerialNumberGenerator.getNextSerial(); // Second
        assertEquals(2, second, "Second serial number should be 2");
    }

    @Test
    public void testSerialFileUpdatesCorrectly() throws IOException {
        SerialNumberGenerator.getNextSerial(); // Serial 1
        SerialNumberGenerator.getNextSerial(); // Serial 2

        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE_PATH));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ISO_DATE), lines.get(0), "Date should be today");
        assertEquals("3", lines.get(1), "Next serial saved should be 3");
    }
}
