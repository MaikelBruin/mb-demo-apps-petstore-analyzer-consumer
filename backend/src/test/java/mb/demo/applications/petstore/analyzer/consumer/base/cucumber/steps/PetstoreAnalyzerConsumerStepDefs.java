package mb.demo.applications.petstore.analyzer.consumer.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.consumer.base.cucumber.TestDataHolder;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.ErrorResponse;
import mb.demo.applications.petstore.analyzer.consumer.webapi.model.EventAcceptedResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PetstoreAnalyzerConsumerStepDefs extends BaseCucumberStepDefs {

    private static final String PROCESS_EVENT_URI = "/api/process/event";

    public PetstoreAnalyzerConsumerStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
    }

    @When("I receive an event that includes an eventType and payload")
    public void iReceiveAnEventThatIncludesAnEventTypeAndPayload() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ratio");
        event.put("payload", Map.of("petId", 947, "status", "available"));
        processEvent(event);
    }

    @When("I receive an event missing an eventType")
    public void iReceiveAnEventMissingAnEventType() {
        Map<String, Object> event = new HashMap<>();
        event.put("payload", Map.of("petId", 947, "status", "available"));
        processEvent(event);
    }

    @When("I receive an event missing a payload")
    public void iReceiveAnEventMissingAPayload() {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "ratio");
        processEvent(event);
    }

    @Then("I expect a {string} response")
    public void iExpectAResponse(String expectedStatus) {
        ResponseEntity<String> response = testDataHolder.getResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(statusWithReasonPhrase(expectedStatus));
    }

    @SneakyThrows
    @And("the response should include an id")
    public void theResponseShouldIncludeAnId() {
        EventAcceptedResponse acceptedResponse = objectMapper.readValue(testDataHolder.getResponse().getBody(), EventAcceptedResponse.class);
        assertThat(acceptedResponse.getEventId()).isNotNull();
    }

    @SneakyThrows
    @And("the response should include an error message")
    public void theResponseShouldIncludeAnErrorMessage() {
        ErrorResponse errorResponse = objectMapper.readValue(testDataHolder.getResponse().getBody(), ErrorResponse.class);
        assertThat(errorResponse.getMessage()).isNotBlank();
    }

    private void processEvent(final Map<String, Object> event) {
        log.info("processing event '{}'...", event);
        testDataHolder.setResponse(testRestTemplate.postForEntity(PROCESS_EVENT_URI, event, String.class));
    }

    /**
     * Resolves the reason phrase used in the feature file, e.g. "Bad Request", to its status.
     */
    private static HttpStatusCode statusWithReasonPhrase(final String reasonPhrase) {
        return Arrays.stream(HttpStatus.values())
                .filter(httpStatus -> httpStatus.getReasonPhrase().equalsIgnoreCase(reasonPhrase))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no http status with reason phrase '" + reasonPhrase + "'"));
    }
}
