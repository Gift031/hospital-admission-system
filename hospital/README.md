# MediCare Hospital – Patient Admission System

A console-based, menu-driven Java application for managing patient records
and bed allocation in a single 20-bed hospital ward (4 × 5 layout).

## Project layout

```
hospital/
├── pom.xml
├── src/main/java/hospital/
│   ├── model/
│   │   ├── Patient.java          # base class
│   │   ├── Inpatient.java        # extends Patient, adds ward/bed info
│   │   ├── PatientCategory.java  # enum: INPATIENT, OUTPATIENT, EMERGENCY
│   │   └── Bed.java
│   ├── service/
│   │   ├── HospitalService.java  # core business logic
│   │   ├── DuplicatePatientException.java
│   │   └── BedUnavailableException.java
│   └── app/
│       └── Main.java             # console menu entry point
└── src/test/java/hospital/
    └── HospitalServiceTest.java  # JUnit 5 tests
```

## Requirements

- JDK 17 or later
- Maven 3.6+ (for building and running tests)

## Build and run

```bash
cd hospital
mvn clean package
java -jar target/hospital-admission-system.jar
```

## Run the tests

```bash
mvn test
```

The test suite covers: registering a patient, searching by ID, updating
details, deleting a patient, allocating a bed, releasing a bed, preventing
duplicate Patient IDs, preventing allocation of an occupied bed, preventing
allocation when the ward is full, and sorting patients by surname / ID.

## Without Maven

If Maven isn't available, compile and run with the JDK directly:

```bash
cd hospital
mkdir out
javac -d out $(find src/main -name "*.java")
java -cp out hospital.app.Main
```

(JUnit tests require Maven, or JUnit jars added manually to the classpath,
since they depend on the JUnit 5 library.)

## Design notes

- `Patient` is the base class holding common fields (ID, name, age, gender,
  condition, category) and a `displayDetails()` method.
- `Inpatient extends Patient`, calls `super()` to initialise inherited
  fields, adds `wardNumber` / `bedNumber`, and overrides `displayDetails()`
  to include them.
- `PatientCategory` is an enum used to distinguish Inpatient / Outpatient /
  Emergency patients. Only `INPATIENT` category patients may be allocated a
  bed — enforced in `HospitalService.allocateBed()`.
- `HospitalService` holds all state in memory (a `Map<String, Patient>` for
  patients and a `List<Bed>` for the 20-bed ward) and implements every
  operation required by the assignment brief, throwing checked exceptions
  (`DuplicatePatientException`, `BedUnavailableException`) for invalid
  operations instead of failing silently.
- `Main` is a thin console/menu layer that talks only to `HospitalService`,
  keeping business logic out of the UI.
