package mb.demo.applications.petstore.analyzer.isolated;

import mb.demo.applications.petstore.analyzer.base.BaseTestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("isolated")
@Configuration
@ComponentScan(basePackages = "mb.demos.openapi.generated.api.client")
public class IsolatedTestConfiguration extends BaseTestConfiguration {

}
