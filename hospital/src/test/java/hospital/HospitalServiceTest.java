package hospital;

import hospital.model.Bed;
import hospital.model.Inpatient;
import hospital.model.Patient;
import hospital.model.PatientCategory;
import hospital.service.BedUnavailableException;
import hospital.service.DuplicatePatientException;
import hospital.service.HospitalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HospitalServiceTest {

    private HospitalService service;

    @BeforeEach
    void setUp() {
        service = new HospitalService();
    }

    // -----------------------------------------------------------
    // Register a patient
    // -----------------------------------------------------------
    @Test
    void testRegisterPatient() throws DuplicatePatientException {
        Patient patient = new Patient("P001", "Jane", "Doe", 30, "Female", "Flu", PatientCategory.OUTPATIENT);
        service.registerPatient(patient);

        assertEquals(1, service.getTotalPatients());
        assertEquals(patient, service.searchPatient("P001"));
    }

    // -----------------------------------------------------------
    // Search for a patient
    // -----------------------------------------------------------
    @Test
    void testSearchPatientFound() throws DuplicatePatientException {
        Patient patient = new Patient("P002", "John", "Smith", 45, "Male", "Fever", PatientCategory.EMERGENCY);
        service.registerPatient(patient);

        Patient found = service.searchPatient("P002");
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
    }

    @Test
    void testSearchPatientNotFound() {
        assertNull(service.searchPatient("NOTHERE"));
    }

    // -----------------------------------------------------------
    // Update patient details
    // -----------------------------------------------------------
    @Test
    void testUpdatePatientDetails() throws DuplicatePatientException {
        Patient patient = new Patient("P003", "Alice", "Brown", 22, "Female", "Cold", PatientCategory.OUTPATIENT);
        service.registerPatient(patient);

        boolean updated = service.updatePatient("P003", "Alicia", "Brown", 23, "Female", "Recovered");

        assertTrue(updated);
        Patient result = service.searchPatient("P003");
        assertEquals("Alicia", result.getFirstName());
        assertEquals(23, result.getAge());
        assertEquals("Recovered", result.getMedicalCondition());
    }

    @Test
    void testUpdateNonExistentPatientFails() {
        boolean updated = service.updatePatient("NOTHERE", "A", "B", 1, "M", "None");
        assertFalse(updated);
    }

    // -----------------------------------------------------------
    // Delete a patient
    // -----------------------------------------------------------
    @Test
    void testDeletePatient() throws DuplicatePatientException {
        Patient patient = new Patient("P004", "Sam", "Green", 60, "Male", "Diabetes", PatientCategory.OUTPATIENT);
        service.registerPatient(patient);

        boolean deleted = service.deletePatient("P004");

        assertTrue(deleted);
        assertNull(service.searchPatient("P004"));
        assertEquals(0, service.getTotalPatients());
    }

    @Test
    void testDeleteNonExistentPatientFails() {
        assertFalse(service.deletePatient("NOTHERE"));
    }

    // -----------------------------------------------------------
    // Allocate a bed
    // -----------------------------------------------------------
    @Test
    void testAllocateBed() throws DuplicatePatientException, BedUnavailableException {
        Inpatient patient = new Inpatient("P005", "Nomsa", "Khumalo", 50, "Female", "Surgery recovery");
        service.registerPatient(patient);

        Bed bed = service.allocateNextAvailableBed(patient);

        assertTrue(bed.isOccupied());
        assertEquals("P005", bed.getOccupant().getPatientId());
        assertEquals(bed.getBedNumber(), patient.getBedNumber());
    }

    @Test
    void testOnlyInpatientsCanBeAllocatedBed() throws DuplicatePatientException {
        // Outpatients are not Inpatient instances, so allocation is not possible via the
        // Inpatient-only API. Verify allocateBed rejects a non-inpatient category defensively
        // by constructing an Inpatient with an Outpatient category is not possible by design;
        // instead we confirm the category guard on allocateBed directly.
        Inpatient fakeOutpatient = new Inpatient("P006", "Test", "Case", 20, "Male", "N/A");
        fakeOutpatient.setCategory(PatientCategory.OUTPATIENT);
        service.registerPatient(fakeOutpatient);

        assertThrows(BedUnavailableException.class, () -> service.allocateBed("B01", fakeOutpatient));
    }

    // -----------------------------------------------------------
    // Release a bed
    // -----------------------------------------------------------
    @Test
    void testReleaseBed() throws DuplicatePatientException, BedUnavailableException {
        Inpatient patient = new Inpatient("P007", "Peter", "Naidoo", 35, "Male", "Broken leg");
        service.registerPatient(patient);
        Bed bed = service.allocateNextAvailableBed(patient);

        service.releaseBed(bed.getBedNumber());

        assertFalse(bed.isOccupied());
        assertNull(patient.getBedNumber());
    }

    @Test
    void testReleaseAlreadyFreeBedThrows() {
        assertThrows(BedUnavailableException.class, () -> service.releaseBed("B01"));
    }

    // -----------------------------------------------------------
    // Prevent duplicate Patient IDs
    // -----------------------------------------------------------
    @Test
    void testPreventDuplicatePatientIds() throws DuplicatePatientException {
        Patient patient1 = new Patient("P008", "First", "Patient", 40, "Male", "A", PatientCategory.OUTPATIENT);
        Patient patient2 = new Patient("P008", "Second", "Patient", 41, "Female", "B", PatientCategory.OUTPATIENT);
        service.registerPatient(patient1);

        assertThrows(DuplicatePatientException.class, () -> service.registerPatient(patient2));
    }

    // -----------------------------------------------------------
    // Prevent allocating an occupied bed
    // -----------------------------------------------------------
    @Test
    void testPreventAllocatingOccupiedBed() throws DuplicatePatientException, BedUnavailableException {
        Inpatient patient1 = new Inpatient("P009", "A", "One", 30, "Male", "Cond1");
        Inpatient patient2 = new Inpatient("P010", "B", "Two", 31, "Female", "Cond2");
        service.registerPatient(patient1);
        service.registerPatient(patient2);

        service.allocateBed("B01", patient1);

        assertThrows(BedUnavailableException.class, () -> service.allocateBed("B01", patient2));
    }

    // -----------------------------------------------------------
    // Prevent bed allocation when all beds are occupied
    // -----------------------------------------------------------
    @Test
    void testPreventAllocationWhenWardFull() throws DuplicatePatientException, BedUnavailableException {
        // Fill all 20 beds
        for (int i = 1; i <= HospitalService.TOTAL_BEDS; i++) {
            Inpatient p = new Inpatient("F" + i, "Name" + i, "Surname" + i, 20 + i, "Male", "Condition");
            service.registerPatient(p);
            service.allocateNextAvailableBed(p);
        }

        assertEquals(0, service.getAvailableBeds().size());

        Inpatient overflow = new Inpatient("OVERFLOW", "No", "Room", 25, "Male", "N/A");
        service.registerPatient(overflow);

        assertThrows(BedUnavailableException.class, () -> service.allocateNextAvailableBed(overflow));
    }

    // -----------------------------------------------------------
    // Sort patients by surname / Patient ID
    // -----------------------------------------------------------
    @Test
    void testSortPatientsBySurname() throws DuplicatePatientException {
        service.registerPatient(new Patient("P020", "X", "Zulu", 20, "Male", "A", PatientCategory.OUTPATIENT));
        service.registerPatient(new Patient("P021", "Y", "Adams", 21, "Female", "B", PatientCategory.OUTPATIENT));
        service.registerPatient(new Patient("P022", "Z", "Mkhize", 22, "Male", "C", PatientCategory.EMERGENCY));

        List<Patient> sorted = service.getPatientsSortedBySurname();

        assertEquals("Adams", sorted.get(0).getLastName());
        assertEquals("Mkhize", sorted.get(1).getLastName());
        assertEquals("Zulu", sorted.get(2).getLastName());
    }

    @Test
    void testSortPatientsById() throws DuplicatePatientException {
        service.registerPatient(new Patient("P030", "X", "One", 20, "Male", "A", PatientCategory.OUTPATIENT));
        service.registerPatient(new Patient("P010", "Y", "Two", 21, "Female", "B", PatientCategory.OUTPATIENT));
        service.registerPatient(new Patient("P020", "Z", "Three", 22, "Male", "C", PatientCategory.EMERGENCY));

        List<Patient> sorted = service.getPatientsSortedById();

        assertEquals("P010", sorted.get(0).getPatientId());
        assertEquals("P020", sorted.get(1).getPatientId());
        assertEquals("P030", sorted.get(2).getPatientId());
    }

    // -----------------------------------------------------------
    // Reports
    // -----------------------------------------------------------
    @Test
    void testOccupancyPercentage() throws DuplicatePatientException, BedUnavailableException {
        for (int i = 1; i <= 5; i++) {
            Inpatient p = new Inpatient("R" + i, "N" + i, "S" + i, 20, "Male", "Cond");
            service.registerPatient(p);
            service.allocateNextAvailableBed(p);
        }
        // 5 out of 20 beds occupied = 25%
        assertEquals(25.0, service.getOccupancyPercentage(), 0.001);
    }

    // -----------------------------------------------------------
    // Inpatient inheritance / displayDetails override
    // -----------------------------------------------------------
    @Test
    void testInpatientDisplayDetailsIncludesBedAndWard() throws DuplicatePatientException, BedUnavailableException {
        Inpatient patient = new Inpatient("P099", "Thabo", "Mokoena", 29, "Male", "Observation");
        service.registerPatient(patient);
        Bed bed = service.allocateNextAvailableBed(patient);

        String details = patient.displayDetails();

        assertTrue(details.contains("Ward:"));
        assertTrue(details.contains(bed.getBedNumber()));
    }
}
