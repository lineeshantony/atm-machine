package atm.machine.exception;

public class InsufficientAccountBalanceException extends AtmMachineException {
    public InsufficientAccountBalanceException(String message) {
        super(message);
    }
}
