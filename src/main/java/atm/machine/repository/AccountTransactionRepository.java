package atm.machine.repository;

import atm.machine.model.AccountTransaction;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountTransactionRepository extends CrudRepository<AccountTransaction, Long> {
    List<AccountTransaction> findAllByAccountNumberOrderByTransactionTimeDesc(long accountNumber);
}
