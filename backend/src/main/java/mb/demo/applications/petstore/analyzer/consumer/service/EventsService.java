package mb.demo.applications.petstore.analyzer.consumer.service;

import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventRequestBody;

public interface EventsService {

    /**
     * Accepts an event for processing.
     *
     * @param requestBody the incoming event
     * @return the acknowledgement of the accepted event
     * @throws InvalidEventException when the event does not carry both a non-empty eventType and a
     *                               non-empty payload object
     */
    EventAcceptedResponse processEvent(EventRequestBody requestBody);
}
