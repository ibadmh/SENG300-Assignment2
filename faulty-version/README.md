# Faulty version exercise

Complete your initial tests before applying this patch.

This patch introduces one bug within the published rules. Keep your reference branch unchanged.
Commit your work, create a separate branch, and run:

```sh
git apply --check /path/to/buggy-version.patch
git apply /path/to/buggy-version.patch
```

Commit this version before repairing it. Run your existing tests first, then add a focused test if needed.

Save evidence from the reference, buggy, and repaired versions. A compiler error does not count as detecting the bug.

Do not use this branch for your coverage and mutation comparisons.
