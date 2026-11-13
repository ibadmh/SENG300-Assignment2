# R1 contributions to the shared report (Ibad Hassan, 30244305; rule R1 valid times)

Commits: reference b4c869a; Phase A ba349f2; Phase B e9f23e0; R1 tests a3c5dfa.

## Part 1
Groups: start (far past, 1 min past, now, 1 min after, far after) and end relative to start (far before, 1 before, equal, 1 after, far after), one condition bad at a time. Edges: start at(-1), at(0), at(1) with end at(60); end at(119), at(120), at(121) with start at(120). "After now" is strict (R2, R4 name inclusive edges; confirm with TA). Ordering: ACTIVE same-room booking plus a bad request gives INVALID_TIME, with a CONFLICT control. Rows: evidence/templates/test-map.csv.

## Part 2
R1TimeValidationTest.java: 9 methods, 17 cases, fresh TestFixtures each (fixed clock). Rejections assert status, null id, repository equal to the snapshot, no notifier requests. 20 of 20 pass with the starter. Status-only checks miss save-then-reject (manual mutant saveThenReject).

## Part 3
Starter lines 22/31, branches 7/20; with R1 29/31 and 15/20. Gap G-R1-1 line 27 and start-bad branch (invalidTimes[0,60]; expected INVALID_TIME, nothing saved, no notification); G-R1-2 end-bad branch (invalidTimes[120,120]; same expectation). Both are R1, the group needs two in total. create() outcomes O1 start bad, O2 end bad, O3 valid all run (line 26 branches 2/4 to 4/4). Weakness: coverage cannot see missing assertions.

## Part 4
PIT: 20 generated, 13 killed (13/20 = 65%), 5 survived, 2 no coverage; strength 72%. No R1 survivors. Offered fallback: line 26 end check (starter SURVIVED, now killed by invalidTimes[120,120], one of five killing tests); the group prefers survivors elsewhere first. Manual R1 mutants (separate from PIT) all killed, including start equal to now.

## Part 5
Supplied patch is not R1: all tests pass on it. Group repair branch must start from the commit with everyone's tests.
