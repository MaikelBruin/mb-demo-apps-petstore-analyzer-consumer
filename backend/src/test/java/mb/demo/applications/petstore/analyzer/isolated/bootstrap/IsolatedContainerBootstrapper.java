package mb.demo.applications.petstore.analyzer.isolated.bootstrap;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.PetstoreAnalyzerConsumerApplication;
import mb.demo.applications.petstore.analyzer.isolated.IsolatedTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = {PetstoreAnalyzerConsumerApplication.class, IsolatedTestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "isolated", "test", "cucumber"})
@Slf4j
public class IsolatedContainerBootstrapper {

}
