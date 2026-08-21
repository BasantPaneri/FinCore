package com.tnt.fincore.controller;

import com.tnt.fincore.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//CONTROLLER ONLY FOR THE FINANCIAL TRANSACTIONS.
@RestController
@RequestMapping("/payment")
public class FinController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PaymentService paymentService;

    @PostMapping("/doDebit")
    Map<Object,Object> doDebit (@RequestBody Map<Object,Object> data){
        logger.info("initiating payment api");
        Map<Object,Object> response = paymentService.doDebit(data);
        return new ResponseEntity<>(response, HttpStatus.valueOf(Integer.parseInt(response.get("errorCode").toString()))).getBody();
    }

}
