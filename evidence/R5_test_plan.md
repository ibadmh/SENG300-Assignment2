# Initial Test Plan
Written using handout, README.md, API.md, records, enums, TestFixtures and starter tests only. No method bodies read.

Rule: 
- Successful cancellation returns CANCELLED, saves that state, and requests one
notification. 
- Rejected requests change nothing and request no notification. 
- Cancelling again adds no request. 
- Creating a booking sends no notification. 
- Leave other bookings unchanged.

## Input groups
| Group                    | Return    | Change Saved State | Notifications |
|--------------------------|-----------|--------------------|---------------|
| Successfull cancellation | CANCELLED | Yes                | 1             |
| Inactive Request         | INACTIVE  | No                 | 0             |
| Denied Request           | DENIED    | No                 | 0             |
| Too Late Request         | TOO_LATE  | No                 | 0             |
| Repeat Cancellation      | CANCELLED | No                 | 0             |
| Booking Created          | ACTIVE    | Yes                | 0             |

## Boundaries
See R1-R4 for boundaries of the above input groups

## Cancellation decision table (Shared w. R3, R4)
Owner = caller is the booking owner. Time = minutes from now to start.

| #   | Owner | State     | Time   | Expected  | Saved state | Notifications |
|-----|-------|-----------|--------|-----------|-------------|---------------|
| D1  | yes   | ACTIVE    | >= 120 | CANCELLED | CANCELLED   | 1             |
| D2  | yes   | ACTIVE    | < 120  | TOO_LATE  | ACTIVE      | 0             |
| D3  | yes   | CANCELLED | >= 120 | INACTIVE  | CANCELLED   | 0             |
| D4  | yes   | CANCELLED | < 120  | INACTIVE  | CANCELLED   | 0             |
| D5  | yes   | COMPLETED | >= 120 | INACTIVE  | COMPLETED   | 0             |
| D6  | yes   | COMPLETED | < 120  | INACTIVE  | COMPLETED   | 0             |
| D7  | no    | ACTIVE    | >= 120 | DENIED    | ACTIVE      | 0             |
| D8  | no    | ACTIVE    | < 120  | DENIED    | ACTIVE      | 0             |
| D9  | no    | CANCELLED | any    | DENIED    | CANCELLED   | 0             |
| D10 | no    | COMPLETED | any    | DENIED    | COMPLETED   | 0             |

## Two action sequences
### Repeat Cancellation (Owner)
- Test for D1, then D3
- Test for D1, then D4
### Repeat Cancellation (Non-Owner)
- Test for D1, then D9
## Changes after code inspection