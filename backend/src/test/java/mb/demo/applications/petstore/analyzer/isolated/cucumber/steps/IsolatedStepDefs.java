package mb.demo.applications.petstore.analyzer.isolated.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.base.cucumber.TestDataHolder;
import mb.demo.applications.petstore.analyzer.base.cucumber.steps.BaseCucumberStepDefs;
import mb.demos.openapi.generated.api.client.petstore.api.PetApiClient;
import mb.demos.openapi.generated.api.client.petstore.client.ApiResponse;
import mb.demos.openapi.generated.api.client.petstore.model.Pet;
import mb.demos.openapi.generated.api.client.petstore.model.Tag;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.mockito.Mockito;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class IsolatedStepDefs extends BaseCucumberStepDefs {
    private final PetApiClient petApiClient;

    public IsolatedStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate, final PetApiClient petApiClient) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
        this.petApiClient = petApiClient;
    }

    @Given("I have access to the mocked petstore")
    public void iHaveAccessToTheMockedPetstore() {
        log.info("I have access to the mocked petstore");
        assertThat(petApiClient).isNotNull();
        boolean isSpy = Mockito.mockingDetails(petApiClient).isSpy();
        assertThat(isSpy).isTrue();
    }

    @SneakyThrows
    @Given("I configured the petstore find pets by status {string} response to return an empty array")
    public void iConfiguredThePetstoreFindPetsByStatusResponseToReturnAnEmptyArray(String inputStatus) {
        Mockito.when(petApiClient.findPetsByStatusWithHttpInfo(inputStatus)).thenReturn(new ApiResponse<>(200, new HashMap<>(), Collections.emptyList()));
    }

    @SneakyThrows
    @Given("I configured the petstore find pets by tags {string} response to return an empty array")
    public void iConfiguredThePetstoreFindPetsByTagsResponseToReturnAnEmptyArray(String tags) {
        List<String> tagList = Arrays.stream(tags.split(",")).toList();
        Mockito.when(petApiClient.findPetsByTagsWithHttpInfo(tagList)).thenReturn(new ApiResponse<>(200, new HashMap<>(), Collections.emptyList()));
    }

    @SneakyThrows
    @Given("I configured the petstore find pets by tags {string} response to return a non-empty array")
    public void iConfiguredThePetstoreFindPetsByTagsResponseToReturnANonEmptyArray(String tags) {
        List<String> tagList = Arrays.stream(tags.split(",")).toList();
        Pet rat = new Pet()
                .tags(Collections.singletonList(new Tag().id(1L).name("rat")))
                .name("scruffy")
                .id(947L)
                .status(Pet.StatusEnum.AVAILABLE);
        Mockito.when(petApiClient.findPetsByTagsWithHttpInfo(tagList)).thenReturn(new ApiResponse<>(200, new HashMap<>(), Collections.singletonList(rat)));
    }
}
