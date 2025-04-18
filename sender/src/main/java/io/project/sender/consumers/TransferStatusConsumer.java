package io.project.sender.consumers;

import com.google.gson.Gson;
import io.project.sender.events.EventStatusUpdate;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 *
 * @author armena
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransferStatusConsumer {

    private final ApplicationEventPublisher publisher;

    @KafkaListener(topics = "evn-transfer-status", groupId = "sender", concurrency = "3")
    public void duty(@Payload @NonNull String payload,
            @Header(KafkaHeaders.KEY) String key,
            @Header(KafkaHeaders.TOPIC) String topic,
            @Header("X-Producer-Header") String header
    ) {
        log.debug("--- TRANSACTION START ---");
        //log.info("receiver-transfer: key '{}' ", key);
        log.debug("receiver-transfer: payload '{}' ", payload);
        Gson gson = new Gson();
        EventStatusUpdate record = gson.fromJson(payload, EventStatusUpdate.class);
        publisher.publishEvent(record);
    }
}
