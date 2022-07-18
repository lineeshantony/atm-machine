package atm.machine.model;

import java.util.Map;

public class Cash {

    private Map<BankNote, Integer> bankNotes;
    private double remainingBalance;
    private double remainingOverdraft;

    public Cash(Map<BankNote, Integer> bankNotes, double remainingBalance, double remainingOverdraft) {
        this.bankNotes = bankNotes;
        this.remainingBalance = remainingBalance;
        this.remainingOverdraft = remainingOverdraft;
    }

    public Map<BankNote, Integer> getBankNotes() {
        return bankNotes;
    }

    public double getRemainingBalance() {
        return remainingBalance;
    }

    public double getRemainingOverdraft() {
        return remainingOverdraft;
    }

}
