import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AuthManager {
    private static final String USER_FILE = "user_accounts.txt";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private List<AppUser> users;

    public AuthManager() {
        users = new ArrayList<>();
        ensureFileExists();
        loadUsersFromFile();
        seedDefaultAdmin();
    }

    private void ensureFileExists() {
        try {
            File file = new File(USER_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("Error creating user accounts file: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        users.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 5) {
                    continue;
                }

                users.add(new AppUser(
                        parts[0].trim(), // fullName
                        parts[1].trim().toLowerCase(), // username
                        parts[2].trim().toLowerCase(), // email
                        parts[3].trim(), // passwordHash
                        parts[4].trim().toUpperCase() // role
                ));
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (AppUser user : users) {
                bw.write(user.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private void seedDefaultAdmin() {
        AppUser admin = findUserByUsername("admin");
        if (admin == null) {
            users.add(new AppUser(
                    "System Administrator",
                    "admin",
                    "admin@system.com",
                    SecurityUtil.hashPassword("Admin@123"),
                    "ADMIN"
            ));
            saveUsersToFile();
        }
    }

    private AppUser findUserByEmail(String email) {
        if (email == null) return null;

        String normalizedEmail = email.trim().toLowerCase();

        for (AppUser user : users) {
            if (user.getEmail().equalsIgnoreCase(normalizedEmail)) {
                return user;
            }
        }
        return null;
    }

    private AppUser findUserByUsername(String username) {
        if (username == null) return null;

        String normalizedUsername = username.trim().toLowerCase();

        for (AppUser user : users) {
            if (user.getUsername().equalsIgnoreCase(normalizedUsername)) {
                return user;
            }
        }
        return null;
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;

        email = email.trim().toLowerCase();

        if (email.isEmpty()) return false;
        if (email.contains(" ")) return false;
        if (email.contains("..")) return false;
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;

        String[] parts = email.split("@");
        if (parts.length != 2) return false;

        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.isEmpty()) return false;

        return domain.equals("gmail.com")
                || domain.equals("yahoo.com")
                || domain.equals("outlook.com");
    }

    public boolean isValidUsername(String username) {
        if (username == null) return false;

        username = username.trim().toLowerCase();

        if (username.isEmpty()) return false;
        if (username.length() < 4) return false;
        if (username.contains(" ")) return false;

        for (char ch : username.toCharArray()) {
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '.')) {
                return false;
            }
        }

        return true;
    }

    public boolean passengerSignup(String fullName, String username, String email, String password) {

        if (fullName == null || fullName.trim().isEmpty()) {
            System.out.println("Full name cannot be empty.");
            return false;
        }

        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if (!isValidUsername(normalizedUsername)) {
            System.out.println("Invalid username. Use at least 4 characters with letters, numbers, _ or .");
            return false;
        }

        if (findUserByUsername(normalizedUsername) != null) {
            System.out.println("This username is already taken.");
            return false;
        }

        if (!isValidEmail(normalizedEmail)) {
            System.out.println("Invalid email format. Only gmail.com, yahoo.com, or outlook.com allowed.");
            return false;
        }

        if (findUserByEmail(normalizedEmail) != null) {
            System.out.println("An account with this email already exists.");
            return false;
        }

        if (!SecurityUtil.isStrongPassword(password)) {
            System.out.println("Weak password. Use 8+ chars with uppercase, lowercase, number, and special character.");
            return false;
        }

        users.add(new AppUser(
                fullName.trim(),
                normalizedUsername,
                normalizedEmail,
                SecurityUtil.hashPassword(password),
                "PASSENGER"
        ));

        saveUsersToFile();
        return true;
    }

    public AppUser authenticate(String loginId, String password, String expectedRole) {
        if (loginId == null || loginId.trim().isEmpty()) {
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        String normalizedLoginId = loginId.trim().toLowerCase();

        AppUser user;

        if (expectedRole.equalsIgnoreCase("PASSENGER")) {
            user = findUserByUsername(normalizedLoginId);
        } else {
            user = findUserByUsername(normalizedLoginId);
        }

        if (user == null) {
            return null;
        }

        if (!user.getRole().equalsIgnoreCase(expectedRole)) {
            return null;
        }

        if (!user.getPasswordHash().equals(SecurityUtil.hashPassword(password))) {
            return null;
        }

        return user;
    }

    public AppUser findPassengerByUsername(String username) {
        AppUser user = findUserByUsername(username);
        if (user != null && user.getRole().equalsIgnoreCase("PASSENGER")) {
            return user;
        }
        return null;
    }
    public AppUser findPassengerByEmail(String email) {
        AppUser user = findUserByEmail(email);
        if (user != null && user.getRole().equalsIgnoreCase("PASSENGER")) {
            return user;
        }
        return null;
    }

    public AppUser findAdmin() {
        return findUserByUsername("admin");
    }

    public void displayAllUsers() {
        if (users.isEmpty()) {
            System.out.println("No users available.");
            return;
        }

        System.out.println("fullName|username|email|role");
        for (AppUser user : users) {
            System.out.println(
                    user.getFullName() + "|" +
                            user.getUsername() + "|" +
                            user.getEmail() + "|" +
                            user.getRole()
            );
        }
    }
}