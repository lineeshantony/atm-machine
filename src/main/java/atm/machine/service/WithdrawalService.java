package atm.machine.service;

import atm.machine.exception.*;
import atm.machine.model.*;
import atm.machine.repository.AccountRepository;
import atm.machine.repository.AccountTransactionRepository;
import atm.machine.repository.AtmCashRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WithdrawalService {

    private static final Logger LOG = LoggerFactory.getLogger(WithdrawalService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private AtmCashRepository atmCashRepository;

    @Transactional
    public Cash withdraw(long accountNumber, int withdrawalAmount)
            throws AtmMachineException {
        LOG.info("Withdrawal amount: " + withdrawalAmount);
        Account account = accountRepository.findByAccountNumber(accountNumber);
        double totalWithdrawableAmount = account.getBalance() + account.getOverdraft();
        LOG.info("Total withdrawable amount: " + totalWithdrawableAmount);
        List<BankNoteCount> bankNotes = atmCashRepository.findAll();
        validateWithdrawalRequest(withdrawalAmount, totalWithdrawableAmount, bankNotes);
        Map<BankNote, Integer> bankNoteCountMap = bankNotes.stream()
                .collect(Collectors.toMap(BankNoteCount::getBankNote, BankNoteCount::getCount));

        Map<BankNote, Integer> cashBankNoteCountMap = computeAtmWithdrawal(withdrawalAmount, bankNoteCountMap);
        updateBankNoteCount(bankNoteCountMap);
        updateAccount(withdrawalAmount, account);
        updateAccountTransaction(withdrawalAmount, account);
        return new Cash(cashBankNoteCountMap, account.getBalance(), account.getOverdraft());
    }

    private void updateAccountTransaction(int withdrawalAmount, Account account) {
        LOG.info("Updating AccountTransaction");
        accountTransactionRepository.save(new AccountTransaction(account.getAccountNumber(),
                TransactionType.DEBIT, withdrawalAmount, account.getBalance(), account.getOverdraft(),
                new Timestamp(new java.util.Date().getTime())));
    }

    private void validateWithdrawalRequest(int withdrawalAmount, double totalWithdrawableAmount, List<BankNoteCount> bankNotes) throws AtmMachineException {
        if (withdrawalAmount <= 0) {
            LOG.info("Withdrawal amount less than or equal to 0");
            throw new InvalidRequestException("Invalid Request");
        }
        if (withdrawalAmount > totalWithdrawableAmount) {
            LOG.info("Withdrawal amount less than total withdrawable amount");
            throw new InsufficientAccountBalanceException("Insufficient Account Balance");
        }
        if (getAmount(bankNotes) < withdrawalAmount) {
            LOG.info("Withdrawal amount more than ATM balance");
            throw new InsufficientAtmBalanceException("Insufficient ATM Balance");
        }
    }

    private void updateBankNoteCount(Map<BankNote, Integer> bankNoteCountMap) {
        LOG.info("Updating BankNoteCount");
        for (Map.Entry<BankNote, Integer> bankNoteCountEntry : bankNoteCountMap.entrySet()) {
            BankNote bankNote = bankNoteCountEntry.getKey();
            atmCashRepository.save(new BankNoteCount(bankNote.getValue(), bankNoteCountEntry.getValue()));
        }
    }

    private void updateAccount(int withdrawalAmount, Account account) {
        LOG.info("Updating Account");
        if (withdrawalAmount < account.getBalance()) {
            account.setBalance(account.getBalance() - withdrawalAmount);
        } else {
            account.setOverdraft(account.getBalance() + account.getOverdraft() - withdrawalAmount);
            account.setBalance(0);
        }
        accountRepository.save(account);
    }

    private Map<BankNote, Integer> computeAtmWithdrawal(int withdrawalAmount, Map<BankNote, Integer> bankNoteCountMap) throws NotesUnavailableException {
        LOG.info("Computing atm cash withdrawal");
        Map<BankNote, Integer> cashBankNoteCountMap = new HashMap<>();
        for (BankNote bankNote : BankNote.values()) {
            int count = bankNoteCountMap.get(bankNote);
            if (count == 0) {
                continue;
            }
            int noteCount = withdrawalAmount / bankNote.getValue();
            if (noteCount == 0) {
                continue;
            }
            if (noteCount > count) {
                bankNoteCountMap.put(bankNote, 0);
                cashBankNoteCountMap.put(bankNote, count);
                withdrawalAmount = withdrawalAmount - count * bankNote.getValue();
            } else {
                bankNoteCountMap.put(bankNote, count - noteCount);
                cashBankNoteCountMap.put(bankNote, noteCount);
                withdrawalAmount = withdrawalAmount - noteCount * bankNote.getValue();
            }
            if (withdrawalAmount == 0) {
                break;
            }
        }
        if (withdrawalAmount != 0) {
            throw new NotesUnavailableException(getNotesMultipleMessage(bankNoteCountMap));
        }
        return cashBankNoteCountMap;
    }

    private String getNotesMultipleMessage(Map<BankNote, Integer> bankNoteCountMap) {
        List<BankNote> remainingBankNotes = Arrays.stream(BankNote.values())
                .filter(bankNote -> bankNoteCountMap.get(bankNote) > 0)
                .collect(Collectors.toList());

        String message = "Please withdraw cash in the multiples of ";
        for (BankNote bankNote : remainingBankNotes) {
            message += bankNote.getValue() + ", ";
        }
        return message.substring(0, message.length() - 2);
    }

    public Double getAmount(List<BankNoteCount> bankNotes) {
        return bankNotes.stream().mapToDouble(BankNoteCount::getTotal).sum();
    }
}
