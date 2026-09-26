package com.nora.service;

import com.nora.dto.OrderDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class OrderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String placeOrder(OrderDto orderDto) {
        // توليد requestId ثابت ومشتق من الـ orderId
        if (orderDto.getRequestId() == null || orderDto.getRequestId().isEmpty()) {
            String generatedRequestId = UUID.nameUUIDFromBytes(orderDto.getOrderId().getBytes(StandardCharsets.UTF_8)).toString();
            orderDto.setRequestId(generatedRequestId);
        }

        // Publish order event to Kafka
        kafkaTemplate.send("ORDER_TOPIC", orderDto.getOrderId(), orderDto);

        return "Order placed successfully!";
    }
}