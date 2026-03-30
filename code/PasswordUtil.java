import java.io.Console;
import java.util.Scanner;

public class PasswordUtil {

    public static String readPassword(Scanner sc, String prompt) {

        // Proper spacing and formatting
        System.out.println();
        System.out.print(prompt + " : ");

        Console console = System.console();

        // If console supports hidden password
        if (console != null) {
            char[] chars = console.readPassword();
            return chars == null ? "" : new String(chars);
        }

        // Fallback (IDE like IntelliJ / VSCode)
        return sc.nextLine();
    }
}