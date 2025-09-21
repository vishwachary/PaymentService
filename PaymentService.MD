PaymentService
Perfect 💯 you’re now connecting all the pieces of the flow 🚀

Let’s design this cleanly with three Spring Boot services:

✅ 1. PaymentService

Runs on port: 8082

Receives REST calls from OrderService (via Feign).

Persists the payment in H2 DB.

Publishes a Kafka event (payment-topic) if the payment succeeds.

Entity: Payment.java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long orderId;
    private BigDecimal amount;
    private String status; // SUCCESS or FAILED
}
Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

DTOs
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private String status;
}
Kafka Producer
@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentResponse> kafkaTemplate;

    public void sendPaymentEvent(PaymentResponse response) {
        kafkaTemplate.send("payment-topic", response);
        System.out.println("📤 Payment event published to Kafka: " + response);
    }
}


REST Contoller

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer producer;

    @PostMapping("/process")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        // 1️⃣ Dummy logic: Always SUCCESS
        Payment payment = new Payment(null, request.getOrderId(), request.getAmount(), "SUCCESS");
        Payment saved = paymentRepository.save(payment);

        // 2️⃣ Build response
        PaymentResponse response = new PaymentResponse(saved.getOrderId(), saved.getStatus());

        // 3️⃣ Publish Kafka event
        producer.sendPaymentEvent(response);

        return response;
    }
}
kafka prodcuer

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, PaymentResponse> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, PaymentResponse> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
