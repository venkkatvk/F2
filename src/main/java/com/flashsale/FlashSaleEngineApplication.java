package com.flashsale.telemetry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/telemetry")
public class TelemetrySseController {

    // A thread-safe ledger of all the peasants (browsers) currently listening
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // The endpoint where browsers connect to listen
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        // Set timeout to Long.MAX_VALUE to keep the connection open indefinitely
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        // Crucial: Clean up the ledger if a peasant walks away or dies!
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // This method is called by our internal metrics engine whenever a stat changes
    public void broadcastMetrics(String jsonPayload) {
        for (SseEmitter emitter : emitters) {
            try {
                // The Herald shouts the update!
                emitter.send(SseEmitter.event().name("telemetry").data(jsonPayload));
            } catch (IOException e) {
                // The peasant's connection broke. Remove them.
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
