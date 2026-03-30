import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {

    // =========================
    // Password Hashing (SHA-256)
    // =========================
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available.", e);
        }
    }

    // =========================
    // Password Strength Check
    // =========================
    public static boolean isStrongPassword(String password) {

        if (password == null) return false;

        if (password.length() < 8) return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {

            if (Character.isUpperCase(c)) {
                hasUpper = true;
            }
            else if (Character.isLowerCase(c)) {
                hasLower = true;
            }
            else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            else {
                hasSpecial = true;
            }

        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}