package io.project.receiver.processors;

import io.project.receiver.domain.PaymentEvent;
import io.project.receiver.events.AccountEvent;
import io.project.receiver.helpers.ObjectMapperHelper;
import io.project.receiver.helpers.UTCTimeProvider;
import io.project.receiver.repositories.EventRepository;
import io.project.receiver.transport.EventProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class NewTransferCreator {

    private final EventRepository eventRepository;
    private final EventProducer eventProducer;

    @EventListener
    @Async
    public void handleNewTransferEvent(AccountEvent event) {
        createTransfer(event);
    }

    @Transactional
    public void createTransfer(AccountEvent event) {
        Optional<PaymentEvent> existingRecord = eventRepository.findById(event.getId());

        if (existingRecord.isPresent()) {
            log.error("Already saved do not send again");
            return;
        }

        if (!"NEW".equalsIgnoreCase(event.getStatus())) {
            log.error("Invalid transaction state: {}. Expected state: NEW", event.getStatus());
            return;
        }

        createNewTransaction(event);
    }

    private void createNewTransaction(AccountEvent event) {
        LocalDateTime now = UTCTimeProvider.getUtcTime();
        PaymentEvent newTransaction = ObjectMapperHelper.fromEventToEntity(event);

        newTransaction.setReceived(now);
        newTransaction.setStatus("PROCESSED");

        PaymentEvent savedTransaction = eventRepository.save(newTransaction);

        eventProducer.sendMessage(event.getId(), event.getTransactionId(), savedTransaction.getStatus());

    }
}
