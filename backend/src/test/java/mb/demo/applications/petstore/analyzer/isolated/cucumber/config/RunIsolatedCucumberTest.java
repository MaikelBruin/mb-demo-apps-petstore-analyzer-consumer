package mb.demo.applications.petstore.analyzer.isolated.cucumber.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * A configuration for cucumber test setups.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/isolated")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "mb.demo.applications.petstore.analyzer.base,mb.demo.applications.petstore.analyzer.isolated")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@Slf4j
public class RunIsolatedCucumberTest {

}
