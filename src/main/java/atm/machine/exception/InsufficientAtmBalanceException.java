package atm.machine.exception;

public class InsufficientAtmBalanceException extends AtmMachineException {
    public InsufficientAtmBalanceException(String message) {
        super(message);
    }
}
