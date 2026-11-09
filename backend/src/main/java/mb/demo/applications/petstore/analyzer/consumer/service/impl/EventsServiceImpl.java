package mb.demo.applications.petstore.analyzer.consumer.service.impl;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.consumer.service.EventsService;
import mb.demo.applications.petstore.analyzer.consumer.service.InvalidEventException;
import mb.demo.applications.petstore.analyzer.consumer.service.TeapotException;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventRequestBody;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Slf4j
@Service
public class EventsServiceImpl implements EventsService {

    private static final int TEAPOT_ODDS = 100;

    private final RandomGenerator randomGenerator;

    public EventsServiceImpl(final RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    /**
     * An event is only accepted when it carries both a non-empty eventType and a non-empty payload
     * object.
     */
    private static void validate(final EventRequestBody requestBody) {
        if (requestBody == null) {
            throw new InvalidEventException("event is missing");
        }
        if (requestBody.getEventType() == null || requestBody.getEventType().isBlank()) {
            throw new InvalidEventException("event is missing a non-empty eventType");
        }
        if (!(requestBody.getPayload() instanceof Map<?, ?> payload) || payload.isEmpty()) {
            throw new InvalidEventException("event is missing a non-empty payload object");
        }
    }

    @Override
    public EventAcceptedResponse processEvent(final EventRequestBody requestBody) {
        // for demo purposes, one in a hundred requests is answered with a teapot
        if (randomGenerator.nextInt(TEAPOT_ODDS) == 0) {
            throw new TeapotException();
        }
        validate(requestBody);
        log.info("accepting event of type '{}'...", requestBody.getEventType());
        return new EventAcceptedResponse()
                .eventId(UUID.randomUUID())
                .receivedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
