package mb.demo.applications.petstore.analyzer.controllers;

import mb.demo.applications.petstore.analyzer.webapi.api.EventsApi;
import mb.demo.applications.petstore.analyzer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.EventRequestBody;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
public class EventsRestController extends BaseRestController implements EventsApi {

    public EventsRestController(final ProducerTemplate producerTemplate) {
        super(producerTemplate);
    }

    @Override
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/api/process/event",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    public ResponseEntity<EventAcceptedResponse> processEvent(EventRequestBody eventRequestBody) {
        if (eventRequestBody == null || eventRequestBody.getEventType() == null || eventRequestBody.getEventType().isEmpty() ||
                eventRequestBody.getPayload() == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.accepted().body(new EventAcceptedResponse().eventId(UUID.randomUUID()).receivedAt(OffsetDateTime.now()));

    }
}
