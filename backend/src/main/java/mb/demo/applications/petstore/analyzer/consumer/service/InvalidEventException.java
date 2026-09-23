package mb.demo.applications.petstore.analyzer.consumer.service;

/**
 * Thrown when an incoming event does not meet the requirements to be accepted for processing.
 */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(final String message) {
        super(message);
    }
}
