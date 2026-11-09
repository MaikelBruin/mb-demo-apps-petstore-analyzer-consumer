package mb.demo.applications.petstore.analyzer.consumer.base;

import io.cucumber.spring.ScenarioScope;
import mb.demo.applications.petstore.analyzer.consumer.base.cucumber.TestDataHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Random;
import java.util.random.RandomGenerator;

@Configuration
public abstract class BaseTestConfiguration {

    /**
     * Keeps the demo teapot out of the scenarios: the draw never comes up zero.
     */
    @Bean
    @Primary
    public RandomGenerator neverATeapotRandomGenerator() {
        return new Random() {
            @Override
            public int nextInt(final int bound) {
                return bound - 1;
            }
        };
    }

    @ScenarioScope
    @Bean
    public TestDataHolder testDataHolder() {
        return new TestDataHolder();
    }

}
