package com.sparta.paymentcore.service;

import com.sparta.paymentcore.dto.CancelPaymentRequest;
import com.sparta.paymentcore.dto.ConfirmPaymentRequest;
import com.sparta.paymentcore.dto.PortOnePaymentResponse;
import com.sparta.paymentcore.entity.Order;
import com.sparta.paymentcore.entity.Payment;
import com.sparta.paymentcore.entity.PaymentStatus;
import com.sparta.paymentcore.infra.PortOneClient;
import com.sparta.paymentcore.repository.OrderRepository;
import com.sparta.paymentcore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PortOneClient portOneClient;
    private final OrderRepository orderRepository;

    @Transactional
    public void confirm(ConfirmPaymentRequest request) {
        // 1. DB에서 결제 조회
        Payment payment = paymentRepository.findByPortonePaymentId(request.portonePaymentId()).orElseThrow(
                () -> new IllegalArgumentException("")
        );

        // 2. PortOne에서 실제 결제 정보 조회
        PortOnePaymentResponse portOnePaymentResponse = portOneClient.getPayment(request.portonePaymentId());

        // 3. 금액 인증
        if (payment.getAmount() != portOnePaymentResponse.amount().total()) {
            throw new RuntimeException("금액 불일치! DB : " + payment.getAmount()
                    + ", PortOne : " + portOnePaymentResponse.amount().total());
        }

        // 4. 상태 업데이트
        payment.markAsPaid();

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));
        order.confirm();
    }

    @Transactional
    public void cancel(Long paymentId, CancelPaymentRequest request) {
        // 1. DB에서 결제 조회
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(
                () -> new RuntimeException("결제 정보를 찾을 수 없습니다.")
        );

        // 2. 이미 취소된 결제인지 확인 <- 이중 취소 방지
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new RuntimeException("이미 취소된 결제입니다.");
        }

        // 3. DB 상태 업데이트
        payment.markAsCancelled();

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다."));
        order.cancel();

        // 4. PortOne 취소 API 호출
        try {
            portOneClient.cancelPayment(payment.getPortonePaymentId(), request.reason());
        } catch (Exception e) {
            log.error("PG 결제 취소 실패 : DB는 이미 CANCELLED 커밋됨, 수동 처리 필요 : portonePaymentId={}", payment.getPortonePaymentId());
        }
    }
}
