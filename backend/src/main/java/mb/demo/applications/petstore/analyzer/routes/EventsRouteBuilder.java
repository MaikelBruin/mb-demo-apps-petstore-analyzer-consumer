package mb.demo.applications.petstore.analyzer.routes;

import mb.demo.applications.petstore.analyzer.service.EventsService;
import mb.demo.applications.petstore.analyzer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.EventRequestBody;
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
        from(RouteBuilderConstants.DIRECT_ROUTE_PROCESS_EVENT)
                .routeId(RouteBuilderConstants.DIRECT_ROUTE_PROCESS_EVENT + "Id")
                .process(exchange -> {
                    EventAcceptedResponse response = eventsService.processEvent(new EventRequestBody());
                    exchange.getMessage().setBody(response);
                });
    }
}
