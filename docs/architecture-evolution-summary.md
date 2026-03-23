# GdeiAssistant Architecture Evolution Summary

**Date:** 2026-03-23
**Scope:** Structural refactors completed during the Phase 1-2 governance sessions across all four client/server codebases.

---

## 1. Android: Repository / Display Separation

### Before

Repositories directly called `context.getString(R.string.*)` to format user-facing text. This coupled the data layer to Android framework resources, made repositories untestable without a real `Context`, and scattered display logic across the data access layer.

**Responsibility boundary:** Repositories owned both data retrieval and human-readable formatting.

### After

A **DisplayMapper** pattern separates raw domain data from presentation.

- **Repositories** return raw domain objects with numeric values, date objects, and enum-like status fields. They have zero references to `context.getString()` or Android resource IDs.
- **7 DisplayMapper classes** handle all formatting:
  - `GradeDisplayMapper`
  - `ScheduleDisplayMapper`
  - `CardDisplayMapper`
  - `CETDisplayMapper`
  - `BookDisplayMapper`
  - `ExamDisplayMapper`
  - `ClassroomDisplayMapper`
- Each mapper takes a raw domain object and produces a display-ready model with pre-formatted strings.

**Responsibility boundary:** Repositories own data; DisplayMappers own formatting; ViewModels compose both.

---

## 2. WeChat Mini Program: Registry Migration

### Before

Each module page contained 4-6 `switch` blocks dispatching on item type, action type, or state. Adding a new module meant touching every switch in every page that could render or interact with that module type. The pattern was duplicated across list pages, detail pages, and action handlers.

**Responsibility boundary:** Pages owned both routing logic and per-module behavior inline.

### After

A central **`registry.js`** maps module keys to handler objects. Each handler encapsulates its own list-rendering, detail-loading, and action-dispatch logic.

- **8 module handlers** registered: grade, schedule, cet, card, book, exam, classroom, custom
- **Zero module-specific switch statements** remain in page code
- Pages call `registry.getHandler(type)` and delegate uniformly

**Responsibility boundary:** `registry.js` owns the module-to-handler mapping; handlers own per-module logic; pages own only layout and lifecycle.

---

## 3. iOS: AppContainer Split

### Before

A single `AppContainer` file exceeded 500 lines and was responsible for:
- Dependency construction for every feature
- Service wiring and configuration injection
- Navigation coordination
- Feature-specific assembly details

Every new feature required editing this monolith, and unrelated features were coupled through shared container scope.

**Responsibility boundary:** One file owned all dependency assembly for the entire application.

### After

The monolith was split into **4 feature-scoped assemblies** plus a thin top-level coordinator:

| Assembly | Scope |
|---|---|
| `CoreAssembly` | Networking, auth, persistence, shared services |
| `CampusAssembly` | Grade, schedule, CET, card, book, exam, classroom |
| `CommunityAssembly` | Secret, topic, photograph, express, marketplace, lost-and-found, delivery, dating |
| `ProfileAssembly` | User profile, settings, privacy, account management |

- The top-level `AppContainer` is now a thin coordinator that composes the four assemblies.
- Each assembly is independently testable with bootstrap tests that verify construction succeeds.

**Responsibility boundary:** Each assembly owns its own feature graph; the coordinator owns only composition.

---

## 4. Backend: ProfileController Split

### Before

`ProfileController.java` was a 589-line God Controller that handled:
- Profile CRUD
- Avatar and nickname updates
- Location formatting and validation
- Privacy option management
- Response shaping and DTO mapping

All concerns lived in a single class with long methods and mixed abstraction levels.

**Responsibility boundary:** One controller owned all profile-related HTTP handling, validation, formatting, and response shaping.

### After

The controller was decomposed into focused collaborators:

| Component | Responsibility | Approximate Size |
|---|---|---|
| `ProfileController` | HTTP routing, request binding, response delegation | ~308 lines |
| Request DTOs | Input validation and binding (one per endpoint group) | Small per-DTO |
| `ResponseMapper` | Domain-to-response conversion, field selection | Focused |
| `LocationFormatter` | Province/city/region display string assembly | Single-purpose |
| `OptionsFacade` | Privacy option read/write orchestration | Thin coordination |
| `LocationValidator` | Province/city/region validation rules | Pure validation |

**Responsibility boundary:** Controller owns routing; DTOs own input shape; ResponseMapper owns output shape; LocationFormatter and LocationValidator own location-specific logic; OptionsFacade owns multi-option coordination.

---

## Known Remaining Debt

The following items are acknowledged and intentionally deferred. They do not require reopening the completed refactors above.

| Area | Debt Item | Status |
|---|---|---|
| Backend | Several community endpoints still return unbounded lists (`/api/secret/profile`, `/api/ershou/profile`, `/api/lostandfound/profile`, `/api/delivery/mine`) | Tracked in unbounded-endpoints inventory; pagination planned for Phase 2 Track 3 |
| iOS | `MessagesViewModel` refreshes multiple sections concurrently; acceptable today but would benefit from per-section cancellation | Low priority; no correctness issue |
| WeChat | Registry currently has 8 handlers; future modules will need new handler files but no page changes | By design; no action needed until new modules ship |
| Android | DisplayMapper tests cover formatting but not error/edge cases for all 7 mappers exhaustively | Test expansion is incremental; no blocking issue |
| Backend | `QueryLogAspect` coverage was expanded but is still aspect-based rather than metric-based | Acceptable for current scale; metric instrumentation is a future track |
