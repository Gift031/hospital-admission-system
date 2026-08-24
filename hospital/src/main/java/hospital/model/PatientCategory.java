package hospital.model;

/**
 * Represents the three categories of patient the hospital treats.
 * Only INPATIENT may be allocated a hospital bed.
 */
public enum PatientCategory {
    INPATIENT,
    OUTPATIENT,
    EMERGENCY
}
