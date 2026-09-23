package mb.demo.applications.petstore.analyzer.consumer.routes;

import mb.demo.applications.petstore.analyzer.consumer.service.EventsService;
import mb.demo.applications.petstore.analyzer.consumer.service.InvalidEventException;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventRequestBody;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

@Component
public class EventsRouteBuilder extends BaseRouteBuilder {

    private final EventsService eventsService;

    public EventsRouteBuilder(final CamelContext context, final EventsService eventsService) {
        super(context);
        this.eventsService = eventsService;
    }

    @Override
    public void configure() {
        // a rejected event is an expected outcome, so it is not logged as a delivery failure. It is
        // left unhandled on purpose, so that it still surfaces to the caller as a bad request.
        onException(InvalidEventException.class)
                .logStackTrace(false)
                .logExhausted(false)
                .handled(false);

        from(RouteBuilderConstants.DIRECT_ROUTE_PROCESS_EVENT)
                .routeId(RouteBuilderConstants.DIRECT_ROUTE_PROCESS_EVENT + "Id")
                .process(exchange -> {
                    EventRequestBody requestBody = exchange.getMessage().getBody(EventRequestBody.class);
                    EventAcceptedResponse response = eventsService.processEvent(requestBody);
                    exchange.getMessage().setBody(response);
                });
    }
}
