package atm.machine.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BankNoteCount {

    @Id
    private int value;
    private int count;

    public BankNoteCount() {
    }

    public BankNoteCount(int value, int count) {
        this.value = value;
        this.count = count;
    }

    public BankNote getBankNote() {
        return BankNote.bankNoteOf(value);
    }

    public int getCount() {
        return count;
    }

    public int getTotal() {
        return value * count;
    }

    public int getValue() {
        return value;
    }

}
