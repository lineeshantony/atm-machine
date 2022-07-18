package atm.machine.controller;

import atm.machine.model.CardDetails;
import atm.machine.model.TransactionDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AtmMachineControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testTokenGeneration() throws Exception {
        CardDetails cardDetails = new CardDetails();
        cardDetails.setAccountNumber(123456789);
        cardDetails.setPin(1234);
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/generateToken")
                .content(asJsonString(cardDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());
    }

    @Test
    public void testTokenGenerationWithIncorrectPin() throws Exception {
        CardDetails cardDetails = new CardDetails();
        cardDetails.setAccountNumber(123456789);
        cardDetails.setPin(1235);
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/generateToken")
                .content(asJsonString(cardDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTokenGenerationWithIncorrectAccountNumber() throws Exception {
        CardDetails cardDetails = new CardDetails();
        cardDetails.setAccountNumber(123456788);
        cardDetails.setPin(1234);
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/generateToken")
                .content(asJsonString(cardDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testWithdrawalForMultipleOf50() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(100);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"bankNotes\":{\"FIFTY\":2},\"remainingBalance\":700.0,\"remainingOverdraft\":200.0}")));
    }

    @Test
    public void testWithdrawalForMultipleOf20() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(320);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":6")))
                .andExpect(content().string(containsString("\"TWENTY\":1")));
    }

    @Test
    public void testWithdrawalForMultipleOf10() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(210);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":4")))
                .andExpect(content().string(containsString("\"TEN\":1")));
    }

    @Test
    public void testWithdrawalForMultipleOf5() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(215);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":4")))
                .andExpect(content().string(containsString("\"TEN\":1")))
                .andExpect(content().string(containsString("\"FIVE\":1")));
    }

    @Test
    public void testWithdrawalMultipleTimesAndSmallerDenominationNotesGetUsed() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(415);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":8")))
                .andExpect(content().string(containsString("\"TEN\":1")))
                .andExpect(content().string(containsString("\"FIVE\":1")));

        transactionDetails.setAmount(415);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":2")))
                .andExpect(content().string(containsString("\"TWENTY\":15")))
                .andExpect(content().string(containsString("\"TEN\":1")))
                .andExpect(content().string(containsString("\"FIVE\":1")));
    }

    @Test
    public void testWithdrawalForInsufficientAccountBalance() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(2000);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Insufficient Account Balance")));
    }

    @Test
    public void testWithdrawalForInsufficientAtmBalance() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(1000);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"FIFTY\":10")))
                .andExpect(content().string(containsString("\"TWENTY\":25")));

        transactionDetails.setAccountNumber(987654321);
        transactionDetails.setAmount(600);
        transactionDetails.setToken(getAuthenticationToken(987654321, 4321));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Insufficient ATM Balance")));
    }

    @Test
    public void testWithdrawalFor0Amount() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(0);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Invalid Request")));
    }

    @Test
    public void testWithdrawalForAmountNotAMultipleOfBankNote() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setAmount(177);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Please withdraw cash in the multiples of 50, 20, 10, 5")));
    }

    @Test
    public void testWithdrawalMultipleOf5IsRequestedButBankNoteIsNotAvailable() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        for (int i = 0; i < 20; i++) {
            transactionDetails.setAmount(5);
            transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
            mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                    .content(asJsonString(transactionDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"FIVE\":1")));
        }
        transactionDetails.setAmount(5);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Please withdraw cash in the multiples of 50, 20, 10")));
    }

    @Test
    public void testWithdrawalMultipleOf10IsRequestedButBankNoteIsNotAvailable() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        for (int i = 0; i < 20; i++) {
            transactionDetails.setAmount(15);
            transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
            mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                    .content(asJsonString(transactionDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"FIVE\":1")))
                    .andExpect(content().string(containsString("\"TEN\":1")));
        }

        for (int i = 0; i < 10; i++) {
            transactionDetails.setAmount(10);
            transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
            mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                    .content(asJsonString(transactionDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("\"TEN\":1")));
        }
        transactionDetails.setAmount(10);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(content().string(containsString("Please withdraw cash in the multiples of 50, 20")));
    }

    @Test
    public void testGetAccountBalanceWhenNoTokenUsed() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/getAccountBalance")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Authentication failed")));
    }

    @Test
    public void testGetAccountBalanceWithCorrectToken() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/getAccountBalance")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("{\"accountNumber\":123456789,\"balance\":800.0,\"overdraft\":200.0}")));
    }

    @Test
    public void testGetStatementWithCorrectToken() throws Exception {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAccountNumber(123456789);

        transactionDetails.setAmount(215);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        transactionDetails.setAmount(430);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        transactionDetails.setAmount(350);
        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/withdraw")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        transactionDetails.setToken(getAuthenticationToken(123456789, 1234));
        mvc.perform(MockMvcRequestBuilders.post("/atm-machine/getStatement")
                .content(asJsonString(transactionDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"id\":3,\"accountNumber\":123456789,\"balance\":0.0,\"overdraft\":5.0")))
                .andExpect(content().string(containsString("\"id\":2,\"accountNumber\":123456789,\"balance\":155.0,\"overdraft\":200.0")))
                .andExpect(content().string(containsString("\"id\":1,\"accountNumber\":123456789,\"balance\":585.0,\"overdraft\":200.0")));

    }

    private String getAuthenticationToken(long accountNumber, int pin) throws Exception {
        CardDetails cardDetails = new CardDetails();
        cardDetails.setAccountNumber(accountNumber);
        cardDetails.setPin(pin);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/atm-machine/generateToken")
                .content(asJsonString(cardDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        return mvcResult.getResponse().getContentAsString();
    }

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}