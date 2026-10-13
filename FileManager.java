import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
2import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final Path ACCOUNT_FILE = Paths.get("data", "accounts.txt");
    private static final Path LEGACY_ACCOUNT_FILE = Paths.get("accounts.txt");

    public static List<BankAccount> loadAccounts() throws IOException {
        if ((!Files.exists(ACCOUNT_FILE) || Files.size(ACCOUNT_FILE) == 0)
                && Files.exists(LEGACY_ACCOUNT_FILE) && Files.size(LEGACY_ACCOUNT_FILE) > 0) {
            List<BankAccount> legacyAccounts = readAccounts(LEGACY_ACCOUNT_FILE);
            List<String> legacyTransactions = readTransactionLines(LEGACY_ACCOUNT_FILE);
            writeData(legacyAccounts, legacyTransactions);
        }
        return readAccounts(ACCOUNT_FILE);
    }

    public static void saveAccounts(List<BankAccount> accounts) throws IOException {
        writeData(accounts, readTransactionLines(ACCOUNT_FILE));
    }

    public static void recordTransaction(List<BankAccount> accounts, BankAccount account,
            String type, double amount) throws IOException {
        List<String> transactions = readTransactionLines(ACCOUNT_FILE);
        transactions.add(String.join(",",
                "TRANSACTION",
                Instant.now().toString(),
                account.getAccountNumber(),
                account.getOwner(),
                type,
                Double.toString(amount),
                Double.toString(account.getBalance())));
        writeData(accounts, transactions);
    }

    private static List<BankAccount> readAccounts(Path file) throws IOException {
        List<BankAccount> accounts = new ArrayList<>();
        if (!Files.exists(file)) {
            return accounts;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] parts = line.split(",", -1);
                if (parts.length == 7 && "TRANSACTION".equals(parts[0])) {
                    validateTransaction(parts, lineNumber);
                    continue;
                }
                if (parts.length != 3) {
                    throw new IOException("Invalid account data on line " + lineNumber + ".");
                }
                try {
                    accounts.add(new BankAccount(parts[0], parts[1], Double.parseDouble(parts[2])));
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid balance on account data line " + lineNumber + ".", e);
                }
            }
        }
        return accounts;
    }

    private static void validateTransaction(String[] parts, int lineNumber) throws IOException {
        if (!"DEPOSIT".equals(parts[4]) && !"WITHDRAWAL".equals(parts[4])) {
            throw new IOException("Invalid transaction type on line " + lineNumber + ".");
        }
        try {
            Instant.parse(parts[1]);
            Double.parseDouble(parts[5]);
            Double.parseDouble(parts[6]);
        } catch (RuntimeException e) {
            throw new IOException("Invalid transaction data on line " + lineNumber + ".", e);
        }
    }

    private static List<String> readTransactionLines(Path file) throws IOException {
        List<String> transactions = new ArrayList<>();
        if (!Files.exists(file)) {
            return transactions;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TRANSACTION,")) {
                    transactions.add(line);
                }
            }
        }
        return transactions;
    }

    private static void writeData(List<BankAccount> accounts, List<String> transactions) throws IOException {
        Path parent = ACCOUNT_FILE.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "accounts-", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile)) {
                for (BankAccount account : accounts) {
                    writer.write(account.toFileString());
                    writer.newLine();
                }
                for (String transaction : transactions) {
                    writer.write(transaction);
                    writer.newLine();
                }
            }

            try {
                Files.move(temporaryFile, ACCOUNT_FILE,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, ACCOUNT_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
