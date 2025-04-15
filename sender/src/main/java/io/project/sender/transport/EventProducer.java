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
import java.util.concurrent.Callable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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

    private final EventRepository eventRepository;

    private static final String TARGET_TOPIC = "evn-transfer";

    public void sendMessage1(List<AccountEvent> events) {

        long startTime = System.nanoTime(); // Start the timer

        Gson gson = new Gson();

        ThreadFactory factory = Thread.ofVirtual().factory();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(0, factory);

        Callable<String> scheduledCallable = () -> {

            for (AccountEvent event : events) {
                PaymentEvent fromEventToEntity = ObjectMapperHelper.fromEventToEntity(event);
                LocalDateTime utcTime = UTCTimeProvider.getUtcTime();
                fromEventToEntity.setCreated(utcTime);
                fromEventToEntity.setUpdated(utcTime);
                eventRepository.save(fromEventToEntity);
                pushMessage(gson.toJson(event), event.getTransactionId());
            }

            long endTime = System.nanoTime(); // Stop the timer
            long executionTimeNano = endTime - startTime; // Calculate the execution time in nanoseconds
            double executionTimeSeconds = executionTimeNano / 1_000_000_000.0; // Convert nanoseconds to seconds

            log.info("Execution time: " + executionTimeSeconds + " seconds");

            return "Done";
        };

        scheduledExecutorService.schedule(scheduledCallable, 1, TimeUnit.SECONDS);

    }

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

        log.info("Execution time: " + executionTimeSeconds + " seconds");
    }

    @SneakyThrows
    @Transactional("kafkaTransactionManager")
    public void pushMessage(String message, String transactionId) {
       // log.info("TRANSFER REQUEST TransactionId, sent {}", transactionId);
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
                      log.info("@KAFKA SUCCESS: Message sent: {}", sendResult.getProducerRecord().value());
                    }
                });

            }
        });
    }

}
