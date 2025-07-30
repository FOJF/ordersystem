package com.study.odersystem.ordering.dto;

import com.study.odersystem.ordering.domain.OrderDetail;
import com.study.odersystem.ordering.domain.Ordering;
import com.study.odersystem.product.domain.Product;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailSpecificResDto {
    private Long detailId;
    private String productName;
    private Integer quantity;

    public static OrderDetailSpecificResDto fromEntity(OrderDetail orderDetail) {
        return OrderDetailSpecificResDto.builder()
                .detailId(orderDetail.getId())
                .productName(orderDetail.getProduct().getName())
                .quantity(orderDetail.getQuantity())
                .build();
    }
}
