package atm.machine.repository;

import atm.machine.model.AccountAuthenticationDetails;
import org.springframework.data.repository.CrudRepository;

public interface AccountAuthenticationDetailsRepository extends CrudRepository<AccountAuthenticationDetails, Long> {
    AccountAuthenticationDetails findByAccountNumber(long accountNumber);
}
