# One bug record

Scope note (Ibad, R1): the supplied bug is NOT an R1 bug. No R1 test and no starter test detects it, so the repair, three-version evidence and bug record belong to the member who owns the broken rule (R3 and R5, cancellation). This file records only what the R1 suite shows.

## Rule and setup

Branch `investigate-bug`, created from the r1-tests commit the R1 tests commit (all R1 tests pass there). Patch applied unchanged from `booking-tests` with `git apply --check ../faulty-version/buggy-version.patch` then `git apply ../faulty-version/buggy-version.patch`. Committed as d3ddb52 (supplied buggy version). Not used for coverage or PIT.

## Steps to reproduce

1. `git switch -c investigate-bug` from the commit with the tests.
2. Apply the patch as above and commit.
3. `./mvnw clean test` (Windows: `.\mvnw.cmd clean test`).

## Expected and actual outcomes

R1 suite plus starter tests on the buggy version: 19 run, 0 failures, 0 errors (evidence/runs/part5/buggy-r1-suite). No R1 test fails, so R1 does not detect it.
Change made by the patch (diff of d3ddb52 against its parent): in `cancel()`, inside the "caller is not the owner" branch, a call `repository.save(booking.withState(BookingState.CANCELLED))` is added before `return CancelStatus.DENIED`. Handoff for the R3/R5 owner: a non-owner's attempt still returns DENIED but the booking is saved as CANCELLED. This breaks R3 (non-owner receives DENIED, nothing changes) and R5 (rejected requests change nothing). The starter test otherStudentIsDenied checks only the returned status, so it still passes; a test that also reads `repository.find("b1")` after the denied call, will detect it (the bug requests no notification, so only the stored-state check detects it).

## Cause and minimal fix

For the owning member: remove the added save line (one line) on a repair branch.

## Same test on reference and buggy and repaired versions

Not done by Ibad (not R1). R1 tests: pass on reference (the R1 tests commit), pass on buggy (d3ddb52).

## Full suite after repair

Owner of the repair records this.

## Remaining risk

Rerunning the suite only checks the inputs the tests chose. R1 tests do not exercise cancel() at all, so they could not notice this class of bug.

Group note: the group repair branch must be created from the commit holding ALL members' tests, not from the R1-only commit.
