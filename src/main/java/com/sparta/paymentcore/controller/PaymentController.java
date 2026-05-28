package com.sparta.paymentcore.controller;

import com.sparta.paymentcore.dto.CancelPaymentRequest;
import com.sparta.paymentcore.dto.ConfirmPaymentRequest;
import com.sparta.paymentcore.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmPaymentRequest request) {
        paymentService.confirm(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id,
                                    @RequestBody CancelPaymentRequest request) {
        paymentService.cancel(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}