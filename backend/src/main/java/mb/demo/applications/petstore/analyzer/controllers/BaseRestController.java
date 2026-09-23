package mb.demo.applications.petstore.analyzer.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class BaseRestController {

    @Autowired
    protected ProducerTemplate producerTemplate;

    public BaseRestController(final ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }
}
