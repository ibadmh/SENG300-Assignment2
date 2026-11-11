### R4 Cancellation time (Dawood)

Status: Phase A. Written from the handout, README, API.md, records, enums, TestFixtures and starter tests only. BookingService method bodies not yet read.

Rule: an owner can cancel an ACTIVE booking at least 120 minutes before its start. Exactly 120 is allowed. Less than 120 returns TOO_LATE.

Fixed clock: TestFixtures.NOW = 2026-11-01T10:00:00Z. at(m) = NOW + m minutes. Time remaining = start - NOW, in whole minutes. Every test builds a fresh TestFixtures and seeds with f.seed(...), never through create().

Held fixed unless the row says otherwise: booking b1, room R204, owner student-a, caller student-a, state ACTIVE, end = start + 60.

Reading of "at least 120": INCLUSIVE, so remaining = 120 is CANCELLED. The rule states this edge explicitly ("Exactly 120 is allowed").

Reading of precedence: R3 is checked first. A non-owner gets DENIED and an inactive booking gets INACTIVE, even when the time is also too late. R4 only decides between CANCELLED and TOO_LATE for owner + ACTIVE.

#### Input groups (time remaining before start)

| Group | Remaining | Example start | Expected |
| --- | --- | --- | --- |
| T1 | already started (start in the past, end in the future) | at(-30), end at(30) | TOO_LATE |
| T2 | starts exactly now | at(0) | TOO_LATE |
| T3 | clearly too late | at(60) | TOO_LATE |
| T4 | one minute under the edge | at(119) | TOO_LATE |
| T5 | the edge | at(120) | CANCELLED |
| T6 | one minute over the edge | at(121) | CANCELLED |
| T7 | clearly in time | at(1440) (one day) | CANCELLED |

T1 is seeded directly (the service could not create a past booking). It is in the input domain because time passes after creation.

#### Boundary values

| Start | End | Expected status | Expected saved state of b1 | Notifications |
| --- | --- | --- | --- | --- |
| at(119) | at(179) | TOO_LATE | ACTIVE (unchanged) | 0 |
| at(120) | at(180) | CANCELLED | CANCELLED | 1 (for b1) |
| at(121) | at(181) | CANCELLED | CANCELLED | 1 (for b1) |

End-independence check: the rule depends on start only. Two extra rows hold start at the edge and change the end:
- at(119) to at(600) (long booking): TOO_LATE. A long booking does not make cancellation allowed.
- at(120) to at(121) (one-minute booking): CANCELLED. A short booking does not block it.
These catch an implementation that measures to end instead of start.

#### Precedence with R3 (time is too late in every row)

| Caller | State | Start | Expected | Why |
| --- | --- | --- | --- | --- |
| student-b (non-owner) | ACTIVE | at(30) | DENIED | ownership checked before time |
| student-a | CANCELLED | at(30) | INACTIVE | state checked before time |
| student-a | COMPLETED | at(30) | INACTIVE | state checked before time |

All three: b1 unchanged, 0 notifications.

#### Expected saved data and notifications

- CANCELLED: f.repository.find("b1") has state CANCELLED; id, room, owner, start, end unchanged. notifier.requests() has exactly one entry, with id b1. Other seeded bookings unchanged.
- TOO_LATE: f.repository.all() equals the seeded list (b1 still ACTIVE). notifier.requests() is empty.
- Every test also seeds b2 (R305, student-c, at(300) to at(360), ACTIVE) to check "leave other bookings unchanged".

#### Cancellation decision table (shared with R3, Tahim)

Owner = caller is the booking owner. Time = minutes from now to start.

| # | Owner | State | Time | Expected | Saved state | Notifications |
| --- | --- | --- | --- | --- | --- | --- |
| D1 | yes | ACTIVE | >= 120 | CANCELLED | CANCELLED | 1 |
| D2 | yes | ACTIVE | < 120 | TOO_LATE | ACTIVE | 0 |
| D3 | yes | CANCELLED | >= 120 | INACTIVE | CANCELLED | 0 |
| D4 | yes | CANCELLED | < 120 | INACTIVE | CANCELLED | 0 |
| D5 | yes | COMPLETED | >= 120 | INACTIVE | COMPLETED | 0 |
| D6 | yes | COMPLETED | < 120 | INACTIVE | COMPLETED | 0 |
| D7 | no | ACTIVE | >= 120 | DENIED | ACTIVE | 0 |
| D8 | no | ACTIVE | < 120 | DENIED | ACTIVE | 0 |
| D9 | no | CANCELLED | any | DENIED | CANCELLED | 0 |
| D10 | no | COMPLETED | any | DENIED | COMPLETED | 0 |

R4 tests cover D1, D2, D4, D6 and D8. R3 tests (Tahim) cover the rest.

#### Mutation link (Part 4)

Required mutant: minutes < 120 changed to minutes <= 120. Under the mutant, remaining = 120 returns TOO_LATE. The at(120) row kills it in two ways: the status assertion (CANCELLED expected) and the saved-state assertion (CANCELLED expected, ACTIVE found). The at(119) and at(121) rows do not kill it, which is why the exact edge is tested.

#### Planned tests (R4CancellationTimeTest.java)

- tooLate_returnsTooLateAndChangesNothing (parameterized: start -30, 0, 60, 119)
- inTime_cancelsSavesAndNotifiesOnce (parameterized: start 120, 121, 1440)
- tooLate_longBookingStillTooLate (start 119, end 600)
- edge_shortBookingStillCancels (start 120, end 121)
- tooLate_nonOwnerGetsDenied (D8)
- tooLate_cancelledBookingGetsInactive (D4)
- tooLate_completedBookingGetsInactive (D6)
- cancel_leavesOtherBookingsUnchanged

#### Changes after code inspection

(Phase B: fill in after the Phase A commit, once BookingService.cancel() has been read.)
