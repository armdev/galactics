package io.project.sender.transport;

import com.google.gson.Gson;
import io.project.sender.domain.PaymentEvent;
import io.project.sender.events.AccountEvent;
import io.project.sender.helpers.ObjectMapperHelper;
import io.project.sender.helpers.UTCTimeProvider;
import io.project.sender.repositories.EventRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TransactionTemplate transactionTemplate;

    private final EventRepository eventRepository;

    private static final String TARGET_TOPIC = "evn-transfer";

    public void sendMessage(List<AccountEvent> events) {
        long startTime = System.nanoTime(); // Start the timer

        Gson gson = new Gson();

        ///  ThreadFactory factory = Thread.ofVirtual().factory();

        // Create a thread pool with a fixed number of threads
        ExecutorService executorService = Executors.newFixedThreadPool(600);
        ///increase for performance

        for (AccountEvent event : events) {
            executorService.execute(() -> {
                // Perform the pushMessage operation in a separate thread
                PaymentEvent fromEventToEntity = ObjectMapperHelper.fromEventToEntity(event);
                LocalDateTime utcTime = UTCTimeProvider.getUtcTime();
                fromEventToEntity.setCreated(utcTime);
                fromEventToEntity.setUpdated(utcTime);
                eventRepository.save(fromEventToEntity);
                this.pushMessage(gson.toJson(event), event.getTransactionId());
            });
        }

        // Shutdown the executor service and wait for all tasks to complete
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
        }

        long endTime = System.nanoTime(); // Stop the timer
        long executionTimeNano = endTime - startTime; // Calculate the execution time in nanoseconds
        double executionTimeSeconds = executionTimeNano / 1_000_000_000.0; // Convert nanoseconds to seconds
        log.info("-------------------- ");
        log.info("Execution time: " + executionTimeSeconds + " seconds");
        log.info("-------------------- ");
    }

    @SneakyThrows
    @Transactional("kafkaTransactionManager")
    public void pushMessage(String message, String transactionId) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TARGET_TOPIC, transactionId, message);
        producerRecord.headers().add(KafkaHeaders.TOPIC, TARGET_TOPIC.getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add(KafkaHeaders.KEY, transactionId.getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add("X-Producer-Header", TARGET_TOPIC.getBytes(StandardCharsets.UTF_8));

        transactionTemplate.executeWithoutResult(status -> {
            try {
                SendResult<String, String> sendResult = kafkaTemplate.send(producerRecord).get(10, TimeUnit.SECONDS);
                log.info("@KAFKA SUCCESS: Message sent to topic={}, partition={}, offset={}, transactionId={}",
                        sendResult.getRecordMetadata().topic(),
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset(),
                        transactionId);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("@KAFKA FAILURE: Failed to send message for transactionId={}, message={}", transactionId, message, e);
                throw new RuntimeException("Kafka send failed for transactionId=" + transactionId, e);
            }
        });
    }

}
