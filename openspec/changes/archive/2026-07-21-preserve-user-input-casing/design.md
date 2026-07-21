## Context

Garage names and plates, reminder titles, workshops, notes, and provider profile names are trimmed but retain case. Custom maintenance types are the exception: the original text is transformed into `maintenanceTypeId` and discarded. Exact legacy casing cannot be reconstructed.

## Goals / Non-Goals

**Goals:**

- Preserve exact meaningful casing and punctuation after trimming surrounding whitespace.
- Keep normalized IDs stable for synchronization and deterministic reminder references.
- Support local migration, remote sync, editing, and both platform UIs.
- Remain compatible with legacy rows and older remote payloads.

**Non-Goals:**

- Reconstructing irreversibly lost historical casing.
- Changing enum serialization, email identity, IDs, search normalization, or reminder identities.
- Automatically uppercasing plates or changing unaffected text fields.

## Decisions

### Store a nullable custom label beside the technical ID

`MaintenanceRecord.maintenanceTypeLabel` stores the trimmed original text only for custom types. Canonical records keep it null. A nullable additive column avoids rewriting existing rows and keeps constructors source-compatible.

### Synchronize the label directly on maintenance records

The sync-v0 maintenance record receives `maintenance_type_label`; using the separate maintenance-type catalog would require a larger ownership and lifecycle redesign. Remote decoding defaults to null for rolling compatibility.

### Resolve display from semantic fields

Canonical types use localized enum labels. Custom types use `maintenanceTypeLabel`. Legacy null-label custom records fall back to a humanized technical ID, with only a display-time initial capitalization.

## Risks / Trade-offs

- [Legacy casing is irrecoverable] -> Keep a readable fallback and upgrade the value when a user edits it.
- [Older clients omit the new remote field] -> Keep the column and DTO field nullable.
- [Technical IDs remain lowercased] -> Never use IDs as primary display text.
