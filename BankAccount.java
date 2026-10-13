public class BankAccount {
    private final String accountNumber;
    private final String owner;
    private double balance;

    public BankAccount(String accountNumber, String owner, double balance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
    }
    public void deposit(double amt) {
        if(SecurityUtil.isValidAmount(amt)) balance += amt;
    }
    public boolean withdraw(double amt) {
        if(SecurityUtil.isValidAmount(amt) && balance >= amt) {
            balance -= amt; return true;
        }
        return false;
    }
    public double getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    // For saving to file
    public String toFileString() { return accountNumber+","+owner+","+balance; }
}
