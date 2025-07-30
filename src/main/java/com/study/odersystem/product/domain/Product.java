package com.study.odersystem.product.domain;

import com.study.odersystem.common.domain.BaseTime;
import com.study.odersystem.member.domain.Member;
import com.study.odersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Product extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String name;
    private Integer price;
    private String category;
    private Integer stockQuantity;
//    private String url;
    @Builder.Default
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    public void updateDetail(ProductUpdateDto productUpdateDto) {
        if (productUpdateDto.getName() != null)
            this.name = productUpdateDto.getName();

        if (productUpdateDto.getCategory() != null)
            this.category = productUpdateDto.getCategory();

        if (productUpdateDto.getPrice() != null)
            this.price = productUpdateDto.getPrice();

        if (productUpdateDto.getStockQuantity() != null)
            this.stockQuantity = productUpdateDto.getStockQuantity();
    }

    public void updateStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

//    public void updateUrl(String url) {
//        this.url = url;
//    }
}
