package com.sparta.paymentcore.controller;

import com.sparta.paymentcore.dto.CreateOrderRequest;
import com.sparta.paymentcore.dto.CreateOrderResponse;
import com.sparta.paymentcore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<CreateOrderResponse> checkout(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(1L, request));
    }

}
