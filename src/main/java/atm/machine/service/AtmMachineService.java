package atm.machine.service;

import atm.machine.exception.AtmMachineException;
import atm.machine.model.Account;
import atm.machine.model.AccountBalance;
import atm.machine.model.AccountTransaction;
import atm.machine.model.Cash;
import atm.machine.repository.AccountRepository;
import atm.machine.repository.AccountTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtmMachineService {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    public Cash withdraw(long accountNumber, int withdrawalAmount)
            throws AtmMachineException {
        return withdrawalService.withdraw(accountNumber, withdrawalAmount);
    }

    public AccountBalance getBalance(long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        return new AccountBalance(account.getAccountNumber(), account.getBalance(), account.getOverdraft());
    }

    public List<AccountTransaction> getStatement(long accountNumber) {
        return accountTransactionRepository.findAllByAccountNumberOrderByTransactionTimeDesc(accountNumber);
    }

}
