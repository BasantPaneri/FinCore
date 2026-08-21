package com.tnt.fincore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "customer_data")
@EntityListeners(CustomerDataListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "cust_id", unique = true, nullable = false, length = 10)
    private String custId;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "upi_id", unique = true)
    private String upiId;

    @Column(name = "mpin")
    private String mpin;

    @Column(name = "account_balance", precision = 19, scale = 2)
    private BigDecimal accountBalance;

    @OneToMany(mappedBy = "payer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerTransaction> transactions;
}
