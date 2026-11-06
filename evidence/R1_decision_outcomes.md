# R1 decision outcomes for create() (Part 3), compared with JaCoCo

Method chosen: `BookingService.create()` as a whole (to agree with the R2 owner). R1 supplies outcomes O1 to O3; R2 supplies the rest.
JaCoCo data: evidence/runs/starter-coverage (reference b4c869a) and evidence/runs/final-coverage (tests commit a3c5dfa).

| Outcome | start after now? | end after start? | Same-room ACTIVE overlap? | Result | Tests that execute it |
| --- | --- | --- | --- | --- | --- |
| O1 | no | not evaluated (|| short-circuits) | not evaluated | INVALID_TIME | R1TimeValidationTest.invalidTimes[-1,60], [0,60], [-60,-30], [-30,-60]; invalidTimesFarFromTheEdge[-1440,60]; pastStartThatWouldConflict_isInvalidNotConflict; invalidRequest_leavesAllOtherBookingsUnchanged |
| O2 | yes | no | not evaluated | INVALID_TIME | invalidTimes[120,119], [120,120]; invalidTimesFarFromTheEdge[600,60]; reversedTimesThatWouldConflict_isInvalidNotConflict; zeroLengthInsideActive_isInvalidNotConflict |
| O3a | yes | yes | yes | CONFLICT | controlSameSeedValidOverlap_isConflict (R2 owner adds more) |
| O3b | yes | yes | no | CREATED | validEdgeTimes[1,60], [120,121], [1440,1680]; validRequest_addsOneBookingAndLeavesOthersUnchanged; starter createsFutureBooking |

JaCoCo comparison (line 26 is the guard, line 27 the INVALID_TIME return):

| Item | Starter | Final (starter 3 + R1 17) |
| --- | --- | --- |
| Line 26 branches covered | 2 of 4 | 4 of 4 |
| Line 27 | not executed (6 instructions missed) | executed |
| BookingService lines | 22 of 31 | 29 of 31 |
| BookingService branches | 7 of 20 | 15 of 20 |

All four line-26 branches are covered, which matches O1, O2 and O3 each being executed by at least one test. The 5 branches and 2 lines still missed are outside R1: lines 30, 31, 33 (conflict loop: inactive booking, different room, existing booking starting after the request ends; R2), and lines 47, 48, 51, 52 (INACTIVE and TOO_LATE; R3 and R4).

## One remaining weakness: coverage shows that code ran, not that anything was checked

Experiment: one test that only asserts the status (evidence/runs/status-only-demo, StatusOnlyDemoTest on branch mutant/statusOnlyDemo, commit edac266). JaCoCo reports line 27 as covered, yet that test PASSES on the mutant that saves the bad booking before returning INVALID_TIME. Only the unchanged-repository assertion in R1TimeValidationTest.assertRejectedAsInvalidTime kills that mutant (12 failing tests, commit 813b614).
A second, smaller weakness: `isAfter` versus `!isBefore` run the same branches, so coverage cannot tell whether the edge value (start equal to now) was tested; only the boundary tests and the manual mutant M-R1-m1 show it.

Notes: on branch mutant/saveThenReject the INVALID_TIME return sits on line 28 (an added save line shifts it); jacoco.xml of that run is in evidence/runs/status-only-demo. Manual mutant dropEndCondition behaves exactly like PIT index 16, so it is the same finding, not an extra one. Every manual mutant branch is one commit on top of the R1 test commit a3c5dfa.
