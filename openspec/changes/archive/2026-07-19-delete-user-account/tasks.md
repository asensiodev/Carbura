## 1. Server-Side Deletion

- [x] 1.1 Add a versioned Supabase migration for authenticated `delete_current_user_account()` execution.
- [x] 1.2 Implement transactional final-member family deletion, shared-family retention, and creator transfer.
- [x] 1.3 Restrict the RPC to authenticated callers and derive the target exclusively from `auth.uid()`.

## 2. Shared Account Lifecycle

- [x] 2.1 Extend `AuthGateway` and `SupabaseAuthGateway` with RPC-backed deletion and local session clearing.
- [x] 2.2 Add an idempotent local account-data cleaner for synchronized rows and known reminder notifications.
- [x] 2.3 Add account-deletion event, operation state, failure state, and successful unauthenticated transition to `OnboardingViewModel`.
- [x] 2.4 Preserve cancellation-first handling and prevent duplicate deletion requests.

## 3. Android User Experience

- [x] 3.1 Add localized account-management danger-section copy and an explicit irreversible confirmation dialog.
- [x] 3.2 Connect deletion progress, disabled actions, and clean post-deletion navigation to the app shell.
- [x] 3.3 Keep cancellation and dismissal side-effect free.

## 4. Verification

- [x] 4.1 Add shared ViewModel tests for confirmation dispatch, duplicate suppression, success, failure, and cancellation.
- [x] 4.2 Add data tests for local row cleanup and notification cancellation.
- [x] 4.3 Add Android Compose tests for destructive-section presentation and confirmation/cancellation semantics.
- [x] 4.4 Validate the OpenSpec change strictly and run affected unit tests, static analysis, and the Android debug build.
- [x] 4.5 Document the unresolved production external deletion-request URL as a release prerequisite.
