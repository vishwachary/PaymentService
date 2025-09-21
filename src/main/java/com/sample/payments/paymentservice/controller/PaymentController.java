package com.sample.payments.paymentservice.controller;

import com.sample.payments.paymentservice.dto.PaymentRequest;
import com.sample.payments.paymentservice.dto.PaymentResponse;
import com.sample.payments.paymentservice.entity.Payment;
import com.sample.payments.paymentservice.repository.PaymentRepository;
import com.sample.payments.paymentservice.service.PaymentEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/payments")

public class PaymentController {

    private final PaymentRepository paymentRepository;
   private final PaymentEventProducer producer;

    public PaymentController(PaymentRepository paymentRepository, PaymentEventProducer producer) {
        this.paymentRepository = paymentRepository;
        this.producer = producer;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        //  Dummy logic: Always SUCCESS
        Payment payment = new Payment(null, request.getOrderId(), request.getAmount(), "SUCCESS");
        Payment saved = paymentRepository.save(payment);

        // Build response
        PaymentResponse response = new PaymentResponse(saved.getOrderId(), saved.getStatus());

        // Publish Kafka event
       producer.sendPaymentEvent(response);

        return ResponseEntity.ok(response);
    }

}
