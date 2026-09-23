package mb.demo.applications.petstore.analyzer.integrated.bootstrap;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;
import mb.demo.applications.petstore.analyzer.PetstoreAnalyzerConsumerApplication;
import mb.demo.applications.petstore.analyzer.integrated.IntegratedTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = {PetstoreAnalyzerConsumerApplication.class, IntegratedTestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "test", "cucumber"})
@Slf4j
public class IntegratedContainerBootstrapper {
}
