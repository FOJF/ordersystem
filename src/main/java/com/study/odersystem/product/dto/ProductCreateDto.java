package com.study.odersystem.product.dto;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDto {
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private MultipartFile productImg;

    public Product toEntity(Member member) {
        return Product.builder()
                .name(name)
                .category(category)
                .price(price)
                .stockQuantity(stockQuantity)
                .member(member)
                .build();
    }
}
