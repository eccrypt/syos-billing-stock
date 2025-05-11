package core.utils;

public class DateUtil {
    public static String getCurrentDate() {
        return java.time.LocalDate.now().toString();
    }
}