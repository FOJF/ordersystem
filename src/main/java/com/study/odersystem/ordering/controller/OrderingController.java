package com.study.odersystem.ordering.controller;

import com.study.odersystem.common.dto.ResponseDto;
import com.study.odersystem.ordering.dto.OrderCreateDto;
import com.study.odersystem.ordering.dto.OrderingSpecificResDto;
import com.study.odersystem.ordering.service.OrderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto.ofSuccess(dto, HttpStatus.CREATED.value(), "주문이 정상적으로 진행되고 있습니다."));
    }

}
