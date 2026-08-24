package hospital.service;

import hospital.model.Bed;
import hospital.model.Inpatient;
import hospital.model.Patient;
import hospital.model.PatientCategory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business logic for the Hospital Patient Admission System.
 * Manages patient records and the 20-bed ward (4 x 5 layout).
 */
public class HospitalService {

    public static final int WARD_ROWS = 4;
    public static final int WARD_COLS = 5;
    public static final int TOTAL_BEDS = WARD_ROWS * WARD_COLS;

    private final Map<String, Patient> patients = new LinkedHashMap<>();
    private final List<Bed> beds = new ArrayList<>();

    public HospitalService() {
        initialiseBeds();
    }

    private void initialiseBeds() {
        for (int i = 1; i <= TOTAL_BEDS; i++) {
            String bedNumber = String.format("B%02d", i);
            beds.add(new Bed(bedNumber));
        }
    }

    // ---------------------------------------------------------------
    // Feature 1: Patient Management
    // ---------------------------------------------------------------

    public Patient registerPatient(Patient patient) throws DuplicatePatientException {
        if (patients.containsKey(patient.getPatientId())) {
            throw new DuplicatePatientException(
                    "A patient with ID " + patient.getPatientId() + " already exists.");
        }
        patients.put(patient.getPatientId(), patient);
        return patient;
    }

    public Patient searchPatient(String patientId) {
        return patients.get(patientId);
    }

    public boolean updatePatient(String patientId, String firstName, String lastName, int age,
                                  String gender, String medicalCondition) {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            return false;
        }
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setMedicalCondition(medicalCondition);
        return true;
    }

    public boolean deletePatient(String patientId) {
        Patient patient = patients.get(patientId);
        if (patient == null) {
            return false;
        }
        // If the patient is an inpatient occupying a bed, free that bed first.
        if (patient instanceof Inpatient inpatient && inpatient.getBedNumber() != null) {
            findBed(inpatient.getBedNumber()).ifPresent(Bed::release);
        }
        patients.remove(patientId);
        return true;
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    public List<Patient> getPatientsSortedBySurname() {
        List<Patient> list = getAllPatients();
        list.sort(Comparator.comparing(Patient::getLastName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<Patient> getPatientsSortedById() {
        List<Patient> list = getAllPatients();
        list.sort(Comparator.comparing(Patient::getPatientId));
        return list;
    }

    public int getTotalPatients() {
        return patients.size();
    }

    // ---------------------------------------------------------------
    // Feature 2: Bed Management
    // ---------------------------------------------------------------

    public List<Bed> getWardLayout() {
        return new ArrayList<>(beds);
    }

    public List<Bed> getAvailableBeds() {
        List<Bed> available = new ArrayList<>();
        for (Bed bed : beds) {
            if (!bed.isOccupied()) {
                available.add(bed);
            }
        }
        return available;
    }

    public List<Bed> getOccupiedBeds() {
        List<Bed> occupied = new ArrayList<>();
        for (Bed bed : beds) {
            if (bed.isOccupied()) {
                occupied.add(bed);
            }
        }
        return occupied;
    }

    private java.util.Optional<Bed> findBed(String bedNumber) {
        return beds.stream().filter(b -> b.getBedNumber().equalsIgnoreCase(bedNumber)).findFirst();
    }

    /**
     * Allocates a specific bed to an inpatient.
     * Only patients categorised as INPATIENT may be allocated a bed.
     */
    public void allocateBed(String bedNumber, Inpatient patient) throws BedUnavailableException {
        if (patient.getCategory() != PatientCategory.INPATIENT) {
            throw new BedUnavailableException("Only inpatients may be allocated a hospital bed.");
        }
        if (getAvailableBeds().isEmpty()) {
            throw new BedUnavailableException("No beds are available in the ward.");
        }
        Bed bed = findBed(bedNumber)
                .orElseThrow(() -> new BedUnavailableException("Bed " + bedNumber + " does not exist."));
        if (bed.isOccupied()) {
            throw new BedUnavailableException("Bed " + bedNumber + " is already occupied.");
        }
        bed.occupy(patient);
        patient.setBedNumber(bedNumber);
    }

    /** Allocates the next available bed automatically. */
    public Bed allocateNextAvailableBed(Inpatient patient) throws BedUnavailableException {
        List<Bed> available = getAvailableBeds();
        if (available.isEmpty()) {
            throw new BedUnavailableException("No beds are available in the ward.");
        }
        Bed bed = available.get(0);
        allocateBed(bed.getBedNumber(), patient);
        return bed;
    }

    public void releaseBed(String bedNumber) throws BedUnavailableException {
        Bed bed = findBed(bedNumber)
                .orElseThrow(() -> new BedUnavailableException("Bed " + bedNumber + " does not exist."));
        if (!bed.isOccupied()) {
            throw new BedUnavailableException("Bed " + bedNumber + " is already unoccupied.");
        }
        Inpatient occupant = bed.getOccupant();
        if (occupant != null) {
            occupant.setBedNumber(null);
        }
        bed.release();
    }

    // ---------------------------------------------------------------
    // Feature 3: Reports
    // ---------------------------------------------------------------

    public int getTotalOccupiedBeds() {
        return getOccupiedBeds().size();
    }

    public double getOccupancyPercentage() {
        return (getTotalOccupiedBeds() * 100.0) / TOTAL_BEDS;
    }
}
