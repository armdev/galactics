package io.project.receiver.controllers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * KafkaStreamMvcController
 *
 * This controller exposes an HTTP endpoint (/api/v2/data/stream) that streams messages
 * from a Kafka topic to clients in real time using Server-Sent Events (SSE).
 *
 * - Clients connect to the /stream endpoint and remain connected.
 * - Messages arriving from Kafka are immediately pushed to all connected clients.
 *
 * This implementation uses Spring MVC + SseEmitter.
 */
@RestController
@RequestMapping("/api/v2/data")
public class KafkaStreamMvcController {

    /**
     * Thread-safe list of active client connections (SseEmitters).
     * CopyOnWriteArrayList is used because multiple threads may add/remove emitters
     * while broadcasting messages, and it avoids ConcurrentModificationException.
     */
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * REST endpoint for clients to subscribe to real-time Kafka messages.
     *
     * - Produces an infinite SSE stream.
     * - When a client connects, we create a new SseEmitter and store it in the emitters list.
     * - The connection stays open until the client disconnects or a timeout occurs.
     *
     * Example: curl -N http://localhost:8080/api/v2/data/stream
     * @return 
     */
    @GetMapping("/stream")
    public SseEmitter stream() {
        // Create an SSE connection that never times out (or until client disconnects)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Add this emitter to the active connections list
        emitters.add(emitter);

        // Remove the emitter when the client closes the connection normally
        emitter.onCompletion(() -> emitters.remove(emitter));

        // Remove the emitter when the client disconnects due to timeout
        emitter.onTimeout(() -> emitters.remove(emitter));

        // Return the emitter, keeping the HTTP connection open
        return emitter;
    }

    /**
     * KafkaConsumerService
     *
     * This inner service listens to a Kafka topic and forwards messages
     * to all connected SSE clients.
     *
     * - The @KafkaListener annotation consumes messages from the "evn-transfer" topic.
     * - Each received message is broadcast to all currently connected emitters.
     */
    @Service
    public class KafkaConsumerService {

        /**
         * Kafka consumer method.
         *
         * @param message The message payload consumed from Kafka.
         */
        @KafkaListener(topics = "evn-transfer", groupId = "mvc-receiver")
        public void consume(String message) {
            // Iterate over all connected clients
            for (SseEmitter emitter : emitters) {
                try {
                    // Send the message as an SSE event
                    emitter.send(SseEmitter.event().data(message));
                } catch (IOException e) {
                    // If sending fails (e.g., client disconnected), remove the emitter
                    emitters.remove(emitter);
                }
            }
        }
    }
}
