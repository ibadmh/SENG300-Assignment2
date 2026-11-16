# Part 4: Mutation testing

Records: `evidence/Part4_mutations.csv`, `evidence/Part4_run-log.csv`. Reports: `evidence/runs/pre-mutation/`, `evidence/runs/final-mutation/`.

## Runs
PIT 1.17.4, configured in `pom.xml` (target `BookingService*`, tests `*Test`, DEFAULTS mutators). The configuration was not changed.

```
./mvnw -B clean test
./mvnw -B test-compile org.pitest:pitest-maven:mutationCoverage
```

| Run | Commit | Tests | Generated | Killed | Survived | No coverage | Timed out | Run error | Score |
|---|---|---|---|---|---|---|---|---|---|
| Pre (R1–R4 committed) | 3af2db5 | 52 pass | 20 | 20 | 0 | 0 | 0 | 0 | 20/20 = 100% |
| Final (R1–R4 + sequence tests) | 04745b6 | 54 pass | 20 | 20 | 0 | 0 | 0 | 0 | 20/20 = 100% |

Score = KILLED / generated. In both runs, all 20 kills are assertion failures; none come from timeouts or tool errors (TIMED_OUT, RUN_ERROR, MEMORY_ERROR and NON_VIABLE are all 0). For comparison, the earlier run with only the starter and R4 tests (`evidence/runs/r4/r4-pit`) gave 12 killed, 2 survived and 6 no coverage.

## Three mutants
PIT names only the first killing test, so we rechecked each mutant with a temporary local edit, ran the whole suite and restored the code (logs in `evidence/runs/manual-mutants-part4/`).

| | Location and change | Rule | Status | Detecting assertion |
|---|---|---|---|---|
| M1 | `cancel()` line 51, `minutes < 120` → `minutes <= 120` (PIT ConditionalsBoundary) | R4 | KILLED | `assertEquals(CANCELLED, actual)` in `R4CancellationTimeTest.assertCancelled`, reached only by the start = 120 cases (`inTime_cancelsSavesAndNotifiesOnce[120]`, `edge_shortBookingStillCancels`, `cancel_leavesOtherBookingsUnchanged`); 3 failures |
| M2 | `create()` line 38, `repository.save(booking)` removed (PIT VoidMethodCall) | R2 / R1 | SURVIVED (starter + R4) → KILLED | `assertEquals(List.of(expected), repository.all())` in `R1TimeValidationTest.validEdgeTimes`; size and `find(id)` checks in `R2RoomConflictsTest.checkRoomConflict`; 10 failures |
| M3 | `create()` line 30, `state() == ACTIVE` replaced with `false` (PIT RemoveConditional) | R2 | NO_COVERAGE (starter + R4) → KILLED | `assertEquals(CONFLICT, status)` in `R2RoomConflictsTest` overlap cases and `R1TimeValidationTest.controlSameSeedValidOverlap_isConflict`; 6 failures |

**M1.** At exactly 120 minutes R4 says the cancel is allowed, but the mutant returns TOO_LATE and leaves the booking ACTIVE. The 119 and 121 rows give the same result on both versions, so only the exact edge can kill this mutant. That is why the R4 plan tests 119, 120 and 121.

**M2.** The mutant still returns CREATED with a valid id, so the starter test `createsFutureBooking` passes, because it checks only the status and id. This is the return-value-only weakness. The R1 and R2 tests kill it because they assert the saved repository contents.

**M3.** With the starter and R4 tests no test ever created a booking against an existing ACTIVE booking in the same room, so the line was never reached. The R2 overlap tests reach it and expect CONFLICT. The opposite change (`true`, so CANCELLED and COMPLETED bookings would also block the room) is not a DEFAULTS mutant. Our manual run shows that only `cancelledExistingOverlap_isCreated` and `completedExistingOverlap_isCreated` catch it.

**Improvements.** All three mutants are killed by the committed rule tests, so no new test was needed after the pre run. M2 and M3 were fixed by the R1/R2 saved-data and overlap tests, which were written before the pre run, and M1 by the R4 boundary row at 120. Between the runs, `CancelSequenceTest` (two sequence tests) was added; the final run gives the same result (20/20 killed), and the same tests kill M1, M2 and M3.

## Equivalent mutant
`cancel()` line 51: `minutes < 120` → `minutes <= 119`. `minutes` is a `long` produced by `Duration.toMinutes()`. For every integer m, m < 120 holds exactly when m ≤ 119, so every allowed input gets the same status, saved data and notifications from both versions. The manual run passes 52 of 52 tests, as expected. No test can kill this mutant, and it is not counted in the PIT totals.

## Beyond coverage
In the starter + R4 run, line 38 was covered but its mutant SURVIVED: the line ran, but no test checked that its effect was stored. Line coverage on the pre run is 31/31, but coverage alone could not show that removing `save` or moving the 120 boundary would be caught. Mutation testing checks that the assertions notice a wrong version. It also has limits: DEFAULTS does not generate the M3 opposite change or a `<= 119` style change, so a 100% score covers only the mutants PIT actually made.
