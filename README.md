# SENG 300 testing starter

## Required language

Write all implementation, test doubles, extensions, bug fixes, and automated tests in Java 17. Use JUnit for core tests. The Maven scripts are supplied build tools.

## Start here

Install a full JDK 17, not only a JRE, and Git. Confirm `java -version` and `javac -version` both work.
Unzip the project into your own working folder. Open a terminal in `booking-tests`, where `pom.xml` lives.
Internet is needed for the first run to download Maven and dependencies. Maven itself does not need a separate installation.

On macOS or Linux:

```sh
chmod +x mvnw
./mvnw --version
./mvnw clean test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd --version
.\mvnw.cmd clean test
```

In the commands below, Windows users replace `./mvnw` with `.\mvnw.cmd`.
The wrapper downloads Maven 3.9.9. The pinned tools are JUnit 5.11.4, JaCoCo 0.8.12,
PIT 1.17.4 with JUnit plugin 1.2.2, and optional jqwik 1.9.2. Do not upgrade or change metric targets during the assignment.

## Your first session

1. Read the handout and `API.md` before opening `BookingService.java`.
2. Run the three supplied starter tests. They should pass, but they do not check every rule.
3. Initialize your private local repository using `git init`, then commit the starter.
4. Write your initial test plan in `evidence/templates/test-plan.md` and commit it before inspecting method bodies.
5. Run `./mvnw clean verify` and save the starter coverage report before adding tests.
6. Add your tests under `src/test/java/ca/ucalgary/seng300/booking/`. Test filenames must end in `Test.java`.

Do not publish assignment solutions to a public repository. Use the course's submission process.

## Commands and outputs

| Task | Command | Output |
| --- | --- | --- |
| Run core tests | `./mvnw clean test` | `target/surefire-reports/` |
| Core tests and coverage | `./mvnw clean verify` | `target/site/jacoco/index.html`, `jacoco.xml`, `jacoco.csv` |
| Mutation analysis | `./mvnw test-compile org.pitest:pitest-maven:mutationCoverage` | `target/pit-reports/index.html` and `mutations.xml` |
| Optional generated tests | `./mvnw -Pproperties clean test` | `target/surefire-reports/` and console seed information |
| One test class | `./mvnw -Dtest=YourTestClass test` | `target/surefire-reports/` |

For comparable coverage and PIT results, run without `-Pproperties`. Core metrics target `BookingService` only.
Records, enums, repository support, and fixtures are outside the metric denominator. They are not claims of whole-system coverage.
Add required behavioural tests to the core suite, even if an extension also checks the same behaviour.
PIT creates temporary mutants. It does not edit your source files. Run the ordinary tests successfully before PIT.
Keep production code unchanged on your reference branch. Use a separate branch for the faulty version or a manual mutation.

## Preserve evidence

`clean` removes `target`. Copy reports before running another clean build.
After starter coverage, copy `target/site/jacoco` to `evidence/runs/starter-coverage`.
Before and after mutation improvements, copy `target/pit-reports` to `evidence/runs/pre-mutation` and `evidence/runs/final-mutation`.
After the final coverage run, copy `target/site/jacoco` to `evidence/runs/final-coverage`.
Record commands and commit IDs in the provided templates. Evidence files are excluded from Git by default to keep reports small,
so include them separately in your final submission archive. The templates are tracked.

Copy the report folders manually. No Python installation is needed.

## Fixed time and notification support

`TestFixtures` starts with an empty repository, a fixed clock, and a recording notifier.
`at(180)` means 180 minutes after the fixed time. Each new fixture has its own storage and ID counter.
`seed` adds fixture data directly, including inactive bookings. Do not use the service to create impossible historical fixtures.
`notifier.requests()` returns the recorded requests. No real email is sent.

## Extension setup

Option A: use `-Pproperties`. `ExampleProperties.java` shows annotations and a fixed seed, but does not solve the booking task.
Put your property in that directory with a filename ending in `Properties.java`. Use distinct owner and caller IDs.
Option B: implement a `BookingRepository` substitute in your test code that fails on save after you have seeded its data.
It must fail before changing stored data. Do not make the failure occur during fixture setup.

## Faulty version exercise

The student package includes `faulty-version/buggy-version.patch`. Complete your initial tests before applying it. Use the README in that folder for instructions.
Commit your reference work and create a new branch with `git switch -c investigate-bug`.
Apply it using `git apply --check /path/to/buggy-version.patch` followed by `git apply /path/to/buggy-version.patch`.
Commit the supplied buggy version before fixing it, and retain the exact same distinguishing test for the three comparisons.
Do not use the buggy branch for your coverage or mutation comparisons.

## Troubleshooting

- If Maven reports no compiler, install a full JDK 17 and set JAVA_HOME to its folder. Reopen your terminal.
- If dependencies cannot download, check your network and retry the same command. Do not change versions to work around a proxy.
- If zero tests run, check the test directory, package, class filename, and annotations.
- If PIT reports failing tests before mutation, fix the ordinary test run first. A startup failure is not a killed mutant.
- Open the HTML reports in a browser after the command completes. Do not open `jacoco.exec` as text.

## Documentation

- JUnit: https://docs.junit.org/5.11.4/user-guide/index.html
- JaCoCo counters: https://www.jacoco.org/jacoco/trunk/doc/counters.html
- PIT Maven: https://pitest.org/quickstart/maven/
- jqwik 1.9.2: https://jqwik.net/docs/1.9.2/user-guide.html
