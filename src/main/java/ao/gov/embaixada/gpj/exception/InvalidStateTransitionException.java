package ao.gov.embaixada.gpj.exception;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String entityType, String from, String to) {
        super("Invalid " + entityType + " state transition: " + from + " → " + to);
    }
}
