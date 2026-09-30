# Task API

Small REST API for managing tasks, implemented for the Java/Spring Boot technical assessment.

## Requirements

- Java 17+
- Spring Boot
- H2
- Maven

No frontend, deployment, authentication, or external database is required.

## Run

The application uses an in-memory H2 database, so no database setup is required.

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at:

```text
http://localhost:8080/tasks
```

## Tests

Run the automated tests:

```powershell
.\mvnw.cmd clean test
```

The test suite covers the three required scenarios:

1. Valid task creation → `201 Created`
2. Invalid/blank title → `400 Bad Request`
3. Forbidden `TODO → DONE` transition → `409 Conflict`

Additional tests cover the title length boundary, listing/filtering, unknown status, `404`, backward transitions, self-transitions, and the complete `TODO → IN_PROGRESS → DONE` lifecycle.

## API

### Create a task

```http
POST /tasks
Content-Type: application/json
```

```json
{
  "title": "Learn Spring Boot",
  "description": "Study REST APIs"
}
```

Response: `201 Created`

A new task always starts with:

```json
"status": "TODO"
```

### List tasks

```http
GET /tasks
```

Optional status filter:

```http
GET /tasks?status=TODO
```

Supported statuses:

```text
TODO
IN_PROGRESS
DONE
```

An unknown status returns `400 Bad Request`.

### Update status

```http
PATCH /tasks/1/status
Content-Type: application/json
```

```json
{
  "status": "IN_PROGRESS"
}
```

Allowed transitions:

```text
TODO → IN_PROGRESS
IN_PROGRESS → DONE
```

Any other transition returns `409 Conflict`.

A missing task returns `404 Not Found`.

An unknown status returns `400 Bad Request`.

## Validation

The title is required and must contain between 1 and 120 characters **after trim**.

Examples:

- `"Learn Java"` → valid
- `"   Learn Java   "` → valid and stored as `"Learn Java"`
- `"   "` → `400 Bad Request`
- more than 120 non-whitespace characters after trim → `400 Bad Request`

## Error responses

Errors use a small structured JSON response containing:

- timestamp
- HTTP status
- error
- message
- path

No stack traces or secrets are returned.

## Time spent

To be completed with the actual time spent before submission.

## Limits

Implemented only the requested scope:

- no frontend
- no authentication
- no deployment
- no pagination
- no external database
- no additional task-management features

## AI usage

AI was used as an assistance/review tool during development. The code and design decisions are reviewed by the candidate, who must be able to explain and modify the implementation during the technical discussion.
