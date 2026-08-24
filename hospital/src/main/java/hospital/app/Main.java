package hospital.app;

import hospital.model.Bed;
import hospital.model.Inpatient;
import hospital.model.Patient;
import hospital.model.PatientCategory;
import hospital.service.BedUnavailableException;
import hospital.service.DuplicatePatientException;
import hospital.service.HospitalService;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based, menu-driven entry point for the MediCare Hospital
 * Patient Admission System.
 */
public class Main {

    private static final HospitalService service = new HospitalService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println(" MediCare Hospital - Patient Admission System");
        System.out.println("=======================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> patientManagementMenu();
                case 2 -> bedManagementMenu();
                case 3 -> reportsMenu();
                case 0 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // ---------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------

    private static void printMainMenu() {
        System.out.println("\n--------------------- MAIN MENU ---------------------");
        System.out.println("1. Patient Management");
        System.out.println("2. Bed Management");
        System.out.println("3. Reports");
        System.out.println("0. Exit");
        System.out.println("-------------------------------------------------------");
    }

    private static void patientManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----------------- PATIENT MANAGEMENT -----------------");
            System.out.println("1. Register a new patient");
            System.out.println("2. Search for a patient");
            System.out.println("3. Update patient details");
            System.out.println("4. Delete a patient");
            System.out.println("5. Display all registered patients");
            System.out.println("0. Back to main menu");
            System.out.println("-------------------------------------------------------");
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> registerPatient();
                case 2 -> searchPatient();
                case 3 -> updatePatient();
                case 4 -> deletePatient();
                case 5 -> displayAllPatients(service.getAllPatients());
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void bedManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------------ BED MANAGEMENT --------------------");
            System.out.println("1. Allocate a bed to an inpatient");
            System.out.println("2. Release a bed");
            System.out.println("3. Display complete ward layout");
            System.out.println("4. Display available beds");
            System.out.println("5. Display occupied beds");
            System.out.println("0. Back to main menu");
            System.out.println("-------------------------------------------------------");
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> allocateBed();
                case 2 -> releaseBed();
                case 3 -> displayWardLayout();
                case 4 -> displayBedList(service.getAvailableBeds(), "AVAILABLE BEDS");
                case 5 -> displayBedList(service.getOccupiedBeds(), "OCCUPIED BEDS");
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n---------------------- REPORTS -----------------------");
            System.out.println("1. All registered patients");
            System.out.println("2. All available beds");
            System.out.println("3. All occupied beds");
            System.out.println("4. Total number of registered patients");
            System.out.println("5. Total number of occupied beds");
            System.out.println("6. Ward occupancy percentage");
            System.out.println("7. Patients sorted by surname");
            System.out.println("8. Patients sorted by Patient ID");
            System.out.println("9. Summary report");
            System.out.println("0. Back to main menu");
            System.out.println("-------------------------------------------------------");
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                case 1 -> displayAllPatients(service.getAllPatients());
                case 2 -> displayBedList(service.getAvailableBeds(), "AVAILABLE BEDS");
                case 3 -> displayBedList(service.getOccupiedBeds(), "OCCUPIED BEDS");
                case 4 -> System.out.println("Total registered patients: " + service.getTotalPatients());
                case 5 -> System.out.println("Total occupied beds: " + service.getTotalOccupiedBeds()
                        + " / " + HospitalService.TOTAL_BEDS);
                case 6 -> System.out.printf("Ward occupancy: %.2f%%%n", service.getOccupancyPercentage());
                case 7 -> displayAllPatients(service.getPatientsSortedBySurname());
                case 8 -> displayAllPatients(service.getPatientsSortedById());
                case 9 -> displaySummaryReport();
                case 0 -> back = true;
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displaySummaryReport() {
        System.out.println("\n---------------------- SUMMARY -----------------------");
        System.out.println("Total registered patients: " + service.getTotalPatients());
        System.out.println("Total occupied beds: " + service.getTotalOccupiedBeds()
                + " / " + HospitalService.TOTAL_BEDS);
        System.out.println("Available beds: " + service.getAvailableBeds().size());
        System.out.printf("Ward occupancy: %.2f%%%n", service.getOccupancyPercentage());
        System.out.println("-------------------------------------------------------");
    }

    // ---------------------------------------------------------------
    // Patient Management actions
    // ---------------------------------------------------------------

    private static void registerPatient() {
        System.out.println("\n-- Register New Patient --");
        String id = readLine("Patient ID: ");
        if (service.searchPatient(id) != null) {
            System.out.println("Error: A patient with ID " + id + " already exists.");
            return;
        }
        String firstName = readLine("First Name: ");
        String lastName = readLine("Last Name: ");
        int age = readInt("Age: ");
        String gender = readLine("Gender: ");
        String condition = readLine("Medical Condition: ");
        PatientCategory category = readCategory();

        try {
            Patient patient;
            if (category == PatientCategory.INPATIENT) {
                patient = new Inpatient(id, firstName, lastName, age, gender, condition);
            } else {
                patient = new Patient(id, firstName, lastName, age, gender, condition, category);
            }
            service.registerPatient(patient);
            System.out.println("Patient registered successfully.");

            if (category == PatientCategory.INPATIENT) {
                String allocate = readLine("Allocate a bed now? (Y/N): ");
                if (allocate.equalsIgnoreCase("Y")) {
                    try {
                        Bed bed = service.allocateNextAvailableBed((Inpatient) patient);
                        System.out.println("Bed " + bed.getBedNumber() + " allocated to " + id + ".");
                    } catch (BedUnavailableException e) {
                        System.out.println("Could not allocate a bed: " + e.getMessage());
                    }
                }
            }
        } catch (DuplicatePatientException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void searchPatient() {
        String id = readLine("\nEnter Patient ID to search: ");
        Patient patient = service.searchPatient(id);
        if (patient == null) {
            System.out.println("No patient found with ID " + id + ".");
        } else {
            System.out.println(patient.displayDetails());
        }
    }

    private static void updatePatient() {
        String id = readLine("\nEnter Patient ID to update: ");
        Patient patient = service.searchPatient(id);
        if (patient == null) {
            System.out.println("No patient found with ID " + id + ".");
            return;
        }
        System.out.println("Current details: " + patient.displayDetails());
        String firstName = readLine("New First Name [" + patient.getFirstName() + "]: ", patient.getFirstName());
        String lastName = readLine("New Last Name [" + patient.getLastName() + "]: ", patient.getLastName());
        int age = readIntDefault("New Age [" + patient.getAge() + "]: ", patient.getAge());
        String gender = readLine("New Gender [" + patient.getGender() + "]: ", patient.getGender());
        String condition = readLine("New Medical Condition [" + patient.getMedicalCondition() + "]: ",
                patient.getMedicalCondition());

        boolean updated = service.updatePatient(id, firstName, lastName, age, gender, condition);
        System.out.println(updated ? "Patient updated successfully." : "Update failed.");
    }

    private static void deletePatient() {
        String id = readLine("\nEnter Patient ID to delete: ");
        boolean deleted = service.deletePatient(id);
        System.out.println(deleted ? "Patient deleted successfully." : "No patient found with ID " + id + ".");
    }

    private static void displayAllPatients(List<Patient> patientList) {
        System.out.println("\n----------------- REGISTERED PATIENTS ----------------");
        if (patientList.isEmpty()) {
            System.out.println("No patients registered.");
            return;
        }
        for (Patient p : patientList) {
            System.out.println(p.displayDetails());
        }
        System.out.println("Total: " + patientList.size());
    }

    // ---------------------------------------------------------------
    // Bed Management actions
    // ---------------------------------------------------------------

    private static void allocateBed() {
        String id = readLine("\nEnter Patient ID to allocate a bed to: ");
        Patient patient = service.searchPatient(id);
        if (patient == null) {
            System.out.println("No patient found with ID " + id + ".");
            return;
        }
        if (!(patient instanceof Inpatient inpatient)) {
            System.out.println("Only inpatients may be allocated a bed. This patient is " + patient.getCategory() + ".");
            return;
        }
        if (inpatient.getBedNumber() != null) {
            System.out.println("This patient already occupies bed " + inpatient.getBedNumber() + ".");
            return;
        }
        String bedNumber = readLine("Enter Bed Number (e.g. B01) or press Enter for next available: ");
        try {
            if (bedNumber.isBlank()) {
                Bed bed = service.allocateNextAvailableBed(inpatient);
                System.out.println("Bed " + bed.getBedNumber() + " allocated to " + id + ".");
            } else {
                service.allocateBed(bedNumber, inpatient);
                System.out.println("Bed " + bedNumber + " allocated to " + id + ".");
            }
        } catch (BedUnavailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void releaseBed() {
        String bedNumber = readLine("\nEnter Bed Number to release (e.g. B01): ");
        try {
            service.releaseBed(bedNumber);
            System.out.println("Bed " + bedNumber + " released successfully.");
        } catch (BedUnavailableException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void displayWardLayout() {
        System.out.println("\n-------------------- WARD LAYOUT ---------------------");
        List<Bed> beds = service.getWardLayout();
        for (int i = 0; i < beds.size(); i++) {
            String marker = beds.get(i).isOccupied() ? "[X]" : "[ ]";
            System.out.printf("%s%s  ", beds.get(i).getBedNumber(), marker);
            if ((i + 1) % HospitalService.WARD_COLS == 0) {
                System.out.println();
            }
        }
        System.out.println("Legend: [ ] Available   [X] Occupied");
    }

    private static void displayBedList(List<Bed> beds, String title) {
        System.out.println("\n-------------------- " + title + " --------------------");
        if (beds.isEmpty()) {
            System.out.println("None found.");
            return;
        }
        for (Bed bed : beds) {
            System.out.println(bed);
        }
        System.out.println("Total: " + beds.size());
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private static PatientCategory readCategory() {
        while (true) {
            System.out.println("Patient Category: 1) Inpatient  2) Outpatient  3) Emergency");
            int choice = readInt("Select category: ");
            switch (choice) {
                case 1 -> { return PatientCategory.INPATIENT; }
                case 2 -> { return PatientCategory.OUTPATIENT; }
                case 3 -> { return PatientCategory.EMERGENCY; }
                default -> System.out.println("Invalid choice, please select 1, 2 or 3.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String readLine(String prompt, String defaultValue) {
        String input = readLine(prompt);
        return input.isBlank() ? defaultValue : input;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static int readIntDefault(String prompt, int defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number, keeping previous value.");
            return defaultValue;
        }
    }
}