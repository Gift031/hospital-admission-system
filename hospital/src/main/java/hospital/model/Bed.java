package hospital.model;

/**
 * Represents a single bed in the hospital ward.
 */
public class Bed {

    private final String bedNumber;
    private boolean occupied;
    private Inpatient occupant;

    public Bed(String bedNumber) {
        this.bedNumber = bedNumber;
        this.occupied = false;
        this.occupant = null;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Inpatient getOccupant() {
        return occupant;
    }

    public void occupy(Inpatient patient) {
        this.occupant = patient;
        this.occupied = true;
    }

    public void release() {
        this.occupant = null;
        this.occupied = false;
    }

    @Override
    public String toString() {
        if (occupied) {
            return String.format("%s [OCCUPIED by %s %s]", bedNumber,
                    occupant.getFirstName(), occupant.getLastName());
        }
        return String.format("%s [AVAILABLE]", bedNumber);
    }
}
