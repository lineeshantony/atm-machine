package atm.machine.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Account {
    private long accountNumber;
    private double balance;
    private double overdraft;

    @Id
    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }


    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getOverdraft() {
        return overdraft;
    }

    public void setOverdraft(double overdraft) {
        this.overdraft = overdraft;
    }

}
