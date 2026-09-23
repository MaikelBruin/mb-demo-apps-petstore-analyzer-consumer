package mb.demo.applications.petstore.analyzer.consumer.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.consumer.base.cucumber.TestDataHolder;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

@Slf4j
public abstract class BaseCucumberStepDefs {
    protected final CamelContext camelContext;
    protected final ProducerTemplate producerTemplate;
    protected final TestRestTemplate testRestTemplate;
    protected final TestDataHolder testDataHolder;
    protected final ObjectMapper objectMapper;

    public BaseCucumberStepDefs(final CamelContext camelContext, final ProducerTemplate producerTemplate, final TestDataHolder testDataHolder, final ObjectMapper objectMapper, final TestRestTemplate testRestTemplate) {
        this.camelContext = camelContext;
        this.producerTemplate = producerTemplate;
        this.testRestTemplate = testRestTemplate;
        this.testDataHolder = testDataHolder;
        this.objectMapper = objectMapper;
    }
}
