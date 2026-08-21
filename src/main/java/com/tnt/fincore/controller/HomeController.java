package com.tnt.fincore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/home")
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/hb")
    public ResponseEntity<Map<Object, Object>> heartBeat(){
        logger.info("In HB FIN Core controller....");
        Map<Object, Object> response = new HashMap<>();
        response.put("message", "FinCore is running ....    ");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
