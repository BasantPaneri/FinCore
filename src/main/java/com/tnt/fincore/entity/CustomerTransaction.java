package com.tnt.fincore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", referencedColumnName = "cust_id", nullable = false)
    private CustomerData payer;

    @Column(name = "txn_id", unique = true, nullable = false)
    private String txnId;

    @Column(name = "rrn", unique = true, nullable = false)
    private String rrn;

    @Column(name = "txn_type", nullable = false)
    private String txnType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "payee", nullable = false)
    private String payee;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
