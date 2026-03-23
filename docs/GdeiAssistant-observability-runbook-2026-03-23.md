# GdeiAssistant Performance & Observability Runbook

**Date:** 2026-03-23
**Audience:** On-call engineers, backend maintainers, and anyone investigating production issues.

---

## 1. Where Request Logs Live

Request logging is handled by three components in the backend (`GdeiAssistant` Spring Boot application):

### RequestCorrelationFilter

**File:** `src/main/java/cn/gdeiassistant/common/filter/RequestCorrelationFilter.java`

- Servlet filter registered in `ApplicationWebMvcConfig`
- Runs on every incoming HTTP request
- Generates a unique request ID if the client does not supply one via the `X-Request-ID` header
- Writes the request ID to:
  - The request attribute (available to downstream components)
  - The `X-Request-ID` response header (returned to the client)
  - SLF4J MDC under the key `requestId` (appears in all log lines for that request)

### RequestLogAspect

**File:** `src/main/java/cn/gdeiassistant/common/aspect/RequestLogAspect.java`

- AOP aspect that intercepts controller methods
- Logs per-request:
  - Request ID (from MDC)
  - HTTP method and request path
  - Client IP address
  - Elapsed time in milliseconds
  - Success/failure classification
- Redacts sensitive parameters (passwords, tokens) per existing policy
- Skips body logging for upload/file routes

### QueryLogAspect

**File:** `src/main/java/cn/gdeiassistant/common/aspect/QueryLogAspect.java`

- AOP aspect focused on business query methods in service classes
- Covers modules: grade, schedule, card, CET, secret, topic, express, marketplace, lostandfound, delivery, dating
- Logs query-type, target method, and elapsed time
- Used for slow-query detection (see thresholds below)

---

## 2. Tracing a Request via X-Request-ID

### End-to-end flow

```
Client request
  --> X-Request-ID header (optional; generated if absent)
    --> RequestCorrelationFilter sets MDC["requestId"]
      --> All log lines include [requestId=abc-123]
        --> Response includes X-Request-ID: abc-123
```

### How to trace

1. **From a client-side report:** Obtain the `X-Request-ID` value from the HTTP response headers. If the client logged it, search directly.

2. **Search application logs:**
   ```bash
   grep "requestId=<the-id>" /path/to/application.log
   ```
   All log lines emitted during that request's lifecycle will share the same request ID, including:
   - The RequestLogAspect entry/exit log
   - Any QueryLogAspect entries triggered during the request
   - Any application-level warn/error logs

3. **From server-side only:** If you only know the approximate time and endpoint, search by path and time window, then correlate using the request ID found in the matching log line.

### MDC fields available in log patterns

| MDC Key | Source | Content |
|---|---|---|
| `requestId` | RequestCorrelationFilter | UUID or client-supplied X-Request-ID |

Configure your log pattern to include `%X{requestId}` to ensure the request ID appears in every log line.

---

## 3. Slow Request Thresholds

Two threshold tiers are defined:

| Category | Threshold | Trigger |
|---|---|---|
| **REST request** | 800 ms | Any controller method exceeding 800ms elapsed time triggers a WARN-level log in RequestLogAspect |
| **Business query** | 1500 ms | Any query-class service method exceeding 1500ms triggers a WARN-level log in QueryLogAspect |

These thresholds are defined in `ObservabilityConstants.java` (if extracted) or inline in the respective aspect classes.

### What to do when slow-request warnings appear

1. Check the request ID to find all related log entries
2. Look for the QueryLogAspect entry to see if the slow path is in the database query layer
3. Check whether the endpoint is on the watchlist (see section 4)
4. Review recent deployment changes that may have altered query paths

---

## 4. Watchlist Endpoints (Unbounded Inventory)

The following endpoints are known to return unbounded or high-fanout results. They are the most likely sources of latency spikes under load.

| Endpoint | Risk | Current Status |
|---|---|---|
| `GET /api/secret/profile` | Returns all personal secret posts without pagination | Needs pagination |
| `GET /api/ershou/profile` | Returns full personal marketplace listing | Needs pagination or server-side cap |
| `GET /api/lostandfound/profile` | Returns full personal lost-and-found listing | Needs pagination or server-side cap |
| `GET /api/delivery/mine` | Returns both published and accepted delivery arrays unpaged | Needs pagination; backed by unpaged mapper calls |

**Reference:** Full inventory in `GdeiAssistant/docs/ops/unbounded-endpoints-inventory-2026-03-23.md`

### Why these matter

- These endpoints have no upper bound on response size
- A user with many posts/items will trigger proportionally large database queries and response payloads
- Under concurrent load, these can dominate backend thread pool and database connection usage

---

## 5. Incident Investigation: What to Check First

When a production incident is reported (slow responses, timeouts, errors), follow this order:

### Step 1: Check for slow-request WARN logs

```bash
grep "WARN.*slow" /path/to/application.log | tail -50
```

Or filter by the slow-request log pattern from RequestLogAspect. Look for:
- Which endpoints are slow
- Whether the same endpoint is slow repeatedly (systemic) or only once (transient)

### Step 2: Check watchlist endpoints

If the slow endpoint is one of the watchlist endpoints above, the cause is likely unbounded data:
- Check the affected user's data volume (how many posts/items do they have?)
- Consider whether a temporary server-side cap is needed

### Step 3: Check query-layer logs

```bash
grep "requestId=<the-id>" /path/to/application.log | grep "QueryLog"
```

Look for:
- QueryLogAspect entries showing which service method was slow
- Whether the elapsed time is concentrated in one query or spread across multiple

### Step 4: Check for N+1 patterns

If a request shows multiple QueryLogAspect entries for the same request ID, it may indicate:
- A per-item query loop that was not converted to batch
- A regression in a previously optimized path

Cross-reference with the regression test matrix (`GdeiAssistant/docs/testing/regression-matrix-2026-03-23.md`) to determine if a known regression test should have caught this.

### Step 5: Check resource exhaustion

If many requests are slow simultaneously:
- Check database connection pool usage
- Check thread pool saturation
- Check whether a watchlist endpoint is being called at high concurrency (e.g., a popular user's profile page)

### Step 6: Check client-side contribution

If the backend looks healthy but users report slowness:
- Check if the client is issuing duplicate requests (e.g., `onLoad` + `onShow` double-fire)
- Check if the client is requesting multiple expensive endpoints in parallel (e.g., iOS MessagesViewModel concurrent section refresh)
- Use X-Request-ID to distinguish client retries from genuine separate requests

---

## Log Pattern Quick Reference

| What You Want | Where to Look | Search Pattern |
|---|---|---|
| All logs for one request | Application log | `requestId=<id>` |
| All slow REST requests | Application log | WARN + RequestLogAspect slow indicator |
| All slow queries | Application log | WARN + QueryLogAspect slow indicator |
| Requests to a specific endpoint | Application log | Request path in RequestLogAspect entry log |
| Encryption failures | Application log | Encryption-related ERROR logs at startup |
| JWT failures | Application log | JWT-related ERROR logs; check for missing JWT_SECRET |
| Cron auth failures | Application log | Cron endpoint 401/403 responses |
