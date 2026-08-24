package hospital.service;

/** Thrown when attempting to register a patient with an ID that already exists. */
public class DuplicatePatientException extends Exception {
    public DuplicatePatientException(String message) {
        super(message);
    }
}
