package io.project.receiver.main;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 *
 * @author armena
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.client-id}")
    private String clientConfig;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        // Create a map to hold Kafka consumer configuration properties
        Map<String, Object> props = new HashMap<>();

        // The Kafka server to connect to, specified by its address
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // The consumer group ID, which allows consumers with the same group ID to coordinate and load-balance consumption of topics
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // A unique client identifier, useful for monitoring and tracking the performance of the consumer
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientConfig);

        // Deserializer for the key. Using ErrorHandlingDeserializer ensures that deserialization errors are handled gracefully
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // The actual deserializer for the key, wrapped by the ErrorHandlingDeserializer, using JsonDeserializer to handle JSON format
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Correct deserializer for the key. Use StringDeserializer for deserializing the key
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Correct deserializer for the value. Use StringDeserializer for deserializing the value
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // Set the concurrency to the number of partitions
        return factory;
    }

}
