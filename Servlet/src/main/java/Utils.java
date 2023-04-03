public class Utils {
    public static boolean isValidUserId(String[] urlParts) {
        if (urlParts.length != 2) {
            return false;
        }
        String idStr = urlParts[1];
        if (!idStr.matches("\\d+")) {
            return false;
        }
        int id = Integer.parseInt(idStr);
        return (id >= 1 && id <= 5000);
    }
}
