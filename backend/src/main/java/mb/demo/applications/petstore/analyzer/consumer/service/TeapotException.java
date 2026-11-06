package mb.demo.applications.petstore.analyzer.consumer.service;

/**
 * Thrown on a random one-in-a-hundred basis, for demo purposes only.
 */
public class TeapotException extends RuntimeException {

    public TeapotException() {
        super("I'm a teapot");
    }
}
