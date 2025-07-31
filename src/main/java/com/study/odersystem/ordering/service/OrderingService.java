package com.study.odersystem.ordering.service;

import com.study.odersystem.common.service.StockInventoryService;
import com.study.odersystem.common.service.StockRabbitMqService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final StockRabbitMqService stockRabbitMqService;

    // synchronized를 사용하더라도 mariaDB 자체도 멀티 쓰레드로 동작하기 때문에 여전히 문제가 발생할 소지가 있음
    public OrderingSpecificResDto createOrder(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        dtos.forEach(dto -> {
            Product product = this.productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

            // 동시에 접근하는 상황에서 update한 결과의 정합성이 깨지는 갱신이상(lost update) 발생의 소지가 있음
            // 스프링의 버전이나 mariadb의 버전에 따라 jpa에서 강제에러를 유발시켜 대부분의 요청이 실패(Rollback 처리)할 소지가 있음
            // -> 보통 발생하는 상황이다. 갱신이상은 발생하지 않지만 사용자 경험이 너무 떨어지는 문제가 발생할 수 있어서 역시 문제가 됨
            if (product.getStockQuantity() < dto.getProductCount()) throw new IllegalStateException("재고가 부족합니다.");
            product.decreaseStockQuantity(dto.getProductCount());

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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderingSpecificResDto createOrderConcurrent(List<OrderCreateDto> dtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Ordering ordering = Ordering.builder().member(member).build();

        dtos.forEach(dto -> {
            Product product = this.productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

            // redis에서 재고수량 확인 및 재고수량 감소처리
            stockInventoryService.decreaseStockQuantity(dto.getProductId(), dto.getProductCount());

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .ordering(ordering)
                    .quantity(dto.getProductCount())
                    .build();

            ordering.getOrderDetails().add(orderDetail);
            stockRabbitMqService.publish(dto.getProductId(), dto.getProductCount());
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
//                product.decreaseStockQuantity(product.getStockQuantity() - dto.getProductCount());
//                successDetails.add(orderDetail);
//            }
//        }
//
//        if (!failedDetails.isEmpty()) {
//            // 재고 복구 로직: 실패한 상품만 복구
//            successDetails.forEach(orderDetail -> {
//                Product product = orderDetail.getProduct();
//                product.decreaseStockQuantity(product.getStockQuantity() + orderDetail.getQuantity());
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

    public List<OrderingSpecificResDto> findMyList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return this.orderingRepository.findAllByMember(member).stream().map(OrderingSpecificResDto::fromEntity).toList();
    }
}
