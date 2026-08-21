package com.tnt.fincore.service;

import java.util.Map;

public interface PaymentService {
    Map<Object, Object> doDebit(Map<Object, Object> data);
    Map<Object, Object> doDeposit(Map<Object, Object> data);
    Map<Object, Object> doWithdraw(Map<Object, Object> data);
    Map<Object, Object> makePayment(Map<Object, Object> data);
}
