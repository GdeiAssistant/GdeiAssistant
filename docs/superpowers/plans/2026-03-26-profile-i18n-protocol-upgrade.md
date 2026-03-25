# Profile I18n Protocol Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade profile-related APIs and the iOS, Android, and WeChat clients to a single structured i18n-aware protocol for faculty, major, location, and hometown.

**Architecture:** The backend becomes the source of truth for profile option codes and current-language labels. `GET /api/profile/options`, `GET /api/profile/locations`, and `GET /api/user/profile` return structured objects instead of legacy display strings. iOS, Android, and WeChat are updated in rounds to consume the new contract and align their mock data with the real API.

**Tech Stack:** Spring Boot, Java, Swift, Kotlin, WeChat Mini Program JavaScript, JUnit, XCTest, Gradle, xcodebuild, Node test runner

---

### Task 1: Fix GdeiAssistant Repository Ignore Rules

**Files:**
- Modify: `.gitignore`
- Test: `git status --short`

- [ ] **Step 1: Remove ignore rules that block real documentation and test files**

Update `.gitignore` so that:
- `docs/superpowers/**` can be committed
- `src/test/java/**` is no longer ignored
- `frontend/src/**/*.test.*` and `frontend/src/**/*.spec.*` are no longer ignored
- `.superpowers/`, `.worktrees/`, and `worktrees/` stay ignored

- [ ] **Step 2: Run git status to verify the ignore rules behave as expected**

Run: `git status --short`
Expected: `.superpowers/` stays untracked and ignored; new docs/tests are no longer blocked by ignore rules

- [ ] **Step 3: Commit**

```bash
git add .gitignore
git commit -m "chore: allow profile protocol docs and tests"
```

### Task 2: Backend Protocol Model and Localization Support

**Files:**
- Modify: `src/main/java/cn/gdeiassistant/core/userprofile/pojo/UserProfileVO.java`
- Modify: `src/main/java/cn/gdeiassistant/core/userprofile/pojo/ProfileOptionsVO.java`
- Create: `src/main/java/cn/gdeiassistant/core/userprofile/pojo/ProfileValueLabelVO.java`
- Create: `src/main/java/cn/gdeiassistant/core/userprofile/pojo/ProfileLocationValueVO.java`
- Create: `src/main/java/cn/gdeiassistant/core/userprofile/service/ProfileLocalizationService.java`
- Modify: `src/main/java/cn/gdeiassistant/core/userprofile/controller/mapper/ProfileResponseMapper.java`
- Modify: `src/main/java/cn/gdeiassistant/core/userprofile/controller/ProfileController.java`
- Modify: `src/main/java/cn/gdeiassistant/core/userprofile/service/ProfileOptionsFacade.java`
- Modify: `src/main/java/cn/gdeiassistant/core/i18n/I18nFieldConfig.java`
- Test: `src/test/java/cn/gdeiassistant/core/userprofile/service/ProfileOptionsFacadeTest.java`
- Test: `src/test/java/cn/gdeiassistant/contract/ProfileOptionsContractTest.java`
- Test: `src/test/java/cn/gdeiassistant/contract/ProfileLocationContractTest.java`
- Create: `src/test/java/cn/gdeiassistant/contract/UserProfileContractTest.java`
- Create: `src/test/java/cn/gdeiassistant/core/userprofile/service/ProfileLocalizationServiceTest.java`

- [ ] **Step 1: Write failing backend tests for the new payload shape**

Cover:
- `profile/options` returns faculty `code + label + majors[{code,label}]`
- `profile/locations` returns localized region/state/city names
- `user/profile` returns object fields for `faculty`, `major`, `location`, `hometown`
- unsupported language falls back to `zh-CN`

- [ ] **Step 2: Run the targeted backend tests and confirm they fail**

Run:
- `./gradlew test --tests cn.gdeiassistant.contract.ProfileOptionsContractTest`
- `./gradlew test --tests cn.gdeiassistant.contract.UserProfileContractTest`
- `./gradlew test --tests cn.gdeiassistant.core.userprofile.service.ProfileLocalizationServiceTest`

Expected: failures showing the old payload shape and missing localization support

- [ ] **Step 3: Add minimal backend value objects and localization service**

Implement:
- a reusable `code + label` value object for faculty and major
- a location value object with `region`, `state`, `city`, `displayName`
- a localization service that:
  - normalizes request language to `zh-CN`, `zh-HK`, `zh-TW`, `en`, `ja`, `ko`
  - resolves localized faculty and major labels
  - resolves localized location node names and display strings

- [ ] **Step 4: Update controller and mapper output**

Make:
- `GET /api/profile/options` emit structured majors
- `GET /api/profile/locations` emit current-language names
- `GET /api/user/profile` emit structured `faculty`, `major`, `location`, `hometown`
- remove the old string-only fields from `UserProfileVO`

- [ ] **Step 5: Update i18n field config and static contract fixtures if needed**

Ensure the translation filter no longer targets removed legacy fields and the contract resources match the new response schema.

- [ ] **Step 6: Run targeted backend tests**

Run:
- `./gradlew test --tests cn.gdeiassistant.contract.ProfileOptionsContractTest`
- `./gradlew test --tests cn.gdeiassistant.contract.ProfileLocationContractTest`
- `./gradlew test --tests cn.gdeiassistant.contract.UserProfileContractTest`
- `./gradlew test --tests cn.gdeiassistant.core.userprofile.service.ProfileLocalizationServiceTest`
- `./gradlew test --tests cn.gdeiassistant.core.userprofile.service.ProfileOptionsFacadeTest`

Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/cn/gdeiassistant/core/userprofile src/main/java/cn/gdeiassistant/core/i18n src/test/java/cn/gdeiassistant/contract src/test/java/cn/gdeiassistant/core/userprofile
git commit -m "feat: upgrade profile api i18n protocol"
```

### Task 3: iOS Profile DTO and Mapper Upgrade

**Files:**
- Modify: `GdeiAssistant-iOS/Features/Profile/DTOs/ProfileRemoteDTO.swift`
- Modify: `GdeiAssistant-iOS/Features/Profile/Mappers/ProfileRemoteMapper.swift`
- Modify: `GdeiAssistant-iOS/Features/Profile/Models/ProfileFormSupport.swift`
- Modify: `GdeiAssistant-iOS/Features/Profile/ViewModels/ProfileViewModel.swift`
- Modify: `GdeiAssistant-iOS/Features/Profile/Repositories/RemoteProfileRepository.swift`
- Modify: `GdeiAssistant-iOSTests/Profile/ProfileViewModelTests.swift`
- Create or modify: profile mapper tests under `GdeiAssistant-iOSTests/Profile/`

- [ ] **Step 1: Write failing iOS tests for the new DTO shape**

Cover:
- profile mapping from `{ code, label }` and `{ region, state, city, displayName }`
- profile options mapping from structured majors
- location selection restoration from saved codes

- [ ] **Step 2: Run targeted iOS tests and confirm they fail**

Run:
- `xcodebuild test -project /Users/lisiyi/Projects/GdeiAssistant-iOS/GdeiAssistant-iOS.xcodeproj -scheme GdeiAssistant-iOS -destination 'id=B43CF5B3-DE30-4319-AF99-8F06038AF0D1' -only-testing:GdeiAssistant-iOSTests/Profile`

Expected: failures caused by the old string-based DTOs

- [ ] **Step 3: Update iOS DTOs and mapper with minimal structural changes**

Implement:
- faculty/major DTO objects with `code` and `label`
- location/hometown DTO objects with codes and `displayName`
- structured major options in `ProfileOptionsDTO`
- mapping to current `UserProfile`, `ProfileOptions`, and location selection state

- [ ] **Step 4: Run targeted iOS tests**

Run:
- `xcodebuild test -project /Users/lisiyi/Projects/GdeiAssistant-iOS/GdeiAssistant-iOS.xcodeproj -scheme GdeiAssistant-iOS -destination 'id=B43CF5B3-DE30-4319-AF99-8F06038AF0D1' -only-testing:GdeiAssistant-iOSTests/Profile`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add GdeiAssistant-iOS/Features/Profile GdeiAssistant-iOSTests/Profile
git commit -m "feat: consume structured profile i18n payload on ios"
```

### Task 4: iOS Mock Alignment

**Files:**
- Modify: `GdeiAssistant-iOS/Features/Profile/Repositories/MockProfileRepository.swift`
- Modify: `GdeiAssistant-iOS/Mock/MockSeedData.swift`
- Modify: `GdeiAssistant-iOSTests/Profile/ProfileFormSupportTests.swift`

- [ ] **Step 1: Write or update iOS mock consistency tests**

Verify mock profile, profile options, and location data match the real API shape and restore edit selections using codes.

- [ ] **Step 2: Update mock payload builders**

Ensure iOS mock data emits the same field structure used by the backend and remote repository.

- [ ] **Step 3: Run iOS profile-related tests**

Run:
- `xcodebuild test -project /Users/lisiyi/Projects/GdeiAssistant-iOS/GdeiAssistant-iOS.xcodeproj -scheme GdeiAssistant-iOS -destination 'id=B43CF5B3-DE30-4319-AF99-8F06038AF0D1' -only-testing:GdeiAssistant-iOSTests/Profile`

Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add GdeiAssistant-iOS/Features/Profile/Repositories/MockProfileRepository.swift GdeiAssistant-iOS/Mock/MockSeedData.swift GdeiAssistant-iOSTests/Profile
git commit -m "test: align ios profile mock with structured payload"
```

### Task 5: Android Profile Protocol Upgrade

**Files:**
- Modify: `app/src/main/java/cn/gdeiassistant/network/api/ProfileApi.kt`
- Modify: `app/src/main/java/cn/gdeiassistant/data/ProfileRepository.kt`
- Modify: `app/src/main/java/cn/gdeiassistant/data/ProfileOptionsRepository.kt`
- Modify: `app/src/main/java/cn/gdeiassistant/network/mock/MockProfileProvider.kt`
- Modify: `app/src/test/java/cn/gdeiassistant/data/ProfileOptionsRepositoryTest.kt`
- Modify: `app/src/test/java/cn/gdeiassistant/data/ProfileRepositoryDisplaySeparationTest.kt`
- Create or modify: profile repository tests under `app/src/test/java/cn/gdeiassistant/data/`

- [ ] **Step 1: Write failing Android tests for the new DTO contract**

Cover:
- structured `faculty`, `major`, `location`, `hometown` parsing
- structured `majors` option parsing
- edit state restore from location codes

- [ ] **Step 2: Run targeted Android tests and confirm they fail**

Run:
- `./gradlew testDebugUnitTest --tests cn.gdeiassistant.data.ProfileOptionsRepositoryTest --tests cn.gdeiassistant.data.ProfileRepositoryDisplaySeparationTest`

Expected: failures because DTOs still assume legacy string fields

- [ ] **Step 3: Update Android DTOs, repository mapping, and mock provider**

Implement the same structured protocol used by the backend and iOS.

- [ ] **Step 4: Run targeted Android tests**

Run:
- `./gradlew testDebugUnitTest --tests cn.gdeiassistant.data.ProfileOptionsRepositoryTest --tests cn.gdeiassistant.data.ProfileRepositoryDisplaySeparationTest`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/cn/gdeiassistant/network/api/ProfileApi.kt app/src/main/java/cn/gdeiassistant/data/ProfileRepository.kt app/src/main/java/cn/gdeiassistant/data/ProfileOptionsRepository.kt app/src/main/java/cn/gdeiassistant/network/mock/MockProfileProvider.kt app/src/test/java/cn/gdeiassistant/data
git commit -m "feat: consume structured profile i18n payload on android"
```

### Task 6: WeChat Profile Protocol Upgrade

**Files:**
- Modify: `services/endpoints.js`
- Modify: `constants/profile.js`
- Modify: `mock/mock-data.js`
- Modify: `mock/profile-handlers.js`
- Modify: `mock/index.js`
- Modify: `pages/profile/profile.js`
- Modify: `tests/profile-options.test.js`
- Create or modify: profile-related tests under `tests/`

- [ ] **Step 1: Write failing WeChat tests for the new profile payload**

Cover:
- fetching profile options with structured majors
- parsing structured profile fields
- updating and restoring location/hometown using codes

- [ ] **Step 2: Run targeted WeChat tests and confirm they fail**

Run:
- `npm test -- --test-name-pattern="profile"`

Expected: failures caused by the old profile shape assumptions

- [ ] **Step 3: Update profile constants, page logic, and mock handlers**

Implement the same structured protocol and make the mock profile state match the backend shape.

- [ ] **Step 4: Run WeChat tests**

Run:
- `npm test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add constants/profile.js mock/mock-data.js mock/profile-handlers.js mock/index.js pages/profile tests
git commit -m "feat: consume structured profile i18n payload on wechat"
```

### Task 7: Cross-Repo Verification

**Files:**
- Modify: if needed based on test failures

- [ ] **Step 1: Run backend verification**

Run:
- `./gradlew test`

Expected: PASS

- [ ] **Step 2: Run iOS verification**

Run:
- `xcodebuild test -project /Users/lisiyi/Projects/GdeiAssistant-iOS/GdeiAssistant-iOS.xcodeproj -scheme GdeiAssistant-iOS -destination 'id=B43CF5B3-DE30-4319-AF99-8F06038AF0D1'`

Expected: PASS

- [ ] **Step 3: Run Android verification**

Run:
- `./gradlew testDebugUnitTest`

Expected: PASS

- [ ] **Step 4: Run WeChat verification**

Run:
- `npm test`

Expected: PASS

- [ ] **Step 5: Record any remaining gaps**

Document known limitations, especially around professional code generation and any missing backend language resources.
