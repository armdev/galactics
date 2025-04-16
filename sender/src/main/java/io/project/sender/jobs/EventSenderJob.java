package io.project.sender.jobs;

import io.project.sender.events.AccountEvent;
import io.project.sender.events.AccountEventDataGenerator;
import io.project.sender.transport.EventProducer;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author lenovo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSenderJob {

    private final EventProducer eventProducer;

    @Scheduled(fixedRate = 5000)
    public void job() {
        List<AccountEvent> events = AccountEventDataGenerator.generateEvents();
        log.info("Send events size " + events.size());
        eventProducer.sendMessage(events);

    }

}
