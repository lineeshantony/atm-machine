package atm.machine.repository;

import atm.machine.model.BankNoteCount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AtmCashRepository extends CrudRepository<BankNoteCount, Integer> {
    List<BankNoteCount> findAll();
}
