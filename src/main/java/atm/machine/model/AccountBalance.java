package atm.machine.model;

public class AccountBalance {
    private long accountNumber;
    private double balance;
    private double overdraft;

    public AccountBalance(long accountNumber, double balance, double overdraft) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.overdraft = overdraft;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }


    public double getBalance() {
        return balance;
    }

    public double getOverdraft() {
        return overdraft;
    }

}
