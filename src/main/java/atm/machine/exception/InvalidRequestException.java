package atm.machine.exception;

public class InvalidRequestException extends AtmMachineException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
