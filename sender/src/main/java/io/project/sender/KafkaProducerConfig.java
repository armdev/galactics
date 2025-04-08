package io.project.sender;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author armen
 */
@Configuration
@EnableTransactionManagement
@Slf4j
public class KafkaProducerConfig {

    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.client-id}")
    private String clientIdConfig;

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean(name = "kafkaTransactionManager")
    public KafkaTransactionManager<String, String> kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        // Create a map to hold Kafka producer configuration properties
        final Map<String, Object> configProps = new HashMap<>();

        // The Kafka server to connect to, specified by its address
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Maximum size (in bytes) of a request to the broker. Used to avoid too large payloads.
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 1048570000);

        // Size of the batch (in bytes) to send to the broker. Larger batches improve efficiency by reducing the number of requests.
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 20971520);

        // Number of retries if a request fails. Retries are used to handle transient errors.
        configProps.put(ProducerConfig.RETRIES_CONFIG, 10);

        // The Kafka server to connect to (repeated here for clarity, should ideally be defined once in code)
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Serializer for the key (in this case, a String)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Serializer for the value (in this case, also a String)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Enables idempotency, which ensures exactly-once delivery by preventing duplicate records in case of retries
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Acknowledgement mode. "all" ensures that all replicas have acknowledged the message, providing the highest level of durability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");

        // Limits the number of unacknowledged requests per connection to improve reliability with idempotency enabled
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Compression type for messages to save bandwidth and improve performance, SNAPPY is a fast compression algorithm
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name);

        // A unique ID for the producer's transactions, ensuring proper transaction isolation and management
        configProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transfers-" + UuidCreator.getTimeOrderedEpoch());

        // Timeout in milliseconds for completing a transaction. This ensures that a transaction is not left hanging for too long.
        configProps.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15000);

        // A custom ID for the producer client, useful for monitoring and logging
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientIdConfig);

        // Delay in milliseconds before sending a batch, useful for grouping more records into a single batch to increase throughput
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);

        // Adjusted batch size (32KB), which is more suitable for high-throughput systems
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);

        // The timeout in milliseconds for the broker to respond to requests. If exceeded, a retry will be attempted.
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 15000);

        // Maximum time (in milliseconds) that Kafka will attempt to deliver a message before considering it failed
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // Initial backoff time before attempting to reconnect to a broker after a connection failure, to reduce overwhelming the broker
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 50);

        // Maximum time to wait between reconnect attempts, to avoid overloading the broker with frequent connection attempts
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 1000);

        // The window of time (in milliseconds) for sampling metrics data for monitoring Kafka performance
        configProps.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 30000);

        // The number of samples maintained to compute metrics averages. More samples help smooth out spikes in metric data.
        configProps.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, 2);

        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        // Create and return a new Kafka Producer Factory using the above configuration
        return new DefaultKafkaProducerFactory<>(configProps);
    }

}
