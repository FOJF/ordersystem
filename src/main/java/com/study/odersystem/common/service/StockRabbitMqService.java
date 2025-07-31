package com.study.odersystem.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.odersystem.common.config.RabbitMqConfig;
import com.study.odersystem.common.dto.StockRabbitMqDto;
import com.study.odersystem.product.domain.Product;
import com.study.odersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StockRabbitMqService {
    private final RabbitTemplate rabbitTemplate;
    private final ProductRepository productRepository;

    public void publish(Long productId, Integer productCount) {
        StockRabbitMqDto stockRabbitMqDto = StockRabbitMqDto.builder()
                .productId(productId)
                .productCount(productCount)
                .build();
        rabbitTemplate.convertAndSend(RabbitMqConfig.stockDecreaseQueueName, stockRabbitMqDto);
    }

    // listener는 단일 스레드로 메세지를 처리하므로, 동시성이슈X
    @RabbitListener(queues = RabbitMqConfig.stockDecreaseQueueName)
    @Transactional
    public void subscribe(Message message) throws IOException {

//        String messageBody = new String(message.getBody());
//
//        System.out.println(messageBody);
//        System.out.println(message.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        StockRabbitMqDto stockRabbitMqDto = objectMapper.readValue(message.getBody(), StockRabbitMqDto.class);

        Product product = this.productRepository.findById(stockRabbitMqDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.decreaseStockQuantity(stockRabbitMqDto.getProductCount());
    }
}
