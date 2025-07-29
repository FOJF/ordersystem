package com.study.odersystem.product.dto;

import com.study.odersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailResDto {
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String email;
    private String url;

    public static ProductDetailResDto fromEntity(Product product) {
        return ProductDetailResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .url(product.getUrl())
                .email(product.getMember().getEmail())
                .build();
    }
}
