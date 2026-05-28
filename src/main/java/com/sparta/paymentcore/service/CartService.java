package com.sparta.paymentcore.service;

import com.sparta.paymentcore.dto.AddCartItemRequest;
import com.sparta.paymentcore.dto.AddCartItemResponse;
import com.sparta.paymentcore.entity.CartItem;
import com.sparta.paymentcore.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;

    @Transactional
    public AddCartItemResponse addItem(Long memberId, AddCartItemRequest request) {
        // 장바구니에 회원이 담은 상품이 존재하는지 확인
        Optional<CartItem> existing = cartItemRepository.findByMemberIdAndProductId(memberId, request.productId());

        // 장바구니에 이미 해당 상품이 존재하면, 수량만 변경
        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.addQuantity(request.quantity());
            CartItem savedCartItem = cartItemRepository.save(cartItem);
            return new AddCartItemResponse(savedCartItem.getId());
        }

        // 장바구니에 해당 상품이 존재하지 않으면, 새로운 CartItem 생성
        CartItem cartItem = new CartItem(
                memberId,
                request.productId(),
                request.quantity()
        );
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return new AddCartItemResponse(savedCartItem.getId());
    }
}
