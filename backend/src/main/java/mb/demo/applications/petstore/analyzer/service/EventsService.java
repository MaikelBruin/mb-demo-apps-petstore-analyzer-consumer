package mb.demo.applications.petstore.analyzer.service;

import mb.demo.applications.petstore.analyzer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.EventRequestBody;
import mb.demos.openapi.generated.api.client.petstore.client.ApiException;

public interface EventsService {
    EventAcceptedResponse processEvent(EventRequestBody requestBody) throws ApiException, InterruptedException;
}
