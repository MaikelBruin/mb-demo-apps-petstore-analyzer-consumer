package mb.demo.applications.petstore.analyzer.consumer.controllers;

import mb.demo.applications.petstore.analyzer.consumer.routes.RouteBuilderConstants;
import mb.demo.applications.petstore.analyzer.consumer.webapi.api.EventsApi;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventRequestBody;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<EventAcceptedResponse> processEvent(final EventRequestBody eventRequestBody) {
        final EventAcceptedResponse eventAcceptedResponse = producerTemplate.requestBody(RouteBuilderConstants.DIRECT_ROUTE_PROCESS_EVENT, eventRequestBody, EventAcceptedResponse.class);
        return ResponseEntity.accepted().body(eventAcceptedResponse);
    }
}
