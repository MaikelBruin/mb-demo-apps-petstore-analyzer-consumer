package mb.demo.applications.petstore.analyzer.consumer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info=@Info(title="Petstore Analyzer Consumer API", description = "API that consumes events from suppliers"))
@SpringBootApplication
public class PetstoreAnalyzerConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetstoreAnalyzerConsumerApplication.class, args);
	}

}
