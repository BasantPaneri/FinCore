package com.tnt.fincore.controller;

import com.tnt.fincore.dto.CustomerRegistrationRequest;
import com.tnt.fincore.dto.CustomerRegistrationResponse;
import com.tnt.fincore.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("customer")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationResponse> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request) {
        logger.info("Received customer registration request for: {}", request.getCustomerName());
        CustomerRegistrationResponse response = customerService.registerCustomer(request);
        logger.info("Customer registration successful with custId: {}", response.getCustId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
