# ATM Machine

### Developer Notes

- Below is a flow diagram (sort of):

```
    1. Generate Token flow:
    
    AtmMachineController <-- generateToken() --> AuthenticationService
        |                                                    |   
        ^-------- AuthenticationFailureException <------------ 
    
    
    2. Withdraw flow:  
    
            verify() <------> AuthenticationService
              |
    AtmMachineController <------> AtmMachineService <---> WithdrawalService <-- retrieve and update --> AccountRepository;
        |                                                   |       |
        ^------- InsufficientAccountBalanceException <-------       <----- retrieve and update ------> AccountTransactionRepository
        |                                                   |       |
        ^-------- InsufficientAtmBalanceException <----------       ------- update -------> AtmCashRepository
        |                                                   |
        ^-----------  InvalidRequestException   <------------
        |                                                   |
        ^----------  NotesUnavailableException   <-----------
        
        
     3. Get Balance Flow   
             verify() <------> AuthenticationService
              |
     AtmMachineController <-- getBalance() --> AtmMachineService <--- retrieve --- AccountRepository
     
     
     4. Get Statement 
             verify() <------> AuthenticationService
              |
     AtmMachineController <-- getStatement() --> AtmMachineService <--- retrieve --- AccountTransactionRepository
                                      
```

## Running the application

### Maven build and run
To start the machine locally you need to run the following commands in the local source repo
 - mvn clean install
 - mvn spring-boot:run

The application should be up in 8080 port.

### Docker build and run
If you want to run it using Docker (in Windows)
 - Start docker desktop
 - Docker build (execute from the folder where the dockerfile is written
   >docker build -f Dockerfile -t lineesh/atm-machine .
   
    This command builds an image and tags it as lineesh/atm-machine
  - Docker run:
    >docker run -p 9080:8080 lineesh/atm-machine

  - Application can be accessed locally using http://localhost:9080

## Testing atm-machine
Testing can be done through 2 ways

###1. Through Postman

####Generating Token
 - I have built a token based authentication system for atm-machine.
   One needs to authenticate and generate a token before accessing other functionalities. 
 - To generate token use below:
    - Method: POST
    - URL: http://localhost:8080/atm-machine/generateToken
    - Body: JSON containing accountNumber and pin
      E.g.:
      >{   "accountNumber": 123456789, "pin": 1234   }
  - Then a response token is returned e.g.:  d2864941-f9e5-4697-b343-d0d93f44962a
  - Use this token to perform a single operation (withdraw or getBalance or getStatement). 
  - Please note that once the operation is performed the token will expire just like a real ATM.

####Withdraw
 - Once you have the token from generateToken above go to postman
   - Method: POST
   - URL: http://localhost:8080/atm-machine/withdraw
   - Body: JSON containing token, accountNumber, amount
     E.g.:
     >{   "accountNumber": 123456789, "amount": 100, "token": "d2864941-f9e5-4697-b343-d0d93f44962a"   }
 - If everthing goes well will get the notes dispensed, remaining balance and remaining overdraft as a response
 - Things that could go wrong and handled are 
   - Insufficient account balance
   - Insufficient ATM balance
   - notes unavailable for the multiples of the selected amount
   - Invalid requests like 0 or negative amount withdrawal, though negative may not be allowed at the keypad itself
   - Authentication failure

####Get Balance
- Once you have the token from generateToken above go to postman
  - Method: POST
  - URL: http://localhost:8080/atm-machine/getBalance
  - Body: JSON containing token, accountNumber
    E.g.:
    >{   "accountNumber": 123456789, "token": "d2864941-f9e5-4697-b343-d0d93f44962a"   }
- If everthing goes well will get the balance displayed as a response 
- Authentication failure could happen if token is incorrect or expired

####Get Statement
- Once you have the token from generateToken above go to postman
  - Method: POST
  - URL: http://localhost:8080/atm-machine/getStatement
  - Body: JSON containing token, accountNumber
    E.g.:
    >{   "accountNumber": 123456789, "token": "d2864941-f9e5-4697-b343-d0d93f44962a"   }
- If everthing goes well will get the transactions performed as a response
- Authentication failure could happen if token is incorrect or expired
  
###2. Through testcases

This is very easy. One can add any kind of custom testcase in AtmMachineControllerTest.
Should be easy to follow from the many test cases already written. The test cases cover majority of the scenarios. The coverage is above 90%.
The testcases use the spring-boot yaml configuration same as the application startup and also generates token and verifies it before executing any functionality request just like the real world application.

##Enabling Metrices collection endpoints
Add the following property in yaml (currently commented)
>management.endpoints.web.exposure.include: "*"

Can also add specific endpoints as well instead of exposing all.
