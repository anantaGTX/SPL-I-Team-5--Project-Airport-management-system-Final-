public class AppUser {
    private String fullName;
    private String username;
    private String email;
    private String passwordHash;
    private String role;

    public AppUser(String fullName, String username, String email, String passwordHash, String role) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String toFileString() {
        return fullName + "," + username + "," + email + "," + passwordHash + "," + role;
    }
}