package com.tnt.fincore.entity;

import com.tnt.fincore.service.CustomerIdSequenceGenerator;
import jakarta.persistence.PrePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataListener {

    private static CustomerIdSequenceGenerator sequenceGenerator;

    @Autowired
    public void setSequenceGenerator(CustomerIdSequenceGenerator generator) {
        sequenceGenerator = generator;
    }

    @PrePersist
    public void generateCustId(CustomerData customerData) {
        if (customerData.getCustId() == null) {
            customerData.setCustId(sequenceGenerator.generateNextCustId());
        }
    }
}

