package com.study.odersystem.ordering.dto;

import com.study.odersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDto {
    private Long productId;
    private Integer productCount;
}
