package io.project.sender.jobs;

import io.project.sender.events.AccountEvent;
import io.project.sender.events.AccountEventDataGenerator;
import io.project.sender.transport.EventProducer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSenderJob {

    private final EventProducer eventProducer;

    // Create a thread pool for parallel processing
    private final ExecutorService executor = Executors.newFixedThreadPool(5); // Adjust pool size as needed

    @Scheduled(fixedRate = 5000)
    public void job() {

        if (((ThreadPoolExecutor) executor).getQueue().remainingCapacity() == 0) {
            log.warn("Executor queue is full, skipping this cycle");
            return;
        }
        // Run the task in a separate thread
        executor.submit(() -> {
            try {
                List<AccountEvent> events = AccountEventDataGenerator.generateEvents();
                log.info("Generated {} events", events.size());

                // Split into batches of 50 (or any suitable size)
                int batchSize = 100;
                for (int i = 0; i < events.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, events.size());
                    List<AccountEvent> batch = events.subList(i, end);
                    executor.submit(() -> sendBatch(batch));
                }

            } catch (Exception e) {
                log.error("Error generating/sending events", e);
            }
        });
    }

    private void sendBatch(List<AccountEvent> batch) {
        try {
            eventProducer.sendMessage(batch);
            log.info("Sent batch of {} events", batch.size());
        } catch (Exception e) {
            log.error("Failed to send batch", e);
        }
    }
}
