package hospital.model;

/**
 * Represents a patient admitted to the ward and occupying a bed.
 * Extends {@link Patient} with ward and bed information.
 */
public class Inpatient extends Patient {

    private static final int WARD_NUMBER = 1;

    private int wardNumber;
    private String bedNumber;

    public Inpatient(String patientId, String firstName, String lastName, int age,
                      String gender, String medicalCondition) {
        super(patientId, firstName, lastName, age, gender, medicalCondition, PatientCategory.INPATIENT);
        this.wardNumber = WARD_NUMBER;
        this.bedNumber = null;
    }

    public int getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(int wardNumber) {
        this.wardNumber = wardNumber;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    @Override
    public String displayDetails() {
        String bedInfo = (bedNumber == null) ? "Unassigned" : bedNumber;
        return super.displayDetails() +
                String.format("  Ward: %-3d Bed: %s", wardNumber, bedInfo);
    }
}
