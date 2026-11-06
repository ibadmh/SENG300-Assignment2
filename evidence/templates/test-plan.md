# Initial test plan

Write this before inspecting method bodies.

Status: Phase A, written before BookingService.java was opened. Sources used: handout, README, API.md, enums, records, TestFixtures, starter tests.
Author of this section: Ibad Hassan (rule R1, valid times). Sections for R2 to R5 and the shared decision table are filled in by the teammates who own those rules.

## Input groups and boundaries

### R1 Valid times (Ibad)

Rule: a booking starts after now and ends after its start. Check this before checking conflicts. Invalid times return INVALID_TIME and save nothing.

Fixed clock: TestFixtures.NOW = 2026-11-01T10:00:00Z. at(m) is NOW plus m whole minutes (at(0) is now, at(-1) is one minute ago). UTC, whole minutes, no sleeps, no real clock.

Reading of "after now": STRICT, so start equal to now is INVALID_TIME. Reasons: "after" is not "at or after", and the other rules state inclusive edges out loud (R2 "may start exactly when another ends", R4 "Exactly 120 is allowed") while R1 does not. Still to confirm with the TA. Test-map rows with at(0) depend on this reading.

Input groups for create(roomId, ownerId, start, end), time only (room R204, owner student-a held fixed):

| Group | Start relative to now | Example |
| --- | --- | --- |
| S1 | clearly before now | at(-60) |
| S2 | one minute before now | at(-1) |
| S3 | exactly now | at(0) |
| S4 | one minute after now | at(1) |
| S5 | clearly after now | at(180) |

| Group | End relative to start | Example (start at(120)) |
| --- | --- | --- |
| E1 | clearly before start | at(60) |
| E2 | one minute before start | at(119) |
| E3 | equal to start | at(120) |
| E4 | one minute after start | at(121) |
| E5 | clearly after start | at(240) |

Rule for isolating a check: when testing a start edge, keep the end valid (for example at(60)). When testing an end edge, keep the start valid (for example at(120)). Otherwise a failure cannot be traced to one check.
Both-bad (start in the past and end before start) is a bonus case. A booking wholly in the past with correct order (at(-60) to at(-30)) is invalid because it is already over.

Boundary values (expected result comes from R1 only):

| Start | End | Expected |
| --- | --- | --- |
| at(-1) | at(60) | INVALID_TIME |
| at(0) | at(60) | INVALID_TIME (strict reading) |
| at(1) | at(60) | CREATED |
| at(120) | at(119) | INVALID_TIME |
| at(120) | at(120) | INVALID_TIME |
| at(120) | at(121) | CREATED |
| at(-60) | at(-30) | INVALID_TIME |
| at(-30) | at(-60) | INVALID_TIME |

Ordering rule ("check this before checking conflicts"): seed an ACTIVE booking in the SAME room (use seed, never the service) whose time overlaps a bad request. Expect INVALID_TIME, not CONFLICT. A CANCELLED seed or another room would prove nothing. A control row with the same seed and a valid overlapping request must give CONFLICT, so the ordering tests are meaningful.

Expected saved data and notifications:
- INVALID_TIME: bookingId is null; repository contents equal what was seeded (empty if nothing); no notification requested (creating or rejecting sends none).
- CREATED edges: exactly one booking, ACTIVE, with the requested room, owner, start, end and the id returned; no notification requested.

### R2 to R5 (teammates)

To be completed by the owners of R2 to R5. Ibad reviews them for the group.

## Cancellation decision table

Owned by the R3 to R5 members. R1 does not cancel anything.

| Owner | State | Time remaining | Expected result |
| --- | --- | --- | --- |
| (teammates) | | | |

## Two action sequences

Owned by the R3 to R5 members: (1) cancel successfully then cancel again; (2) non-owner tries, then the owner cancels.

## Changes after code inspection

Phase B, written after reading BookingService.create() (src/main/java/ca/ucalgary/seng300/booking/BookingService.java lines 24 to 40). I read it to adjust the plan, not to derive expected values. Expected values still come from the rules.

What I saw and what it changes for R1:
1. R1 is one inline guard at line 26, `!start.isAfter(now) || !end.isAfter(start)`, returning INVALID_TIME at line 27, before the conflict loop at line 29. The order matches the rule, so the ordering tests stay.
2. Both comparisons are strict, matching the plain reading of "after now". Tests expect INVALID_TIME because of the rule text, not because of the code. If the TA says otherwise, tests and code change together.
3. `||` short-circuits. When the start is bad the end check never runs. So the both-bad case does not reach the end condition. Cases where ONLY the start is bad and ONLY the end is bad are needed; Phase A already has them (start edges with a valid end, end edges with a valid start). Do not count the both-bad row toward end-branch coverage.
4. `nextId.get()` and `save` run only after validation, so no ID is consumed on rejection. That is not a rule, so tests do not assert it.
5. The conflict loop needs state ACTIVE and the same room, so ordering seeds must be ACTIVE and in the same room (already planned). A comment in the ordering tests says so.
6. There is no separate time-validation method. For the Part 3 "one small method" decision-outcome table, use create() as a whole (agree with the R2 owner) and list R1's share.

Plan changes: added the short-circuit note (item 3) and the same-room/ACTIVE comment in ordering tests. No test was added or removed.
