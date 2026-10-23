---
name: GitHub publishing constraints
description: Publishing this workspace through Replit GitHub credentials, especially workflow scope and large attachment history.
---

When publishing to an empty GitHub remote, avoid sending the entire local history if it contains large user-uploaded source archives. Replit's Git credential and the connected GitHub API integration may have different write permissions; a successful API read does not establish that API writes are available. A `.github/workflows` change specifically requires GitHub workflow-write permission.

**Why:** The initial push of this workspace was rejected for missing workflow scope, while the separate API connection could read the repository but returned 404 for write attempts. The local history also contained large uploaded archives that were not needed in the published snapshot.

**How to apply:** Confirm the target repository and branch with the user. For an empty remote, publish a clean snapshot that excludes temporary attachments. If workflow creation is rejected, report the partial push, preserve the workflow locally, and request the required permission rather than omitting it silently or handling credentials directly.
