import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static final Path USER_FILE = Paths.get("data", "users.txt");
    private static final Path LEGACY_USER_FILE = Paths.get("users.txt");

    private final List<User> users = new ArrayList<>();
    private final List<String> activityRecords = new ArrayList<>();

    public AuthService() throws IOException {
        loadUsers();
    }

    public boolean register(String username, String password) throws IOException {
        if (username == null || username.trim().length() < 3
                || username.contains(",") || username.contains("\n") || username.contains("\r")) {
            System.out.println("Username must be >=3 chars and cannot contain commas or line breaks.");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Password must be >=6 chars");
            return false;
        }
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                System.out.println("User already exists!");
                return false;
            }
        }

        User newUser = new User(username, SecurityUtil.hashPassword(password));
        users.add(newUser);
        activityRecords.add(activityRecord("REGISTER", username));
        saveUsers();
        System.out.println("Registration successful!");
        return true;
    }

    public boolean login(String username, String password) throws IOException {
        if (username == null || password == null) {
            System.out.println("Invalid username or password!");
            return false;
        }
        String hashedInput = SecurityUtil.hashPassword(password);
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.getHashedPassword().equals(hashedInput)) {
                activityRecords.add(activityRecord("LOGIN", username));
                saveUsers();
                System.out.println("Login successful. Welcome " + username);
                return true;
            }
        }
        System.out.println("Invalid username or password!");
        return false;
    }

    private void loadUsers() throws IOException {
        if ((!Files.exists(USER_FILE) || Files.size(USER_FILE) == 0)
                && Files.exists(LEGACY_USER_FILE) && Files.size(LEGACY_USER_FILE) > 0) {
            readUserFile(LEGACY_USER_FILE);
            saveUsers();
            return;
        }
        readUserFile(USER_FILE);
    }

    private void readUserFile(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }

        List<String> lines = Files.readAllLines(file);
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String[] parts = line.split(",", -1);
            if (parts.length == 2) {
                users.add(new User(parts[0], parts[1]));
            } else if (parts.length == 3 && "USER".equals(parts[0])) {
                users.add(new User(parts[1], parts[2]));
            } else if (parts.length == 3
                    && ("REGISTER".equals(parts[0]) || "LOGIN".equals(parts[0]))) {
                try {
                    Instant.parse(parts[1]);
                } catch (RuntimeException e) {
                    throw new IOException("Invalid user activity record on line " + lineNumber + ".", e);
                }
                activityRecords.add(line);
            } else {
                throw new IOException("Invalid user data on line " + lineNumber + ".");
            }
        }
    }

    private void saveUsers() throws IOException {
        Path parent = USER_FILE.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "users-", ".tmp");
        try {
            List<String> lines = new ArrayList<>();
            for (User user : users) {
                lines.add("USER," + user.getUsername() + "," + user.getHashedPassword());
            }
            lines.addAll(activityRecords);
            Files.write(temporaryFile, lines);

            try {
                Files.move(temporaryFile, USER_FILE,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, USER_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static String activityRecord(String action, String username) {
        return action + "," + Instant.now() + "," + username;
    }
}
