## 1. Storage Metadata

- [x] 1.1 Expose the Desktop data-directory resolver as a read-only API.
- [x] 1.2 Add a shared Desktop database-path resolver and use it from SQLDelight driver creation.
- [x] 1.3 Extend Desktop database tests for exact paths and environment fallbacks.

## 2. Platform Actions

- [x] 2.1 Add a testable `DesktopPlatformActions` contract for opening folders and HTTPS URIs.
- [x] 2.2 Implement guarded AWT platform actions with explicit unsupported and failure results.
- [x] 2.3 Add tests that verify delegation and failure behavior without launching native applications.

## 3. Account Workspace

- [x] 3.1 Build the responsive local Account workspace with local-mode disclosure and storage paths.
- [x] 3.2 Add open-data-folder and project-website controls with snackbar feedback.
- [x] 3.3 Route Account explicitly from the Desktop shell and replace future-authentication placeholder copy.
- [x] 3.4 Add shell and workspace policy tests for the dedicated Account destination.

## 4. Verification

- [x] 4.1 Run Desktop tests, repository quality checks, and the CI-equivalent build command.
- [x] 4.2 Validate the OpenSpec change and confirm the worktree remains uncommitted.
