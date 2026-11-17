# R1 contributions to the shared report (Ibad Hassan, 30244305, Group 1; rule R1 valid times)

Commits: reference b4c869a; plan before code reading ba349f2; plan after inspection e9f23e0; R1 tests a3c5dfa; final coverage 46c372d; final PIT dfd4f54.

## Part 1
Groups: start against now (far past, 1 minute past, now, 1 minute after, far after) and end against start (far before, 1 before, equal, 1 after, far after), one condition bad at a time, plus one row with both bad. "After now" is read as strict, so a start equal to now is INVALID_TIME (R2 and R4 name their inclusive edges; R1 does not). Ordering: an ACTIVE same-room booking plus a bad request must give INVALID_TIME, not CONFLICT, with a CONFLICT control. After inspection (e9f23e0): the `||` in the guard short-circuits, so only-start-bad and only-end-bad rows are both needed. 17 R1 rows: evidence/templates/test-map.csv.

| Start | End | Expected |
| --- | --- | --- |
| at(-1), at(0) | at(60) | INVALID_TIME |
| at(1) | at(60) | CREATED |
| at(120) | at(119), at(120) | INVALID_TIME |
| at(120) | at(121) | CREATED |

## Part 2
R1TimeValidationTest.java (a3c5dfa), 9 methods, 17 cases: invalidTimes (6 rows), invalidTimesFarFromTheEdge (2), validEdgeTimes (3), controlSameSeedValidOverlap_isConflict, pastStartThatWouldConflict_isInvalidNotConflict, reversedTimesThatWouldConflict_isInvalidNotConflict, zeroLengthInsideActive_isInvalidNotConflict, invalidRequest_leavesAllOtherBookingsUnchanged, validRequest_addsOneBookingAndLeavesOthersUnchanged. Each test builds a fresh TestFixtures with the fixed clock. Every rejection asserts the status, a null id, the repository equal to its snapshot and no notification (assertRejectedAsInvalidTime). Return-value-only weakness: on mutant/saveThenReject (813b614) the R1 suite has 12 failures, but a status-only test passes (mutant/statusOnlyDemo, edac266; evidence/runs/manual-mutants). Results: 17 of 17 R1 tests pass (evidence/runs/final-tests, 46c372d); the whole suite passes 63 of 63 on main (evidence/runs/final-tests-63, f25fd71).

## Part 3
Gaps in the starter report (evidence/runs/starter-coverage, b4c869a): G-R1-1, line 27 and the start-bad branch, covered by invalidTimes[start=0,end=60] and [start=-1,end=60]; G-R1-2, the end-bad branch with a valid start, covered by invalidTimes[start=120,end=120] and [start=120,end=119]. Both expect INVALID_TIME, a null id, nothing saved and no notification. Line 26 branches go from 2/4 to 4/4 and line 27 from missed to executed. BookingService coverage (`./mvnw clean verify`): starter 22/31 lines, 7/20 branches; starter plus R1 29/31 and 15/20 (evidence/runs/r1-stage-coverage, a3c5dfa); whole suite 31/31 and 20/20 (evidence/runs/final-coverage, 46c372d). create() outcomes O1 start bad, O2 end bad, O3 valid are each run by named R1 tests (evidence/R1_decision_outcomes.md). Weakness: coverage shows a line ran, not that its result was checked.

## Part 4
PIT on the starter (evidence/runs/starter-pit): 5 killed, 7 survived, 8 no coverage, including the line 26 end-check conditional (index 16). With R1 (evidence/runs/r1-pit, a3c5dfa): 13 killed, 5 survived, 2 no coverage; no survivor is in R1 code. Whole suite (evidence/runs/final-mutation, dfd4f54): 20 of 20 killed. R1 tests kill M2 (create() line 38, save removed) through assertEquals(List.of(expected), f.repository.all()) in validEdgeTimes, and both line 26 conditionals and the line 27 null return (indexes 12, 16, 25). Index 16 is killed by zeroLengthInsideActive_isInvalidNotConflict (named by PIT) and by invalidTimes[start=120,end=120]. Nine manual R1 mutants, separate from PIT totals, are all killed (evidence/runs/manual-mutants).

## Part 5
The supplied patch is not an R1 bug: all R1 tests pass on it. See evidence/templates/defect-record.md (R1 scope note); the group bug record is in the shared report.
