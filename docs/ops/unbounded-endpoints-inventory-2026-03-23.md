# Unbounded / Unpaged Endpoints Inventory

**Date:** 2026-03-23
**Purpose:** Catalog all REST endpoints that return lists without server-side pagination or size caps, classify risk, and recommend action.

## Legend

| Classification | Meaning |
|---|---|
| **acceptable** | Bounded by nature (per-user data unlikely to grow large) or already paginated |
| **needs-pagination** | Should add `start`/`size` path params and SQL `LIMIT` |
| **needs-server-side-cap** | Add a hard `LIMIT` in SQL even without client-side paging params |
| **needs-lazy-fetch** | Currently eager-loads nested data; should defer to a separate call |

| Risk | Meaning |
|---|---|
| **low** | Bounded by user behavior; unlikely to exceed tens of rows |
| **medium** | Could reach hundreds of rows for active users |
| **high** | Could reach thousands of rows; may cause OOM or slow response |

---

## Inventory

### 1. Secret (tree-hole)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 1a | `/api/secret/profile` | GET | All personal secret posts | Unbounded `SELECT ... WHERE username=? AND state=0 ORDER BY id DESC` via `selectSecretByUsernameLight` | needs-pagination | medium |
| 1b | `/api/secret/id/{id}/comments` | GET | All comments for one secret post | Unbounded `SELECT ... WHERE content_id=? ORDER BY id` via `selectSecretCommentsByContentId` | needs-server-side-cap | low |

**Notes:** The public feed (`/api/secret/info/start/{start}/size/{size}`) is already paginated. The `selectSecretByUsername` (heavy variant with eager comment loading) is only used indirectly through the profile endpoint and compounds the problem since each row triggers an N+1 comment query. The light variant (`selectSecretByUsernameLight`) avoids N+1 but is still unbounded.

---

### 2. Marketplace (second-hand)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 2a | `/api/ershou/profile` | GET | All personal marketplace items, grouped by state (doing/sold/off) | Unbounded `SELECT ... WHERE username=? ORDER BY id DESC` via `selectItemsByUsername`, then in-memory grouping into three lists | needs-pagination | medium |

**Notes:** Public feeds (`/api/ershou/item/start/{start}`, `/api/ershou/keyword/...`, `/api/ershou/item/type/...`) all use `LIMIT`. The profile endpoint fetches all items regardless of state and groups them in Java. For heavy sellers this could return hundreds of rows.

---

### 3. Lost and Found

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 3a | `/api/lostandfound/profile` | GET | All personal lost-and-found items, grouped by state (lost/found/didfound) | Unbounded `SELECT ... WHERE username=? ORDER BY id DESC` via `selectItemByUsername`, then in-memory grouping into three lists | needs-pagination | medium |

**Notes:** Public feeds use `LIMIT`. Same pattern as marketplace -- unbounded personal profile with in-memory grouping.

---

### 4. Delivery (express pickup)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 4a | `/api/delivery/mine` | GET | Two unbounded arrays: user's published orders + user's accepted orders | `selectDeliveryOrderByUsername` (no LIMIT) and `selectAcceptedDeliveryOrderByUsername` (no LIMIT), returned as `published` + `accepted` | needs-pagination | medium |
| 4b | (mapper only) | -- | `selectDeliveryTradeByUsername` | Unbounded `SELECT ... WHERE username=?` -- not exposed via controller but available in mapper | acceptable | low |

**Notes:** The public order list (`/api/delivery/order/start/{start}/size/{size}`) is paginated. The personal endpoint returns two separate unbounded lists.

---

### 5. Dating (roommate)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 5a | `/api/dating/profile/my` | GET | All personal roommate profiles | Unbounded `SELECT ... WHERE username=?` via `selectDatingProfileByUsername` | acceptable | low |
| 5b | `/api/dating/pick/my/sent` | GET | All picks sent by user | Unbounded `SELECT ... WHERE p.username=? ORDER BY pick_id DESC` via `selectDatingPickListByUsername` (includes JOIN to dating_profile) | needs-server-side-cap | medium |
| 5c | `/api/dating/pick/my/received` | GET | All picks received by user | Unbounded `SELECT ... WHERE d.username=? ORDER BY pick_id DESC` via `selectReceivedRoommatePickListByProfileOwner` | needs-server-side-cap | medium |

**Notes:** User profiles (5a) are naturally bounded (users create few profiles). Picks (5b, 5c) can grow if a profile is popular or a user is very active.

---

### 6. Photograph (campus photos)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 6a | `/api/photograph/id/{id}/comment` | GET | All comments for one photograph | Unbounded `SELECT ... WHERE photo_id=?` via `selectPhotographCommentByPhotoId` | needs-server-side-cap | low |

**Notes:** Main lists and profile list are already paginated. Only the per-item comment list is unbounded.

---

### 7. Express (confessions)

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 7a | `/api/express/id/{id}/comment` | GET | All comments for one confession | Unbounded `SELECT ... WHERE express_id=?` via `selectExpressComment` | needs-server-side-cap | low |

**Notes:** Main feeds, profile list, and keyword search are all paginated. Only per-item comments are unbounded.

---

### 8. Topic

No unbounded endpoints found. All list endpoints use `start`/`size` pagination.

---

### 9. Profile

| # | Path | Method | Description | Current behavior | Classification | Risk |
|---|---|---|---|---|---|---|
| 9a | `/api/profile/locations` | GET | Full region list (static dictionary) | Loads all regions from `LocationUtils.getRegionMap()`, sorts in-memory | acceptable | low |

**Notes:** Region data is a static dictionary, not user-generated. Size is fixed and small.

---

## Summary by Classification

| Classification | Count | Endpoints |
|---|---|---|
| **needs-pagination** | 3 | 1a, 2a, 3a |
| **needs-server-side-cap** | 5 | 1b, 5b, 5c, 6a, 7a |
| **acceptable** | 3 | 4b, 5a, 9a |

## Recommended Priority

1. **High priority** (needs-pagination, medium risk): `/api/secret/profile`, `/api/ershou/profile`, `/api/lostandfound/profile`, `/api/delivery/mine` -- these return full user histories without any limit.
2. **Medium priority** (needs-server-side-cap): `/api/dating/pick/my/sent`, `/api/dating/pick/my/received` -- picks can accumulate over time.
3. **Low priority** (needs-server-side-cap, low risk): Comment endpoints (`/api/secret/id/{id}/comments`, `/api/photograph/id/{id}/comment`, `/api/express/id/{id}/comment`) -- individual items rarely accumulate huge comment counts but should still have a safety cap.
