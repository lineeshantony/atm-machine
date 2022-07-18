package atm.machine.controller;

import atm.machine.exception.AtmMachineException;
import atm.machine.exception.AuthenticationFailureException;
import atm.machine.model.CardDetails;
import atm.machine.model.Cash;
import atm.machine.model.TransactionDetails;
import atm.machine.service.AtmMachineService;
import atm.machine.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atm-machine")
public class AtmMachineController {
    private static final Logger LOG = LoggerFactory.getLogger(AtmMachineController.class);

    @Autowired
    private AtmMachineService atmMachineService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/generateToken")
    public ResponseEntity<String> generateToken(@RequestBody CardDetails cardDetails) {
        LOG.info("Token generation begins for account " + cardDetails.getAccountNumber());
        try {
            return new ResponseEntity<>(authenticationService.generateToken(cardDetails), HttpStatus.OK);
        } catch (AuthenticationFailureException e) {
            LOG.error("Token generation failed for account " + cardDetails.getAccountNumber(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity withdraw(@RequestBody TransactionDetails transactionDetails) {
        LOG.info("Withdrawal begins for account " + transactionDetails.getAccountNumber());
        if (!authenticationService.verifyToken(transactionDetails.getAccountNumber(), transactionDetails.getToken())) {
            LOG.info("Withdrawal failed for account " + transactionDetails.getAccountNumber() + ". Authentication failed.");
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
        try {
            Cash cash = atmMachineService.withdraw(transactionDetails.getAccountNumber(), transactionDetails.getAmount());
            return new ResponseEntity<>(cash, HttpStatus.OK);
        } catch (AtmMachineException e) {
            LOG.error("Withdrawal failed for account " + transactionDetails.getAccountNumber(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.PRECONDITION_FAILED);
        }
    }

    @PostMapping("/getAccountBalance")
    public ResponseEntity getAccountBalance(@RequestBody TransactionDetails transactionDetails) {
        LOG.info("Getting balance for account " + transactionDetails.getAccountNumber());
        if (!authenticationService.verifyToken(transactionDetails.getAccountNumber(), transactionDetails.getToken())) {
            LOG.info("Balance retrieval failed for account " + transactionDetails.getAccountNumber() + ". Authentication failed.");
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(atmMachineService.getBalance(transactionDetails.getAccountNumber()), HttpStatus.OK);
    }

    @PostMapping("/getStatement")
    public ResponseEntity getStatement(@RequestBody TransactionDetails transactionDetails) {
        LOG.info("Getting statement for account " + transactionDetails.getAccountNumber());
        if (!authenticationService.verifyToken(transactionDetails.getAccountNumber(), transactionDetails.getToken())) {
            LOG.info("Statement retrieval failed for account " + transactionDetails.getAccountNumber() + ". Authentication failed.");
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(atmMachineService.getStatement(transactionDetails.getAccountNumber()), HttpStatus.OK);
    }
}
