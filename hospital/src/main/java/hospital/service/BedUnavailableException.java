package hospital.service;

/** Thrown when a bed cannot be allocated (already occupied, invalid, or none free). */
public class BedUnavailableException extends Exception {
    public BedUnavailableException(String message) {
        super(message);
    }
}
