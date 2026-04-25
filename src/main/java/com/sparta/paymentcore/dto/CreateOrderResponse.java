package com.sparta.paymentcore.dto;

public record CreateOrderResponse(
        Long orderId,
        Long paymentId,
        String portonePaymentId,
        int totalAmount,
        String orderName,
        String status
) {}
