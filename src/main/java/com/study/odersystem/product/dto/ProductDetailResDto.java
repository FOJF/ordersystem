package com.study.odersystem.product.dto;

import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<ProductImage> productImages;

    public static ProductDetailResDto fromEntity(Product product) {
        return ProductDetailResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .productImages(product.getProductImages())
                .email(product.getMember().getEmail())
                .build();
    }
}
