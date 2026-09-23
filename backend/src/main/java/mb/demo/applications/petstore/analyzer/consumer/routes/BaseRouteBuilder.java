package mb.demo.applications.petstore.analyzer.consumer.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public abstract class BaseRouteBuilder extends RouteBuilder {

    public BaseRouteBuilder(final CamelContext context) {
        super(context);
    }
}
