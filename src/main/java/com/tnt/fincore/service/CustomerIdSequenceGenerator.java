package com.tnt.fincore.service;

import com.tnt.fincore.repository.CustomerDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CustomerIdSequenceGenerator {

    private final AtomicInteger sequence = new AtomicInteger(0);
    private final CustomerDataRepository customerDataRepository;
    private boolean initialized = false;

    @Autowired
    public CustomerIdSequenceGenerator(CustomerDataRepository customerDataRepository) {
        this.customerDataRepository = customerDataRepository;
    }

    public synchronized String generateNextCustId() {
        if (!initialized) {
            initializeSequence();
        }
        int nextSequence = sequence.incrementAndGet();
        return "FC" + nextSequence;
    }

    private void initializeSequence() {
        long maxSequence = customerDataRepository.getMaxCustIdSequence();
        sequence.set((int) maxSequence);
        initialized = true;
    }
}
