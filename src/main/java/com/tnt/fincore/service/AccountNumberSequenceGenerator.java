package com.tnt.fincore.service;

import com.tnt.fincore.repository.CustomerDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AccountNumberSequenceGenerator {

    private final AtomicInteger sequence = new AtomicInteger(1000);
    private final CustomerDataRepository customerDataRepository;
    private boolean initialized = false;

    @Autowired
    public AccountNumberSequenceGenerator(CustomerDataRepository customerDataRepository) {
        this.customerDataRepository = customerDataRepository;
    }

    public synchronized String generateNextAccountNumber() {
        if (!initialized) {
            initializeSequence();
        }
        int nextSequence = sequence.incrementAndGet();
        return String.valueOf(nextSequence);
    }

    private void initializeSequence() {
        long maxAccountNumber = customerDataRepository.getMaxAccountNumber();
        if (maxAccountNumber > 1000) {
            sequence.set((int) maxAccountNumber);
        }
        initialized = true;
    }
}
