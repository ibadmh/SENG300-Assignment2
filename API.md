# Public API and input domain

Use the handout's rules R1 to R5 as the expected behaviour. This file describes calls without explaining their implementation.

`BookingService(repository, notifier, clock, nextId)` receives its dependencies.
`nextId` supplies fresh nonempty booking IDs. The provided fixture uses created-1, created-2, and so on.
Seed IDs must be different, for example b1 and b2.

`create(roomId, ownerId, start, end)` returns `CreateResult`.
Its status is CREATED, INVALID_TIME, or CONFLICT. `bookingId` is non-null only when CREATED.

`cancel(bookingId, callerId)` returns CANCELLED, DENIED, INACTIVE, or TOO_LATE.
The caller ID comes from a trusted application boundary. Testing login is outside scope.

`Booking` is an immutable record with id, roomId, ownerId, start, end, and state accessors.
Its state is ACTIVE, CANCELLED, or COMPLETED. `withState(next)` returns a new value.

`BookingRepository.find(id)` loads an existing booking. `all()` returns a snapshot list.
`save(booking)` inserts or replaces that ID. All IDs used by the service refer to valid existing entities.
Missing IDs, null arguments, fractional-minute timestamps, duplicate generated IDs, and concurrent calls are outside this assignment's input domain.
Times are Instants in UTC, aligned to whole minutes. Avoid extreme Instant values that could overflow arithmetic.

`Notifier.cancellationRequested(booking)` accepts a request. It does not promise email delivery.
The core task assumes dependencies succeed. For the optional failure experiment, a RepositoryException from save occurs before any data change,
reaches the service caller, and prevents notification. Notification failures are not part of this version of the assignment.

Tests can use the provided in-memory repository for core scenarios. Never assume that this proves a real database's transaction or concurrency behaviour.
