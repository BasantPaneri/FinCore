package com.tnt.fincore.service.impl;

import com.tnt.fincore.dto.CustomerRegistrationRequest;
import com.tnt.fincore.dto.CustomerRegistrationResponse;
import com.tnt.fincore.entity.CustomerData;
import com.tnt.fincore.exception.DuplicateResourceException;
import com.tnt.fincore.exception.ValidationException;
import com.tnt.fincore.repository.CustomerDataRepository;
import com.tnt.fincore.service.AccountNumberSequenceGenerator;
import com.tnt.fincore.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerDataRepository customerDataRepository;

    @Autowired
    private AccountNumberSequenceGenerator accountNumberSequenceGenerator;

    @Override
    public CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request) {
        try {
            validateRegistrationRequest(request);
            
            logger.info("Registering new customer: {}", request.getCustomerName());
            
            CustomerData customer = new CustomerData();
            customer.setCustomerName(request.getCustomerName());
            customer.setAccountNumber(accountNumberSequenceGenerator.generateNextAccountNumber());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setUpiId(request.getUpiId());
            customer.setMpin(String.valueOf(request.getMpin()));
            customer.setAccountBalance(request.getAccountBalance());

            CustomerData savedCustomer = customerDataRepository.save(customer);
            logger.info("Customer registered successfully with custId: {} and accountNumber: {}", savedCustomer.getCustId(), savedCustomer.getAccountNumber());

            return new CustomerRegistrationResponse(
                    savedCustomer.getId(),
                    savedCustomer.getCustId(),
                    savedCustomer.getCustomerName(),
                    savedCustomer.getAccountNumber(),
                    savedCustomer.getPhoneNumber(),
                    savedCustomer.getUpiId(),
                    savedCustomer.getAccountBalance(),
                    "Customer registered successfully"
            );
        } catch (DataIntegrityViolationException e) {
            logger.error("Duplicate resource detected: {}", e.getMessage());
            throw new DuplicateResourceException("Phone number or UPI ID already exists");
        } catch (ValidationException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error while registering customer: ", e);
            throw new RuntimeException("Failed to register customer: " + e.getMessage());
        }
    }

    private void validateRegistrationRequest(CustomerRegistrationRequest request) {
        if (request == null) {
            throw new ValidationException("Registration request cannot be null");
        }
        
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new ValidationException("Customer name is required");
        }
        
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new ValidationException("Phone number is required");
        }
        
        if (!request.getPhoneNumber().matches("^[0-9]{10}$")) {
            throw new ValidationException("Phone number must be exactly 10 digits");
        }
        
        if (request.getUpiId() == null || request.getUpiId().trim().isEmpty()) {
            throw new ValidationException("UPI ID is required");
        }
        
        if (!request.getUpiId().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+$")) {
            throw new ValidationException("UPI ID format is invalid");
        }
        
        if (request.getMpin() <= 0) {
            throw new ValidationException("MPIN must be a positive number");
        }
        
        if (request.getAccountBalance() == null || request.getAccountBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Account balance must be non-negative");
        }
    }
}
