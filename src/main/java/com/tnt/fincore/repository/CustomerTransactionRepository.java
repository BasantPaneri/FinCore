package com.tnt.fincore.repository;

import com.tnt.fincore.entity.CustomerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerTransactionRepository extends JpaRepository<CustomerTransaction ,Integer> {

}
