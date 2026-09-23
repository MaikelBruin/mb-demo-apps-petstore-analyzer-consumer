package mb.demo.applications.petstore.analyzer.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.base.cucumber.TestDataHolder;
import mb.demo.applications.petstore.analyzer.webapi.model.EventAcceptedResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.EventRequestBody;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class PetstoreAnalyzerConsumerStepDefs extends BaseCucumberStepDefs {

    public PetstoreAnalyzerConsumerStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
    }

    @When("I receive an event that includes an eventType and payload")
    public void iReceiveAnEventThatIncludesAnEventTypeAndPayload() {
        log.info("receiving event...");
        String fullUri = "/api/process/event";
        EventRequestBody request = new EventRequestBody().eventType("eventType").payload(new HashMap<>());
        EventAcceptedResponse response = testRestTemplate.postForObject(fullUri, request, EventAcceptedResponse.class);
        testDataHolder.setEventAcceptedResponse(response);
    }

    @Then("I expect an {string} result")
    public void iExpectAnResult(String expectedResponse) {
        assertThat(testDataHolder.getEventAcceptedResponse()).isNotNull();
    }
}
