package com.study.odersystem.ordering.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderingController {
    private final OrderingService orderingService;

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestBody List<OrderCreateDto> orderCreateDtos) {
        OrderingSpecificResDto dto = this.orderingService.createOrder(orderCreateDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "주문 완료"));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll() {
        List<OrderingSpecificResDto> dtos = this.orderingService.findAll();
        return ResponseEntity.ok().body(
                ResponseDto.ofSuccess(dtos, HttpStatus.OK.value(), "주문 목록 조회")
        );
    }
}
