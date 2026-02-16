package ao.gov.embaixada.gpj.exception;

public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(double capacity, double requested) {
        super("Sprint capacity exceeded: capacity=" + capacity + "h, requested=" + requested + "h");
    }
}
