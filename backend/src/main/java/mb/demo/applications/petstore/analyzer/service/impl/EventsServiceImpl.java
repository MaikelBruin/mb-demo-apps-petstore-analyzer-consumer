package mb.demo.applications.petstore.analyzer.service.impl;

import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.service.EventsService;
import mb.demo.applications.petstore.analyzer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.EventRequestBody;
import mb.demos.openapi.generated.api.client.petstore.client.ApiException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventsServiceImpl implements EventsService {


    @Override
    public EventAcceptedResponse processEvent(EventRequestBody requestBody) throws ApiException, InterruptedException {
        return null;
    }
}