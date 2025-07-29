package com.study.odersystem.product.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.dto.ProductCreateDto;
import com.study.odersystem.product.dto.ProductDetailResDto;
import com.study.odersystem.product.dto.ProductSummaryResDto;
import com.study.odersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public Long save(ProductCreateDto productCreateDto, MultipartFile productImg) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("Member not found"));

        log.info("Saving product details for member id {}", member.getId());
        Product product = this.productRepository.save(productCreateDto.toEntity(member));
        if (productImg != null)
            try {
                String[] s = productImg.getOriginalFilename().split("\\.");
                String newProductImgName = "product-" + product.getId() + "-productimg." + s[s.length - 1];

                // 이미지를 byte 형태로 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(newProductImgName)
                        .contentType(productImg.getContentType()) //image/jpeg, video/mp4 ...
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productImg.getBytes()));

                String productImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newProductImgName)).toExternalForm();
                product.updateUrl(productImgUrl);
            } catch (IOException e) {
                // checkedException을 uncheckedException으로 변경해 rollback 되도록 예외 처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

        return product.getId();
    }

    public List<ProductSummaryResDto> findAll() {
        return productRepository.findAll().stream().map(ProductSummaryResDto::fromEntity).toList();
    }

    public ProductDetailResDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductDetailResDto.fromEntity(product);
    }
}
