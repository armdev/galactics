package io.project.sender.processors;

import io.project.sender.domain.PaymentEvent;
import io.project.sender.events.EventStatusUpdate;

import io.project.sender.helpers.UTCTimeProvider;
import io.project.sender.repositories.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ConsumerProcessor {

    private final EventRepository eventRepository;

    @EventListener
   // @Async
    public void handleNewTransferEvent(EventStatusUpdate event) {
        updateTransfer(event);
    }

    @Transactional("transactionManager")
    public void updateTransfer(EventStatusUpdate event) {
        Optional<PaymentEvent> existingRecord = eventRepository.findById(event.getId());
        if (existingRecord.isPresent()) {
            PaymentEvent record = existingRecord.get();
            record.setUpdated(UTCTimeProvider.getUtcTime());
            record.setStatus(event.getStatus());
          //  log.info("Update record status "+event.getStatus());
            PaymentEvent save = eventRepository.save(record);
           // log.info("SAVED WITH STATUS " +save.getStatus());
            
        }
    }

}
