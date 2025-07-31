package com.study.odersystem.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockInventoryService {
    private final RedisTemplate<String, String> stockTemplate;

    public StockInventoryService(@Qualifier("stockInventory") RedisTemplate<String, String> stockInventoryRedisTemplate) {
        this.stockTemplate = stockInventoryRedisTemplate;
    }

    // 상품 등록시 재고 수량 세팅
    public void makeStockQuantity(Long productId, Integer quantity) {
        stockTemplate.opsForValue().set(productId.toString(), quantity.toString());
    }

    // 주문 성공시 재고 수량 감소
    public void decreaseStockQuantity(Long productId, Integer orderQuantity) {
        int stockQuantity = Integer.parseInt(stockTemplate.opsForValue().get(productId.toString()));
        if (stockQuantity < orderQuantity) {
            throw new IllegalStateException("Stock quantity less than quantity");
        }
        stockTemplate.opsForValue().decrement(productId.toString(), orderQuantity);
    }

    // 주문 취소시 재고 수량 증가
    public void increaseStockQuantity(Long productId, Integer quantity) {
        stockTemplate.opsForValue().increment(productId.toString(), quantity);
    }
}
