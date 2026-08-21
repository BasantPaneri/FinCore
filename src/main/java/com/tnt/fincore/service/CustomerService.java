package com.tnt.fincore.service;

import com.tnt.fincore.dto.CustomerRegistrationRequest;
import com.tnt.fincore.dto.CustomerRegistrationResponse;

public interface CustomerService {
    CustomerRegistrationResponse registerCustomer(CustomerRegistrationRequest request);
}
