# uk-netz-app-api-request-feedback

Shared library for capturing user feedback against a request. Creates a feedback record per request and type, then accepts a single submission from an authenticated user.

## Features

- One feedback record per `requestId` + `type` (creation is idempotent)
- Single submission only; resubmission is rejected
- Expires one month after creation
- Records creator and submitter (name and ID) and submission date
- Can be switched off via feature flag
