import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        AuthService auth = new AuthService();

        System.out.println("=== Secure Bank App ===");
        System.out.println("1. Register\n2. Login");
        int option = readInt(sc, "Choose an option: ");

        String username = readRequired(sc, "Username: ");
        String password = readRequired(sc, "Password: ");

        boolean registering = option == 1;
        boolean authenticated;
        if (registering) {
            authenticated = auth.register(username, password);
        } else if (option == 2) {
            authenticated = auth.login(username, password);
        } else {
            System.out.println("Invalid option.");
            return;
        }

        if (!authenticated) {
            return;
        }

        if (registering && !createAccount(sc, username)) {
            return;
        }

        while (true) {
            System.out.println("\n1. View Balance\n2. Deposit\n3. Withdraw\n4. Exit");
            int choice = readInt(sc, "Choose an option: ");
            if (choice == 4) {
                System.out.println("Goodbye!");
                return;
            }
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid option.");
                continue;
            }

            String accountNumber = readRequired(sc, "Enter your account number: ");
            List<BankAccount> accounts = FileManager.loadAccounts();
            BankAccount account = findOwnedAccount(accounts, accountNumber, username);
            if (account == null) {
                System.out.println("Account not found or it does not belong to this user.");
                continue;
            }

            if (choice == 1) {
                System.out.println("Balance: $" + account.getBalance());
            } else if (choice == 2) {
                double amount = readDouble(sc, "Amount to deposit: ");
                if (!SecurityUtil.isValidAmount(amount)) {
                    System.out.println("Enter a valid positive amount.");
                    continue;
                }
                account.deposit(amount);
                FileManager.recordTransaction(accounts, account, "DEPOSIT", amount);
                System.out.println("Deposit successful. Balance: $" + account.getBalance());
            } else {
                double amount = readDouble(sc, "Amount to withdraw: ");
                if (!account.withdraw(amount)) {
                    System.out.println("Insufficient funds or invalid amount.");
                    continue;
                }
                FileManager.recordTransaction(accounts, account, "WITHDRAWAL", amount);
                System.out.println("Withdrawal successful. Balance: $" + account.getBalance());
            }
        }
    }

    private static boolean createAccount(Scanner sc, String username) throws IOException {
        List<BankAccount> accounts = FileManager.loadAccounts();
        String accountNumber;
        do {
            System.out.print("Enter an account number (or press Enter to generate one): ");
            accountNumber = sc.nextLine().trim();
            if (accountNumber.isEmpty()) {
                accountNumber = generateAccountNumber(accounts);
            }
            if (accountExists(accounts, accountNumber)) {
                System.out.println("That account number is already in use. Try another.");
            }
        } while (accountExists(accounts, accountNumber));

        accounts.add(new BankAccount(accountNumber, username, 0));
        FileManager.saveAccounts(accounts);
        System.out.println("Account created successfully: " + accountNumber);
        return true;
    }

    private static String generateAccountNumber(List<BankAccount> accounts) {
        String accountNumber;
        do {
            accountNumber = "ACC" + System.currentTimeMillis();
        } while (accountExists(accounts, accountNumber));
        return accountNumber;
    }

    private static boolean accountExists(List<BankAccount> accounts, String accountNumber) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return true;
            }
        }
        return false;
    }

    private static BankAccount findOwnedAccount(List<BankAccount> accounts, String accountNumber, String username) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)
                    && account.getOwner().equals(username)) {
                return account;
            }
        }
        return null;
    }

    private static String readRequired(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(input);
                if (!Double.isFinite(value)) {
                    throw new NumberFormatException();
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }
}
