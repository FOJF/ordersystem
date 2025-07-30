package com.study.odersystem.ordering.dto;

import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.OrderStatus;
import com.study.odersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderingSpecificResDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailSpecificResDto> orderDetails;

    public static OrderingSpecificResDto fromEntity(Ordering ordering) {
        return OrderingSpecificResDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(ordering.getOrderDetails().stream().map(OrderDetailSpecificResDto::fromEntity).toList())
                .build();
    }
}
