package com.nora.listener;

import com.nora.dto.OrderDto;
import com.nora.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderConsumer {

    private static final String ORDER_TOPIC = "ORDER_TOPIC";

    private final PaymentService paymentService;


    public OrderConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = ORDER_TOPIC, groupId = "payment_group")
    public void processOrder(OrderDto orderDto, Acknowledgment acknowledgment) {

        String orderId = orderDto.getOrderId();
        log.info("Received order event for Order ID: {}", orderId);

        // check requestId already exist in db or not - idempotency check
        try {
            paymentService.processPayment(orderDto);
            log.info("Payment processed for Order ID: {}", orderId);
        }catch (DataIntegrityViolationException ex){
            log.warn("Duplicate payment attempt detected for Order ID: {}. Skipping processing.", orderId);
            acknowledgment.acknowledge(); // Acknowledge the message to prevent reprocessing
            return;
        }
        //application crashes here
        if(true){

            throw new RuntimeException("Simulated processing error for Order ID: " + orderId);
        }

        // Acknowledge the message after successful processing
        acknowledgment.acknowledge();
    }

}
