package atm.machine.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class AccountAuthenticationDetails {
    @Id
    private long accountNumber;
    private int pin;

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getPin() {
        return pin;
    }

}
