package atm.machine.exception;

public class AuthenticationFailureException extends AtmMachineException {
    public AuthenticationFailureException(String message) {
        super(message);
    }
}
