package com.tnt.fincore.service.impl;

import com.tnt.fincore.entity.CustomerData;
import com.tnt.fincore.entity.CustomerTransaction;
import com.tnt.fincore.exception.InvalidCredentialsException;
import com.tnt.fincore.exception.InsufficientBalanceException;
import com.tnt.fincore.exception.ResourceNotFoundException;
import com.tnt.fincore.exception.ValidationException;
import com.tnt.fincore.repository.CustomerDataRepository;
import com.tnt.fincore.repository.CustomerTransactionRepository;
import com.tnt.fincore.service.PaymentService;
import com.tnt.fincore.utility.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private CustomerDataRepository customerDataRepository;
    
    @Autowired
    private CustomerTransactionRepository customerTransactionRepository;

    @Override
    @Transactional
    public Map<Object, Object> doDebit(Map<Object, Object> data) {
        Map<Object, Object> response = new HashMap<>();
        logger.info("Processing debit transaction");
        
        try {
            validateDebitRequest(data);
            
            String upiId = data.get("upiId").toString().trim();
            String mpin = data.get("mpin").toString();
            logger.info("mpin of user from data:{}",mpin);
            logger.info("mpin of user from here:{}",mpin);
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            
            CustomerData customerData = customerDataRepository.findCustomerDataByUpiId(upiId);
            if (customerData == null) {
                throw new ResourceNotFoundException("Customer not found with UPI ID: " + upiId);
            }
            
            logger.debug("Customer found: {}", customerData.getCustId());
            
            if (customerData.getMpin().equalsIgnoreCase(mpin)) {
                logger.warn("Invalid MPIN for customer: {}", customerData.getCustId());
                throw new InvalidCredentialsException("Invalid MPIN");
            }
            
            if (customerData.getAccountBalance().compareTo(amount) < 0) {
                logger.warn("Insufficient balance for customer: {}. Balance: {}, Requested: {}", 
                    customerData.getCustId(), customerData.getAccountBalance(), amount);
                throw new InsufficientBalanceException("Insufficient balance. Available: " + customerData.getAccountBalance());
            }
            
            CustomerData bankPoolData = customerDataRepository.findCustomerDataByUpiId(AppConstants.BANK_UPI_ID);
            if (bankPoolData == null) {
                throw new ResourceNotFoundException("Bank pool account not found");
            }
            
            logger.debug("Bank pool account found");
            
            CustomerTransaction customerTransaction = new CustomerTransaction();
            customerTransaction.setPayer(customerData);
            customerTransaction.setTxnId(data.get("txnId").toString());
            customerTransaction.setRrn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddyyMMddHHmmssSSS")));
            customerTransaction.setTxnType(data.get("type").toString());
            customerTransaction.setAmount(amount);
            customerTransaction.setStatus("SUCCESS");
            customerTransaction.setPayee(AppConstants.BANK_UPI_ID);
            customerTransaction.setCreatedAt(LocalDateTime.now());
            
            customerData.setAccountBalance(customerData.getAccountBalance().subtract(amount));
            bankPoolData.setAccountBalance(bankPoolData.getAccountBalance().add(amount));
            
            logger.info("Saving transaction details - TxnId: {}, Amount: {}", data.get("txnId"), amount);
            customerDataRepository.save(customerData);
            customerDataRepository.save(bankPoolData);
            customerTransactionRepository.save(customerTransaction);
            
            response.put("message", "Debit transaction successful");
            response.put("errorCode", "200");
            response.put("txnId", data.get("txnId"));
            response.put("rrn", customerTransaction.getRrn());
            
            logger.info("Debit transaction completed successfully for customer: {}", customerData.getCustId());
            
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "404");
        } catch (InvalidCredentialsException e) {
            logger.warn("Invalid credentials: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "401");
        } catch (InsufficientBalanceException e) {
            logger.warn("Insufficient balance: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "402");
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "400");
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in request: {}", e.getMessage());
            response.put("message", "Invalid amount or MPIN format");
            response.put("errorCode", "400");
        } catch (Exception e) {
            logger.error("Unexpected error during debit transaction: ", e);
            response.put("message", "An unexpected error occurred during transaction");
            response.put("errorCode", "500");
        }
        
        return response;
    }



    private void validateDebitRequest(Map<Object, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ValidationException("Request data cannot be empty");
        }
        
        if (!data.containsKey("upiId") || data.get("upiId") == null) {
            throw new ValidationException("UPI ID is required");
        }
        
        if (!data.containsKey("mpin") || data.get("mpin") == null) {
            throw new ValidationException("MPIN is required");
        }
        
        if (!data.containsKey("amount") || data.get("amount") == null) {
            throw new ValidationException("Amount is required");
        }
        
        if (!data.containsKey("txnId") || data.get("txnId") == null) {
            throw new ValidationException("Transaction ID is required");
        }
        
        if (!data.containsKey("type") || data.get("type") == null) {
            throw new ValidationException("Transaction type is required");
        }
        
        try {
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount format");
        }
    }

    @Override
    @Transactional
    public Map<Object, Object> doDeposit(Map<Object, Object> data) {
        Map<Object, Object> response = new HashMap<>();
        logger.info("Processing deposit transaction");
        
        try {
            validateDepositWithdrawRequest(data);
            
            String upiId = data.get("upiId").toString().trim();
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            
            CustomerData customerData = customerDataRepository.findCustomerDataByUpiId(upiId);
            if (customerData == null) {
                throw new ResourceNotFoundException("Customer not found with UPI ID: " + upiId);
            }
            
            logger.debug("Customer found for deposit: {}", customerData.getCustId());
            
            CustomerData bankPoolData = customerDataRepository.findCustomerDataByUpiId(AppConstants.BANK_UPI_ID);
            if (bankPoolData == null) {
                throw new ResourceNotFoundException("Bank pool account not found");
            }
            
            CustomerTransaction customerTransaction = new CustomerTransaction();
            customerTransaction.setPayer(bankPoolData);
            customerTransaction.setTxnId(data.get("txnId").toString());
            customerTransaction.setRrn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddyyMMddHHmmssSSS")));
            customerTransaction.setTxnType("DEPOSIT");
            customerTransaction.setAmount(amount);
            customerTransaction.setStatus("SUCCESS");
            customerTransaction.setPayee(customerData.getUpiId());
            customerTransaction.setCreatedAt(LocalDateTime.now());
            
            // Bank Pool deducts and customer receives deposit
            bankPoolData.setAccountBalance(bankPoolData.getAccountBalance().subtract(amount));
            customerData.setAccountBalance(customerData.getAccountBalance().add(amount));
            
            logger.info("Processing deposit - Amount: {}, Customer: {}", amount, customerData.getCustId());
            customerDataRepository.save(bankPoolData);
            customerDataRepository.save(customerData);
            customerTransactionRepository.save(customerTransaction);
            
            response.put("message", "Deposit successful");
            response.put("errorCode", "200");
            response.put("txnId", data.get("txnId"));
            response.put("rrn", customerTransaction.getRrn());
            response.put("newBalance", customerData.getAccountBalance());
            
            logger.info("Deposit completed successfully for customer: {}. New Balance: {}", 
                customerData.getCustId(), customerData.getAccountBalance());
            
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "404");
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "400");
        } catch (InsufficientBalanceException e) {
            logger.warn("Insufficient bank balance for deposit: {}", e.getMessage());
            response.put("message", "Bank pool insufficient balance");
            response.put("errorCode", "402");
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in request: {}", e.getMessage());
            response.put("message", "Invalid amount format");
            response.put("errorCode", "400");
        } catch (Exception e) {
            logger.error("Unexpected error during deposit transaction: ", e);
            response.put("message", "An unexpected error occurred during deposit");
            response.put("errorCode", "500");
        }
        
        return response;
    }

    @Override
    @Transactional
    public Map<Object, Object> doWithdraw(Map<Object, Object> data) {
        Map<Object, Object> response = new HashMap<>();
        logger.info("Processing withdrawal transaction");
        
        try {
            validateDepositWithdrawRequest(data);
            
            String upiId = data.get("upiId").toString().trim();
            String mpin = data.get("mpin").toString();
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            
            CustomerData customerData = customerDataRepository.findCustomerDataByUpiId(upiId);
            if (customerData == null) {
                throw new ResourceNotFoundException("Customer not found with UPI ID: " + upiId);
            }
            
            logger.debug("Customer found for withdrawal: {}", customerData.getCustId());
            if (!customerData.getMpin().equals(mpin)) {
                logger.warn("Invalid MPIN for customer: {}", customerData.getCustId());
                throw new InvalidCredentialsException("Invalid MPIN");
            }
            if (customerData.getAccountBalance().compareTo(amount) < 0) {
                logger.warn("Insufficient balance for withdrawal - Customer: {}, Balance: {}, Requested: {}", 
                    customerData.getCustId(), customerData.getAccountBalance(), amount);
                throw new InsufficientBalanceException("Insufficient balance. Available: " + customerData.getAccountBalance());
            }
            CustomerData bankPoolData = customerDataRepository.findCustomerDataByUpiId(AppConstants.BANK_UPI_ID);
            if (bankPoolData == null) {
                throw new ResourceNotFoundException("Bank pool account not found");
            }
            
            CustomerTransaction customerTransaction = new CustomerTransaction();
            customerTransaction.setPayer(customerData);
            customerTransaction.setTxnId(data.get("txnId").toString());
            customerTransaction.setRrn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddyyMMddHHmmssSSS")));
            customerTransaction.setTxnType("WITHDRAW");
            customerTransaction.setAmount(amount);
            customerTransaction.setStatus("SUCCESS");
            customerTransaction.setPayee(bankPoolData.getUpiId());
            customerTransaction.setCreatedAt(LocalDateTime.now());
            
            // Customer deducts and bank pool receives withdrawal amount
            customerData.setAccountBalance(customerData.getAccountBalance().subtract(amount));
            bankPoolData.setAccountBalance(bankPoolData.getAccountBalance().add(amount));
            
            logger.info("Processing withdrawal - Amount: {}, Customer: {}", amount, customerData.getCustId());
            customerDataRepository.save(customerData);
            customerDataRepository.save(bankPoolData);
            customerTransactionRepository.save(customerTransaction);
            
            response.put("message", "Withdrawal successful");
            response.put("errorCode", "200");
            response.put("txnId", data.get("txnId"));
            response.put("rrn", customerTransaction.getRrn());
            response.put("newBalance", customerData.getAccountBalance());
            
            logger.info("Withdrawal completed successfully for customer: {}. New Balance: {}", 
                customerData.getCustId(), customerData.getAccountBalance());
            
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "404");
        } catch (InvalidCredentialsException e) {
            logger.warn("Invalid credentials: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "401");
        } catch (InsufficientBalanceException e) {
            logger.warn("Insufficient balance: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "402");
        } catch (ValidationException e) {
            logger.warn("Validation error: {}", e.getMessage());
            response.put("message", e.getMessage());
            response.put("errorCode", "400");
        } catch (NumberFormatException e) {
            logger.error("Invalid number format in request: {}", e.getMessage());
            response.put("message", "Invalid amount or MPIN format");
            response.put("errorCode", "400");
        } catch (Exception e) {
            logger.error("Unexpected error during withdrawal transaction: ", e);
            response.put("message", "An unexpected error occurred during withdrawal");
            response.put("errorCode", "500");
        }
        
        return response;
    }

    private void validateDepositWithdrawRequest(Map<Object, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ValidationException("Request data cannot be empty");
        }
        
        if (!data.containsKey("upiId") || data.get("upiId") == null) {
            throw new ValidationException("UPI ID is required");
        }
        
        if (!data.containsKey("amount") || data.get("amount") == null) {
            throw new ValidationException("Amount is required");
        }
        
        if (!data.containsKey("txnId") || data.get("txnId") == null) {
            throw new ValidationException("Transaction ID is required");
        }
        
        if (data.containsKey("mpin") && data.get("mpin") != null) {
            // MPIN validation only for withdraw
            try {
                Long.parseLong(data.get("mpin").toString());
            } catch (NumberFormatException e) {
                throw new ValidationException("Invalid MPIN format");
            }
        }
        
        try {
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount format");
        }
    }

    @Override
    @Transactional
    public Map<Object, Object> makePayment(Map<Object, Object> data) {
        Map<Object, Object> response = new HashMap<>();
        logger.info("Processing payment transaction from payer: {}", data.get("upiId"));
        
        try {
            validateMakePaymentRequest(data);
            
            String txnId = data.get("txnId").toString();
            String payeeUpiId = data.get("payee").toString().trim();
            
            // Step 1: Execute WITHDRAW - Amount from payer to bank pool
            logger.info("Step 1: WITHDRAW - Transferring amount from payer to bank pool");
            Map<Object, Object> withdrawData = new HashMap<>();
            withdrawData.put("upiId", data.get("upiId"));
            withdrawData.put("mpin", data.get("mpin"));
            withdrawData.put("amount", data.get("amount"));
            withdrawData.put("txnId", txnId + "_DEBIT");
            
            Map<Object, Object> withdrawResponse = doWithdraw(withdrawData);
            
            if (!withdrawResponse.get("errorCode").toString().equals("200")) {
                logger.warn("Withdraw failed during payment: {}", withdrawResponse.get("message"));
                return withdrawResponse;
            }
            
            String debitRrn = withdrawResponse.get("rrn").toString();
            BigDecimal payerNewBalance = (BigDecimal) withdrawResponse.get("newBalance");
            logger.info("WITHDRAW completed successfully with RRN: {}", debitRrn);
            
            // Step 2: Execute DEPOSIT - Amount from bank pool to payee
            logger.info("Step 2: DEPOSIT - Transferring amount from bank pool to payee");
            Map<Object, Object> depositData = new HashMap<>();
            depositData.put("upiId", payeeUpiId);
            depositData.put("amount", data.get("amount"));
            depositData.put("txnId", txnId + "_CREDIT");
            
            Map<Object, Object> depositResponse = doDeposit(depositData);
            
            if (!depositResponse.get("errorCode").toString().equals("200")) {
                logger.error("Deposit failed during payment: {}", depositResponse.get("message"));
                return depositResponse;
            }
            
            String creditRrn = depositResponse.get("rrn").toString();
            BigDecimal payeeNewBalance = (BigDecimal) depositResponse.get("newBalance");
            logger.info("DEPOSIT completed successfully with RRN: {}", creditRrn);
            
            // Combine responses
            response.put("message", "Payment successful");
            response.put("errorCode", "200");
            response.put("txnId", txnId);
            response.put("debitRrn", debitRrn);
            response.put("creditRrn", creditRrn);
            response.put("payerNewBalance", payerNewBalance);
            response.put("payeeNewBalance", payeeNewBalance);
            response.put("amount", new BigDecimal(data.get("amount").toString()));
            
            logger.info("Payment transaction completed successfully - TxnId: {}, Amount: {}", 
                txnId, data.get("amount"));
            
        } catch (Exception e) {
            logger.error("Unexpected error during payment transaction: ", e);
            response.put("message", "An unexpected error occurred during payment");
            response.put("errorCode", "500");
        }
        
        return response;
    }

    private void validateMakePaymentRequest(Map<Object, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new ValidationException("Request data cannot be empty");
        }
        
        if (!data.containsKey("upiId") || data.get("upiId") == null) {
            throw new ValidationException("Payer UPI ID is required");
        }
        
        if (!data.containsKey("payee") || data.get("payee") == null) {
            throw new ValidationException("Payee UPI ID is required");
        }
        
        if (!data.containsKey("mpin") || data.get("mpin") == null) {
            throw new ValidationException("MPIN is required");
        }
        
        if (!data.containsKey("amount") || data.get("amount") == null) {
            throw new ValidationException("Amount is required");
        }
        
        if (!data.containsKey("txnId") || data.get("txnId") == null) {
            throw new ValidationException("Transaction ID is required");
        }
        
        String payerUpi = data.get("upiId").toString().trim();
        String payeeUpi = data.get("payee").toString().trim();
        
        if (payerUpi.equals(payeeUpi)) {
            throw new ValidationException("Payer and Payee UPI IDs cannot be the same");
        }
        
        try {
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount format");
        }
        
        try {
            Long.parseLong(data.get("mpin").toString());
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid MPIN format");
        }
    }
}
