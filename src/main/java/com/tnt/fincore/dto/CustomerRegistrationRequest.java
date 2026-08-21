package com.tnt.fincore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegistrationRequest {
    private String customerName;
    private String phoneNumber;
    private String upiId;
    private long mpin;
    private BigDecimal accountBalance;
}
