package atm.machine.service;

import atm.machine.exception.AuthenticationFailureException;
import atm.machine.model.AccountAuthenticationDetails;
import atm.machine.model.CardDetails;
import atm.machine.repository.AccountAuthenticationDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AccountAuthenticationDetailsRepository authenticationDetailsRepository;

    @Value("${authenticationDisabled}")
    private boolean authenticationDisabled;

    private Map<Long, String> accountTokenMap = new HashMap<>();

    public String generateToken(CardDetails cardDetails) throws AuthenticationFailureException {
        AccountAuthenticationDetails accountAuthenticationDetails = authenticationDetailsRepository.findByAccountNumber(cardDetails.getAccountNumber());
        if (accountAuthenticationDetails == null || accountAuthenticationDetails.getPin() != cardDetails.getPin()) {
            throw new AuthenticationFailureException("Authentication Failed");
        }
        String token = UUID.randomUUID().toString();
        accountTokenMap.put(cardDetails.getAccountNumber(), token);
        LOG.info("Token generated");
        return token;
    }

    public boolean verifyToken(long accountNumber, String token) {
        if (authenticationDisabled) {
            return true;
        }
        String existingToken = accountTokenMap.get(accountNumber);

        if (existingToken != null && existingToken.equals(token)) {
            LOG.info("Token is valid");
            accountTokenMap.remove(accountNumber);
            return true;
        }
        return false;
    }

}
