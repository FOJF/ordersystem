package com.study.odersystem.ordering.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.Ordering;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.repository.OrderingRepository;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    public OrderingSpecificResDto createOrder(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        dtos.forEach(dto -> {
            Product product = this.productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

            if (product.getStockQuantity() < dto.getProductCount()) throw new IllegalStateException("재고가 부족합니다.");
            product.updateStockQuantity(product.getStockQuantity() - dto.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .ordering(ordering)
                    .quantity(dto.getProductCount())
                    .build();

            ordering.getOrderDetails().add(orderDetail);
        });

        this.orderingRepository.save(ordering);
        return OrderingSpecificResDto.fromEntity(ordering);
    }
}
