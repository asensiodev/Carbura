## 1. Maintenance Wiring And Navigation

- [x] 1.1 Add the Maintenance feature and Compose runtime dependency to Desktop compilation
- [x] 1.2 Register the shared Maintenance Koin module in Desktop local mode
- [x] 1.3 Extend shell state and routing with an optional selected maintenance vehicle
- [x] 1.4 Connect Garage vehicle-history effects to Desktop Maintenance navigation

## 2. Maintenance Workspace

- [x] 2.1 Implement family-scoped vehicle loading and selection through the shared Garage overview ViewModel
- [x] 2.2 Resolve per-vehicle Maintenance history ViewModels and render loading, retry, empty, and record-list states
- [x] 2.3 Implement the Desktop maintenance form using shared field and submission events
- [x] 2.4 Render canonical/custom maintenance details and shared validation/persistence feedback
- [x] 2.5 Implement the shared future-maintenance reminder decision dialog with accurate notification copy
- [x] 2.6 Implement confirmed record deletion with progress and generated-reminder cleanup feedback

## 3. Verification

- [x] 3.1 Add Desktop integration tests for no-auth resolution, history creation/order, generated reminders, future reminder choices, and deletion
- [x] 3.2 Add shell tests for Garage-to-Maintenance vehicle navigation
- [x] 3.3 Run Desktop tests, quality checks, and the exact CI verification command
- [x] 3.4 Validate the OpenSpec change strictly and launch the Desktop Maintenance application on macOS
