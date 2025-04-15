package io.project.receiver.transport;

import com.google.gson.Gson;
import io.project.receiver.events.EventStatusUpdate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final TransactionTemplate transactionTemplate;

    private static final String TARGET_TOPIC = "evn-transfer-status";

    public void sendMessage(String id, String transactionId, String status) {
        EventStatusUpdate eventStatusUpdate = new EventStatusUpdate();
        eventStatusUpdate.setId(id);
        eventStatusUpdate.setStatus(status);
        eventStatusUpdate.setTransactionId(transactionId);
        long startTime = System.nanoTime(); // Start the timer

        Gson gson = new Gson();

        ThreadFactory factory = Thread.ofVirtual().factory();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(0, factory);

        Callable<String> scheduledCallable = () -> {

            pushMessage(gson.toJson(eventStatusUpdate), eventStatusUpdate.getTransactionId());

            long endTime = System.nanoTime(); // Stop the timer
            long executionTimeNano = endTime - startTime; // Calculate the execution time in nanoseconds
            double executionTimeSeconds = executionTimeNano / 1_000_000_000.0; // Convert nanoseconds to seconds

            log.debug("Execution time: " + executionTimeSeconds + " seconds");

            return "Done";
        };

        scheduledExecutorService.schedule(scheduledCallable, 1, TimeUnit.SECONDS);

    }

    @SneakyThrows
    @Transactional("kafkaTransactionManager")
    public void pushMessage(String message, String transactionId) {
        log.info("SEND BACK TRANSFER REQUEST TransactionId, sent {}", transactionId);
        ProducerRecord<String, String> producerRecord
                = new ProducerRecord<>(TARGET_TOPIC, transactionId, message);
        producerRecord.headers().add(KafkaHeaders.TOPIC, TARGET_TOPIC.getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add(KafkaHeaders.KEY, transactionId.getBytes(StandardCharsets.UTF_8));
        producerRecord.headers().add("X-Producer-Header", TARGET_TOPIC.getBytes(StandardCharsets.UTF_8));
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(producerRecord);
                future.whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        // Handle failure
                        log.error("@KAFKA FAIL: notification unable to send message='{}'", message, throwable);
                    } else {
                        // Handle success
                        log.info("@KAFKA SENT SUCCESS: Message sent: {}", sendResult.getProducerRecord().value());
                    }
                });

            }
        });
    }

}
