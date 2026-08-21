package com.tnt.fincore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationResponse {
    private int id;
    private String custId;
    private String customerName;
    private String accountNumber;
    private String phoneNumber;
    private String upiId;
    private BigDecimal accountBalance;
    private String message;
}
