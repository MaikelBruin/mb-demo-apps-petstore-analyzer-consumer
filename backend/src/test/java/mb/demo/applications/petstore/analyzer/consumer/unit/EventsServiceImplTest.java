package mb.demo.applications.petstore.analyzer.consumer.unit;

import mb.demo.applications.petstore.analyzer.consumer.service.EventsService;
import mb.demo.applications.petstore.analyzer.consumer.service.InvalidEventException;
import mb.demo.applications.petstore.analyzer.consumer.service.impl.EventsServiceImpl;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventRequestBody;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EventsServiceImplTest {

    private final EventsService eventsService = new EventsServiceImpl();

    private static EventRequestBody event(final String eventType, final Object payload) {
        return new EventRequestBody().eventType(eventType).payload(payload);
    }

    @Test
    public void acceptsAnEventWithAnEventTypeAndANonEmptyPayload() {
        EventAcceptedResponse response = eventsService.processEvent(event("ratio", Map.of("petId", 947)));

        assertThat(response.getEventId()).isNotNull();
        assertThat(response.getReceivedAt()).isNotNull();
    }

    @Test
    public void givesEveryAcceptedEventItsOwnId() {
        EventAcceptedResponse first = eventsService.processEvent(event("ratio", Map.of("petId", 947)));
        EventAcceptedResponse second = eventsService.processEvent(event("ratio", Map.of("petId", 947)));

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }

    @Test
    public void rejectsAnEventWithoutAnEventType() {
        assertThatThrownBy(() -> eventsService.processEvent(event(null, Map.of("petId", 947))))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    public void rejectsAnEventWithABlankEventType() {
        assertThatThrownBy(() -> eventsService.processEvent(event("   ", Map.of("petId", 947))))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    public void rejectsAnEventWithoutAPayload() {
        assertThatThrownBy(() -> eventsService.processEvent(event("ratio", null)))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    public void rejectsAnEventWithAnEmptyPayload() {
        assertThatThrownBy(() -> eventsService.processEvent(event("ratio", Collections.emptyMap())))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    public void rejectsAnEventWhosePayloadIsNotAnObject() {
        assertThatThrownBy(() -> eventsService.processEvent(event("ratio", "not an object")))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    public void rejectsAnEventThatIsAbsentAltogether() {
        assertThatThrownBy(() -> eventsService.processEvent(null))
                .isInstanceOf(InvalidEventException.class);
    }
}
