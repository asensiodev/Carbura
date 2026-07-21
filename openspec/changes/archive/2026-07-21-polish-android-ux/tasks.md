## 1. Navigation Correctness TDD

- [x] 1.1 Add failing Android navigation tests for normal launch, reminder-notification launch, Garage selection, and protected-stack cleanup after sign-out.
- [x] 1.2 Introduce a canonical top-level navigation helper that always preserves Garage as the authenticated root.
- [x] 1.3 Append Reminders after authenticated initialization for notification launches instead of making it the root.
- [x] 1.4 Remove the unused or misleading `CreateMaintenance` route and keep maintenance creation inside vehicle context.
- [x] 1.5 Use lifecycle-aware state collection in Android routes and keep the navigation regression tests green.

## 2. Shared Android UX Primitives

- [x] 2.1 Add focused Compose tests for constrained content, loading, empty, and recoverable error presentations.
- [x] 2.2 Add minimal Android design-system primitives for constrained screens and reusable loading, empty, and retry states.
- [x] 2.3 Complete the dark Material color roles used by current screens and align the XML window theme and system-bar icon contrast.
- [x] 2.4 Add compact and expanded horizontal padding behavior without introducing adaptive navigation.

## 3. Garage State And Vehicle Flow TDD

- [x] 3.1 Add failing Garage ViewModel tests for initial loading, load failure, retry, mutation progress, and duplicate-submission prevention.
- [x] 3.2 Extend Garage MVI state and events to separate load, validation, persistence, and active mutation states.
- [x] 3.3 Handle repository failures and Retry while preserving local-first success effects and existing reminder confirmation behavior.
- [x] 3.4 Add UI tests for long vehicle names, large text, item-specific actions, selected vehicle type semantics, and scrollable IME-safe forms.
- [x] 3.5 Rework vehicle card hierarchy around vehicle detail and odometer actions while retaining edit/delete confirmation.
- [x] 3.6 Make vehicle creation and full editing scrollable, width-constrained, IME-safe, and accessible.
- [x] 3.7 Localize all model-supported vehicle type labels and field-associated validation text.

## 4. Vehicle Context And Maintenance TDD

- [x] 4.1 Add failing maintenance presentation tests for selected vehicle identity, explicit loading/error/retry, and local current-date initialization.
- [x] 4.2 Inject a testable local date provider and remove the fixed maintenance date.
- [x] 4.3 Load and expose compact selected-vehicle context without serializing stale vehicle data into the route.
- [x] 4.4 Separate maintenance load, validation, persistence, and active mutation states and block duplicate actions.
- [x] 4.5 Add Compose tests for vehicle context, long maintenance types, localized date/cost display, field errors, and large text.
- [x] 4.6 Reflow the maintenance destination and record cards while keeping the form scrollable and maintenance reminder integration out of scope.

## 5. Reminders Resilience TDD

- [x] 5.1 Add failing Reminders ViewModel tests for initial loading, load failure, retry, mutation progress, and no-vehicle prerequisite behavior.
- [x] 5.2 Extend Reminders MVI state and events to distinguish no vehicles, empty reminders, recoverable error, and active actions.
- [x] 5.3 Add a Garage navigation effect or callback for the no-vehicle prerequisite without coupling the feature to app navigation.
- [x] 5.4 Replace per-vehicle form buttons with a bounded accessible single-choice selector and keep Save reachable with many vehicles.
- [x] 5.5 Reflow reminder cards, replace raw missing-vehicle IDs with localized copy, and add item-specific semantics.
- [x] 5.6 Add notification-permission guidance for permanent denial without blocking reminder CRUD.

## 6. Onboarding Polish TDD

- [x] 6.1 Add onboarding tests for normalized user-facing errors, retry, and accessible initialization/error state.
- [x] 6.2 Keep technical authentication diagnostics out of primary release copy while preserving logs or debug diagnostics.
- [x] 6.3 Make onboarding scrollable, width-constrained, large-text safe, and semantically announce errors and loading.
- [x] 6.4 Remove the unused error-dismiss contract or connect it to a visible dismiss action.

## 7. Sync Feedback And Refresh TDD

- [x] 7.1 Add failing app-shell tests for non-blocking sync failure feedback, retry, success clearing, and duplicate warning suppression.
- [x] 7.2 Surface sync failure in the authenticated shell while explicitly preserving visible local content.
- [x] 7.3 Replace the no-op sync status chip with a non-interactive status presentation or a real sync action.
- [x] 7.4 Add feature refresh events and tests that update visible snapshots after successful sync without clearing open form input.
- [x] 7.5 Wire successful sync timestamps to refresh only the active destination without showing initial loading again.

## 8. Accessibility And Responsive Verification

- [x] 8.1 Add semantics tests for headings, live errors, selected controls, statuses, and item-specific edit/delete actions.
- [x] 8.2 Verify compact portrait, compact landscape, expanded width, software keyboard, and 200 percent font scale in Compose tests where stable.
- [x] 8.3 Audit all new user-facing strings for Spanish copy and keep technical OpenSpec artifacts in English.

## 9. Final Verification

- [x] 9.1 Run focused navigation, design-system, Garage, Maintenance, Reminders, Onboarding, and sync feedback tests.
- [x] 9.2 Run `./gradlew qualityCheck test assembleDebug` and resolve all regressions.
- [x] 9.3 Install the debug APK and manually verify normal launch, notification launch, top-level navigation, retry, offline sync feedback, long forms, large text, and system-bar contrast on Android.
- [x] 9.4 Confirm maintenance-generated reminders, background sync, invitations, and broader adaptive navigation remain unchanged and out of scope.

## 10. Product Interaction Refinement

- [x] 10.1 Add onboarding tests for optical centering with scroll fallback and benefit-led copy without backend vendor names.
- [x] 10.2 Center the onboarding access panel when space allows and replace infrastructure-facing copy with user-facing functionality.
- [x] 10.3 Add Garage tests for compact full-screen editing, reachable save and discard behavior, long content, and retained quick-odometer presentation.
- [x] 10.4 Replace compact full vehicle editing in `AlertDialog` with a dedicated full-screen editor while preserving form state and confirmations.
- [x] 10.5 Add compact Garage and Reminders tests for labeled thumb-reachable creation actions and unobscured final list content.
- [x] 10.6 Replace compact header add buttons with labeled extended floating actions while retaining empty-state calls to action.
- [x] 10.7 Refine vehicle card information hierarchy without changing vehicle or maintenance domain behavior.
- [x] 10.8 Audit vehicle, maintenance, and reminder deletion entry points and test item-specific confirmation before event dispatch.
- [x] 10.9 Run focused onboarding, Garage, Reminders, and destructive-action Compose tests.
- [x] 10.10 Run `./gradlew qualityCheck test assembleDebug` and smoke-test the updated flows on Android.
