package com.study.odersystem.ordering.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.OrderStatus;
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

import java.util.ArrayList;
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


//    // 수량이 부족한 경우에는 주문 취소로 기록을 남기게끔 구현해봄
//    public OrderingSpecificResDto createOrder(List<OrderCreateDto> dtos) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Member member = this.memberRepository.findByEmail(authentication.getName())
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//
//        Ordering ordering = Ordering.builder()
//                .member(member)
//                .build();
//
//        List<OrderDetail> successDetails = new ArrayList<>();
//        List<OrderDetail> failedDetails = new ArrayList<>();
//
//        for (OrderCreateDto dto : dtos) {
//            Product product = this.productRepository.findById(dto.getProductId())
//                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));
//
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .product(product)
//                    .ordering(ordering)
//                    .quantity(dto.getProductCount())
//                    .build();
//
//            if (product.getStockQuantity() < dto.getProductCount()) {
//                failedDetails.add(orderDetail);
//            } else {
//                product.updateStockQuantity(product.getStockQuantity() - dto.getProductCount());
//                successDetails.add(orderDetail);
//            }
//        }
//
//        if (!failedDetails.isEmpty()) {
//            // 재고 복구 로직: 실패한 상품만 복구
//            successDetails.forEach(orderDetail -> {
//                Product product = orderDetail.getProduct();
//                product.updateStockQuantity(product.getStockQuantity() + orderDetail.getQuantity());
//            });
//
//            successDetails.addAll(failedDetails);
//            ordering.updateOrderStatus(OrderStatus.CANCELED); // 전체 주문 취소로 간주
//        } else {
//            ordering.updateOrderStatus(OrderStatus.ORDERED); // 정상 주문
//        }
//
//        ordering.updateOrderDetails(successDetails);
//
//        this.orderingRepository.save(ordering);
//        return OrderingSpecificResDto.fromEntity(ordering);
//    }

    public List<OrderingSpecificResDto> findAll() {
        return this.orderingRepository.findAll().stream().map(OrderingSpecificResDto::fromEntity).toList();
    }
}
