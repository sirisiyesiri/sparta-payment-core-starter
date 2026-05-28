package com.sparta.paymentcore.service;

import com.sparta.paymentcore.dto.CreateOrderRequest;
import com.sparta.paymentcore.dto.CreateOrderResponse;
import com.sparta.paymentcore.entity.CartItem;
import com.sparta.paymentcore.entity.Order;
import com.sparta.paymentcore.entity.Payment;
import com.sparta.paymentcore.entity.Product;
import com.sparta.paymentcore.repository.CartItemRepository;
import com.sparta.paymentcore.repository.OrderRepository;
import com.sparta.paymentcore.repository.PaymentRepository;
import com.sparta.paymentcore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public CreateOrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        //  1. 장바구니 조회 (비어있으면 전체 장바구니 사용)
        // 사용자가 선택한 상품 ID 목록이 비어 있을 경우 → findByMemberId(memberId) : 로그인 한 회원의 장바구니에 담긴 모든 상품
        // 사용자가 선택한 상품 ID 목록이 존재할 경우 → findAllById(request.cartItemIds()) : 사용자가 선택한 특정 상품들만
        List<CartItem> cartItems = request.cartItemIds().isEmpty()
                ? cartItemRepository.findByMemberId(memberId)
                : cartItemRepository.findAllById(request.cartItemIds());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니가 비어있습니다.");
        }

        // 2. 총액 계산 + orderName 구성
        int totalAmount = 0;
        String orderName = "";
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                    () -> new RuntimeException("상품이 존재하지 않습니다." + cartItem.getProductId())
            );
            totalAmount += product.getPrice() * cartItem.getQuantity();
            if (orderName.isEmpty()) {
                orderName = product.getName();
            }
        }
        if (cartItems.size() > 1) {
            orderName += " 외 " + (cartItems.size() - 1) + "건";
        }

        // 3. 주문 저장
        Order order = new Order(memberId, totalAmount);
        orderRepository.save(order);

        // 4. PortOne에 보낼 결제 고유 식별값 생성 + 결제 레코드 저장
        String portOnePaymentId = "pay_" + UUID.randomUUID();
        Payment payment = new Payment(order.getId(), portOnePaymentId, totalAmount);
        paymentRepository.save(payment);

        // 5. 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);

        return new CreateOrderResponse(
                order.getId(),
                payment.getId(),
                portOnePaymentId,
                totalAmount,
                orderName,
                order.getStatus().name()
        );
    }
}
