package com.study.odersystem.product.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.dto.ProductCreateDto;
import com.study.odersystem.product.dto.ProductDetailResDto;
import com.study.odersystem.product.dto.ProductSummaryResDto;
import com.study.odersystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<?> save(@ModelAttribute ProductCreateDto productCreateDto,
                                  @RequestParam(name = "productImg", required = false) MultipartFile productImg) {
        Long id = this.productService.save(productCreateDto, productImg);
        return ResponseEntity.ok().body(ResponseDto.ofSuccess(id, HttpStatus.OK.value(), "Product saved successfully"));
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto<List<ProductSummaryResDto>>> findAll() {
        return ResponseEntity.ok(
                ResponseDto.ofSuccess(
                        this.productService.findAll(),
                        HttpStatus.OK.value(),
                        "Product list successfully")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<ProductDetailResDto>> findById(@PathVariable Long id) {
        ProductDetailResDto dto = this.productService.findById(id);

        return ResponseEntity.ok(ResponseDto.ofSuccess(dto, HttpStatus.OK.value(), "Product detail successfully"));
    }
}
