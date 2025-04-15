package io.project.receiver.consumers;

import com.google.gson.Gson;
import io.project.receiver.events.AccountEvent;

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
public class TransferConsumer {

    private final ApplicationEventPublisher publisher;

    @KafkaListener(topics = "evn-transfer", groupId = "receiver", concurrency = "3")
    public void duty(@Payload @NonNull String payload,
            @Header(KafkaHeaders.KEY) String key,
            @Header(KafkaHeaders.TOPIC) String topic,
            @Header("X-Producer-Header") String header
    ) {
        log.debug("--- TRANSACTION START ---");
        log.debug("receiver-transfer: key '{}' ", key);
        log.debug("receiver-transfer: payload '{}' ", payload);
        Gson gson = new Gson();
        AccountEvent record = gson.fromJson(payload, AccountEvent.class);
        publisher.publishEvent(record);
    }
}
