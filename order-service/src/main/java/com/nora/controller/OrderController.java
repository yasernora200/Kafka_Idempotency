package com.nora.controller;

import com.nora.dto.OrderDto;
import com.nora.service.OrderService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping("/publish")
    public String placeOrder(@RequestBody OrderDto orderDto) {
        return service.placeOrder(orderDto);
    }
}