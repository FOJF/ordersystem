package com.study.odersystem.product.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.dto.*;
import com.study.odersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<?> save(@ModelAttribute ProductCreateDto productCreateDto) {
        Long id = this.productService.save(productCreateDto);
        return ResponseEntity.ok().body(ResponseDto.ofSuccess(id, HttpStatus.OK.value(), "Product saved successfully"));
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto<Page<ProductSummaryResDto>>> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        log.info(pageable.toString());
        log.info(productSearchDto.toString());
        return ResponseEntity.ok(
                ResponseDto.ofSuccess(
                        this.productService.findAll(pageable, productSearchDto),
                        HttpStatus.OK.value(),
                        "Product list successfully")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductDetailResDto>> findById(@PathVariable Long id) {
        ProductDetailResDto dto = this.productService.findById(id);

        return ResponseEntity.ok(ResponseDto.ofSuccess(dto, HttpStatus.OK.value(), "Product detail successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @ModelAttribute ProductUpdateDto productUpdateDto) {
        ProductSummaryResDto dto = this.productService.updateProductDetail(id, productUpdateDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "상품 정보가 수정되었습니다.")
        );
    }
}
