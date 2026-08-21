package com.tnt.fincore.controller;

import com.tnt.fincore.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/debit")
    public ResponseEntity<Map<Object, Object>> doDebit(@RequestBody Map<Object, Object> request) {
        logger.info("Debit request received for UPI: {}", request.get("upiId"));
        
        if (!request.containsKey("txnId") || request.get("txnId") == null) {
            request.put("txnId", UUID.randomUUID().toString());
        }
        
        Map<Object, Object> response = paymentService.doDebit(request);
        
        int statusCode = Integer.parseInt(response.get("errorCode").toString());
        if (statusCode == 200) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (statusCode == 400) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (statusCode == 401) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else if (statusCode == 402) {
            return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED);
        } else if (statusCode == 404) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<Object, Object>> doDeposit(@RequestBody Map<Object, Object> request) {
        logger.info("Deposit request received for UPI: {}", request.get("upiId"));
        
        if (!request.containsKey("txnId") || request.get("txnId") == null) {
            request.put("txnId", UUID.randomUUID().toString());
        }
        
        Map<Object, Object> response = paymentService.doDeposit(request);
        
        int statusCode = Integer.parseInt(response.get("errorCode").toString());
        if (statusCode == 200) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (statusCode == 400) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (statusCode == 402) {
            return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED);
        } else if (statusCode == 404) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<Object, Object>> doWithdraw(@RequestBody Map<Object, Object> request) {
        logger.info("Withdraw request received for UPI: {}", request.get("upiId"));
        
        if (!request.containsKey("txnId") || request.get("txnId") == null) {
            request.put("txnId", UUID.randomUUID().toString());
        }
        
        Map<Object, Object> response = paymentService.doWithdraw(request);
        
        int statusCode = Integer.parseInt(response.get("errorCode").toString());
        if (statusCode == 200) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (statusCode == 400) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (statusCode == 401) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else if (statusCode == 402) {
            return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED);
        } else if (statusCode == 404) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/makePayment")
    public ResponseEntity<Map<Object, Object>> makePayment(@RequestBody Map<Object, Object> request) {
        logger.info("Payment request received from payer UPI: {} to payee UPI: {}", 
            request.get("upiId"), request.get("payee"));
        
        if (!request.containsKey("txnId") || request.get("txnId") == null) {
            request.put("txnId", UUID.randomUUID().toString());
        }
        
        Map<Object, Object> response = paymentService.makePayment(request);
        
        int statusCode = Integer.parseInt(response.get("errorCode").toString());
        if (statusCode == 200) {
            logger.info("Payment successful - TxnId: {}", request.get("txnId"));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (statusCode == 400) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (statusCode == 401) {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else if (statusCode == 402) {
            return new ResponseEntity<>(response, HttpStatus.PAYMENT_REQUIRED);
        } else if (statusCode == 404) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
