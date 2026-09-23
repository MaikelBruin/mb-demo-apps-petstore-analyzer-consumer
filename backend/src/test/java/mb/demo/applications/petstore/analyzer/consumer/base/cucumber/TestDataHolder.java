package mb.demo.applications.petstore.analyzer.consumer.base.cucumber;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
@Getter
@Setter
public class TestDataHolder {

    /**
     * The raw response of the last call, kept as a String body so that both the accepted and the
     * error payloads can be inspected by the Then steps.
     */
    private ResponseEntity<String> response;
}
