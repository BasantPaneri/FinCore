package com.tnt.fincore.repository;

import com.tnt.fincore.entity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerDataRepository extends JpaRepository<CustomerData, Integer> {

    @Query(value = "SELECT COALESCE(CAST(SUBSTRING(MAX(cust_id), 3) AS UNSIGNED), 0) FROM customer_data", nativeQuery = true)
    long getMaxCustIdSequence();

    @Query(value = "SELECT COALESCE(CAST(MAX(account_number) AS UNSIGNED), 1000) FROM customer_data", nativeQuery = true)
    long getMaxAccountNumber();

    CustomerData findCustomerDataByUpiId(String upiId);
}
