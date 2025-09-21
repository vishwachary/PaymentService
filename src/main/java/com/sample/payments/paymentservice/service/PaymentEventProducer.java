package com.sample.payments.paymentservice.service;

import com.sample.payments.paymentservice.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    public void sendPaymentEvent(PaymentResponse response) {
        kafkaTemplate.send("payment-topic", response);
        System.out.println("Payment event published to Kafka: " + response);
    }

}
