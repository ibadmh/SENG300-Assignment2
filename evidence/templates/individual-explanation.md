Group number: TBD
Student name: Ibad Hassan
Student ID: TBD
Include one file per member in the group submission.

# Individual explanation

Name: Ibad Hassan

Name: Ibad Hassan   Student ID: TBD

Boundary test. I wrote R1TimeValidationTest.invalidTimes[start=0,end=60]. The clock is fixed at 2026-11-01T10:00:00Z, the repository is empty, and I call create(R204, student-a, now, now plus 60 minutes). R1 says a booking starts after now. I read "after" as strict, because R2 and R4 say "exactly" when they allow an edge and R1 does not. Expected: INVALID_TIME, null booking id, repository still empty, no notification requested.

Mutant. The PIT mutant on BookingService line 26 (removed conditional on the end-after-start check) stops the end check from ever rejecting. The starter tests let it survive because none used a bad end. My invalidTimes[start=120,end=120] kills it: the start is valid, so only the end check can reject, and assertEquals(INVALID_TIME, result.status()) fails when the mutant returns CREATED. The rerun still shows it killed. I also ran nine manual mutants, reported apart from PIT totals.

Weakness and next test. Coverage can reach line 27 with a status-only assertion, so my checks on saved data and notifications matter: a status-only test passes on a mutant that saves the bad booking first. My ordering tests use one seed layout (an ACTIVE booking in the same room), and the strict start-equals-now reading is unconfirmed. Next test: seed an overlapping ACTIVE booking plus a CANCELLED one and one in another room, send a past start, and expect INVALID_TIME with all seeds unchanged.
