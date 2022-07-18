package atm.machine.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long accountNumber;
    private TransactionType transactionType;
    private double amount;
    private double balance;
    private double overdraft;
    private Timestamp transactionTime;

    public AccountTransaction() {
    }

    public AccountTransaction(long accountNumber, TransactionType transactionType, double amount, double balance, double overdraft, Timestamp transactionTime) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;
        this.overdraft = overdraft;
        this.transactionTime = transactionTime;
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

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Timestamp getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Timestamp transactionTime) {
        this.transactionTime = transactionTime;
    }

    public double getOverdraft() {
        return overdraft;
    }

    public long getId() {
        return id;
    }
}
