package com.study.odersystem.product.service;

import com.study.odersystem.member.domain.Member;
import com.study.odersystem.member.repository.MemberRepository;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.domain.ProductImage;
import com.study.odersystem.product.dto.*;
import com.study.odersystem.product.repository.ProductImageRepository;
import com.study.odersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
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

    public Long save(ProductCreateDto productCreateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("Member not found"));

        log.info("Saving product details for member id {}", member.getId());
        Product product = this.productRepository.save(productCreateDto.toEntity(member));
        MultipartFile productImg = productCreateDto.getProductImg();

        if (productImg != null) {
            log.info(productImg.getOriginalFilename());
            try {
                String[] s = productImg.getOriginalFilename().split("\\.");
                String newProductImgName = "product-" + product.getId() + "-productimg-1." + s[s.length - 1];

//                String newProductImgName = "product-" + product.getId() + "-productimg-1" + productImg.getOriginalFilename().substring(productImg.getOriginalFilename().lastIndexOf('.'));

                // 이미지를 byte 형태로 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(newProductImgName)
                        .contentType(productImg.getContentType()) //image/jpeg, video/mp4 ...
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productImg.getBytes()));

                String productImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newProductImgName)).toExternalForm();

                ProductImage productImage = ProductImage.builder()
                        .fileName(newProductImgName)
                        .url(productImgUrl)
                        .product(product)
                        .build();

                log.info(productImage.getFileName());
                log.info(productImage.getProduct().getName());

                product.getProductImages().add(productImage);
            } catch (IOException e) {
                // checkedException을 uncheckedException으로 변경해 rollback 되도록 예외 처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }
        }

        return product.getId();
    }

    public Page<ProductSummaryResDto> findAll(Pageable pageable, ProductSearchDto productSearchDto) {
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // Root : 엔티티의 속성을 접근하기 위한 객체
                // criteriaBuilder : 쿼리를 생성하기 위한 객체
                List<Predicate> predicates = new ArrayList<>();

                if (productSearchDto.getCategory() != null)
                    predicates.add(criteriaBuilder.equal(root.get("category"), productSearchDto.getCategory()));

                if (productSearchDto.getProductName() != null)
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + productSearchDto.getProductName() + "%"));

                Predicate[] predicateArr = predicates.toArray(Predicate[]::new);
                return criteriaBuilder.and(predicateArr);
            }
        };
        Page<Product> products = this.productRepository.findAll(specification, pageable);
        return products.map(ProductSummaryResDto::fromEntity);
    }

    public ProductDetailResDto findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ProductDetailResDto.fromEntity(product);
    }

    public ProductSummaryResDto updateProductDetail(Long id, ProductUpdateDto dto) {
        Product product = this.productRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = this.memberRepository.findByEmail(authentication.getName()).orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (!member.getId().equals(product.getMember().getId())) throw new AccessDeniedException("Access denied");

        product.updateDetail(dto);

        product.getProductImages().forEach(productImage -> {
            s3Client.deleteObject(a -> a.bucket(bucket).key(productImage.getFileName()));
        });

        product.getProductImages().clear();

        if (dto.getProductImg() != null) {
            MultipartFile productImg = dto.getProductImg();
            try {
                String[] s = productImg.getOriginalFilename().split("\\.");
                String newProductImgName = "product-" + product.getId() + "-productimg-1." + s[s.length - 1];

                // 이미지를 byte 형태로 업로드
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(newProductImgName)
                        .contentType(productImg.getContentType()) //image/jpeg, video/mp4 ...
                        .build();
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(productImg.getBytes()));

                String productImgUrl = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(newProductImgName)).toExternalForm();

                ProductImage productImage = ProductImage.builder()
                        .fileName(newProductImgName)
                        .url(productImgUrl)
                        .product(product)
                        .build();

                product.getProductImages().add(productImage);
            } catch (IOException e) {
                // checkedException을 uncheckedException으로 변경해 rollback 되도록 예외 처리
                throw new IllegalArgumentException("이미지 업로드 실패");
            }
        }


        //        s3Client.deleteObject(a -> a.bucket(bucket).key(파일명));


        return ProductSummaryResDto.fromEntity(product);


        // s3 버킷에 있는 파일 삭제
        //        s3Client.deleteObject(a -> a.bucket(bucket).key(파일명));
    }
}
