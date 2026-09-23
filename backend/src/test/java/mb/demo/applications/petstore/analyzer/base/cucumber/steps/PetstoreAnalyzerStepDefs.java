package mb.demo.applications.petstore.analyzer.base.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.base.cucumber.TestDataHolder;
import mb.demo.applications.petstore.analyzer.webapi.model.AvailabilityRatioResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.HasAvailableResponse;
import mb.demo.applications.petstore.analyzer.webapi.model.TotalResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class PetstoreAnalyzerStepDefs extends BaseCucumberStepDefs {

    public PetstoreAnalyzerStepDefs(CamelContext camelContext, ProducerTemplate producerTemplate, TestDataHolder testDataHolder, ObjectMapper objectMapper, TestRestTemplate testRestTemplate) {
        super(camelContext, producerTemplate, testDataHolder, objectMapper, testRestTemplate);
    }

    @When("I get the total number of dogs")
    public void iGetTheTotalNumberOfDogs() {
        log.info("getting total number of dogs...");
        String fullUri = "/api/totals/dogs";
        TotalResponse response = testRestTemplate.getForObject(fullUri, TotalResponse.class);
        testDataHolder.setTotalDogsResponse(response);
    }

    @Then("the total number of dogs response should not be null")
    public void theTotalNumberOfDogsResponseShouldNotBeNull() {
        assertThat(testDataHolder.getTotalDogsResponse()).isNotNull();
    }

    @When("I get the total number of cats")
    public void iGetTheTotalNumberOfCats() {
        log.info("getting total number of cats...");
        String fullUri = "/api/totals/cats";
        TotalResponse response = testRestTemplate.getForObject(fullUri, TotalResponse.class);
        testDataHolder.setTotalCatsResponse(response);
    }

    @Then("the total number of cats response should not be null")
    public void theTotalNumberOfCatsResponseShouldNotBeNull() {
        assertThat(testDataHolder.getTotalCatsResponse()).isNotNull();
    }

    @And("the total number of dogs should be equal than or greater than {int}")
    public void theTotalNumberOfDogsShouldBeEqualThanOrGreaterThan(int minimumNumberOfDogsExpected) {
        assertThat(testDataHolder.getTotalDogsResponse().getTotal()).isGreaterThanOrEqualTo(minimumNumberOfDogsExpected);
    }

    @And("the total number of cats should be equal than or greater than {int}")
    public void theTotalNumberOfCatsShouldBeEqualThanOrGreaterThan(int minimumNumberOfCatsExpected) {
        assertThat(testDataHolder.getTotalCatsResponse().getTotal()).isGreaterThanOrEqualTo(minimumNumberOfCatsExpected);
    }

    @When("I get the total number of available pets")
    public void iGetTheTotalNumberOfAvailablePets() {
        log.info("getting total number of available pets...");
        String fullUri = "/api/totals/available";
        TotalResponse response = testRestTemplate.getForObject(fullUri, TotalResponse.class);
        testDataHolder.setTotalAvailablePetsResponse(response);
    }

    @Then("the total number of available pets response should not be null")
    public void theTotalNumberOfAvailablePetsResponseShouldNotBeNull() {
        assertThat(testDataHolder.getTotalAvailablePetsResponse()).isNotNull();
    }

    @And("the total number of available pets should be equal than or greater than {int}")
    public void theTotalNumberOfAvailablePetsShouldBeEqualThanOrGreaterThan(int minimumNumberOfAvailablePetsExpected) {
        assertThat(testDataHolder.getTotalAvailablePetsResponse().getTotal()).isGreaterThanOrEqualTo(minimumNumberOfAvailablePetsExpected);
    }

    @When("I get the total number of pets with tag {string}")
    public void iGetTheTotalNumberOfPetsWithTag(String tag) {
        log.info("getting total number of pets with tag '{}'...", tag);
        String fullUri = "/api/totals?tag=" + tag;
        TotalResponse response = testRestTemplate.getForObject(fullUri, TotalResponse.class);
        testDataHolder.setTotalPetsWithTagResponse(response);
    }

    @Then("the total number of pets with tag response should not be null")
    public void theTotalNumberOfPetsWithTagResponseShouldNotBeNull() {
        assertThat(testDataHolder.getTotalPetsWithTagResponse()).isNotNull();
    }

    @And("the total number of pets with tag should be equal than or greater than {int}")
    public void theTotalNumberOfPetsWithTagShouldBeEqualThanOrGreaterThan(int minimumNumberOfPetsWithTagExpected) {
        assertThat(testDataHolder.getTotalPetsWithTagResponse().getTotal()).isGreaterThanOrEqualTo(minimumNumberOfPetsWithTagExpected);
    }

    @When("I get the ratio of available pets vs sold pets")
    public void iGetTheRatioOfAvailablePetsVsSoldPets() {
        log.info("getting availability ratio...");
        String fullUri = "/api/ratios/availability";
        AvailabilityRatioResponse response = testRestTemplate.getForObject(fullUri, AvailabilityRatioResponse.class);
        testDataHolder.setAvailabilityRatioResponse(response);
    }

    @Then("the availability ratio response should not be null")
    public void theAvailabilityRatioResponseShouldNotBeNull() {
        assertThat(testDataHolder.getAvailabilityRatioResponse()).isNotNull();
    }

    @And("the availability ratio should be equal to the total available divided by the total sold")
    public void theAvailabilityRatioShouldBeEqualToTheTotalAvailableDividedByTheTotalSold() {
        Integer availablePets = testDataHolder.getAvailabilityRatioResponse().getAvailablePets();
        Integer soldPets = testDataHolder.getAvailabilityRatioResponse().getSoldPets();
        BigDecimal expected;
        if (soldPets == 0) {
            expected = BigDecimal.ZERO;
        } else {
            expected = BigDecimal.valueOf((float) availablePets / soldPets);
        }
        assertThat(testDataHolder.getAvailabilityRatioResponse().getRatio()).isEqualTo(expected);
    }


    @When("I get if there are any rats available")
    public void iGetIfThereAreAnyRatsAvailable() {
        log.info("checking if there are any rats available...");
        String fullUri = "/api/available/rats";
        HasAvailableResponse response = testRestTemplate.getForObject(fullUri, HasAvailableResponse.class);
        testDataHolder.setHasAvailableResponse(response);
    }

    @Then("the has available rats response should not be null")
    public void theHasAvailableRatsResponseShouldNotBeNull() {
        assertThat(testDataHolder.getHasAvailableResponse()).isNotNull();
    }

    @And("the has available rats response should say that there are {string} rats available")
    public void theHasAvailableRatsResponseShouldSayThatThereAreRatsAvailable(String no) {
        if (no.isEmpty()) assertThat(testDataHolder.getHasAvailableResponse().getHasAvailable()).isTrue();
        else assertThat(testDataHolder.getHasAvailableResponse().getHasAvailable()).isFalse();
    }
}
