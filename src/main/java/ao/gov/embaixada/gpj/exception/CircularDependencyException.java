package ao.gov.embaixada.gpj.exception;

public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException() {
        super("Circular dependency detected between tasks");
    }
}
