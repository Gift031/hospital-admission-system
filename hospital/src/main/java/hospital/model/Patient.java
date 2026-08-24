package hospital.model;

/**
 * Base class representing a hospital patient.
 * Outpatients and Emergency patients are represented directly by this class.
 * Inpatients are represented by the {@link Inpatient} subclass, which adds
 * ward and bed information.
 */
public class Patient {

    private String patientId;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String medicalCondition;
    private PatientCategory category;

    public Patient(String patientId, String firstName, String lastName, int age,
                   String gender, String medicalCondition, PatientCategory category) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.gender = gender;
        this.medicalCondition = medicalCondition;
        this.category = category;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMedicalCondition() {
        return medicalCondition;
    }

    public void setMedicalCondition(String medicalCondition) {
        this.medicalCondition = medicalCondition;
    }

    public PatientCategory getCategory() {
        return category;
    }

    public void setCategory(PatientCategory category) {
        this.category = category;
    }

    /**
     * Returns a human-readable summary of the patient's details.
     * Subclasses override this to add extra information.
     */
    public String displayDetails() {
        return String.format(
                "ID: %-8s Name: %-20s Age: %-4d Gender: %-8s Category: %-11s Condition: %s",
                patientId, (firstName + " " + lastName), age, gender, category, medicalCondition);
    }

    @Override
    public String toString() {
        return displayDetails();
    }
}
