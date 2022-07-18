package atm.machine.repository;

import atm.machine.model.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Account findByAccountNumber(long accountNumber);
}
