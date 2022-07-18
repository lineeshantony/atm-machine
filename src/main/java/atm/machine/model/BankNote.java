package atm.machine.model;

public enum BankNote implements Comparable<BankNote> {

    FIFTY(50),
    TWENTY(20),
    TEN(10),
    FIVE(5);

    private final int value;

    BankNote(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BankNote bankNoteOf(int value) {
        for (BankNote bankNote : values()) {
            if (bankNote.value == value) {
                return bankNote;
            }
        }
        throw new IllegalArgumentException(value + "");
    }
}
