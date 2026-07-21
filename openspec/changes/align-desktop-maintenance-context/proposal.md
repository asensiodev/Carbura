## Why

Desktop maintenance data is vehicle-scoped, but opening the workspace from the sidebar silently selects the first vehicle. This weakens the vehicle context that Android makes explicit and can lead users to add records to a vehicle they did not intentionally choose.

## What Changes

- Require explicit vehicle selection when Maintenance is opened from the Desktop sidebar.
- Continue preselecting the vehicle when Maintenance is opened from a Garage vehicle action.
- Show a clear selection state before loading history or enabling maintenance creation.
- Preserve the Desktop vehicle switcher for efficient large-screen navigation.

## Capabilities

### New Capabilities

### Modified Capabilities
- `desktop-maintenance-workflow`: Clarify explicit vehicle selection and Garage-provided context requirements.

## Impact

- Affects Desktop shell navigation and `MaintenanceWorkspace` selection state.
- Adds Spanish Desktop copy for the unselected maintenance state.
- Does not change repositories, persistence, record ownership, or Android navigation.
