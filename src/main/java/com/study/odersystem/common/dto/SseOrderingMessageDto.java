package com.study.odersystem.common.dto;

import com.study.odersystem.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseOrderingMessageDto {
    private String sender;
    private String receiver;
    private Long orderingId;
    private OrderStatus orderStatus;
}
