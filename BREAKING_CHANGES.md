# Breaking Changes: v2.0-early-access → v2.0-early-access.2

This document describes the breaking changes introduced in `v2.0-early-access.2` of the NFON Call History API and provides migration guidance.

---

## 1. `destination_type` renamed to `service`

**Affected locations:**
- `callee.destination_type` → `callee.service`
- `caller.destination_type` → `caller.service`
- `history[].callee.destination_type` → `history[].callee.service`
- `history[].caller.destination_type` → `history[].caller.service`

The `service` property can appear on both `caller` and `callee` objects.

**Before (v2.0-early-access):**
```json
{
  "callee": {
    "name": "Test Group",
    "number": "10",
    "context": "K9999",
    "destination_type": "group"
  }
}
```

**After (v2.0-early-access.2):**
```json
{
  "callee": {
    "name": "Test Group",
    "number": "10",
    "context": "K9999",
    "service": "group"
  }
}
```

**Migration:** Rename all references from `destination_type` to `service` in your client code. The allowed values remain unchanged: `queue`, `skill`, `ivr`, `group`, `time-control`, `voicemail`.

---

## 2. `crm` moved into `custom_data`

**Affected location:** `record.crm` → `record.custom_data.crm`

**Before (v2.0-early-access):**
```json
{
  "crm": "ticket-123"
}
```

**After (v2.0-early-access.2):**
```json
{
  "custom_data": {
    "crm": "ticket-123"
  }
}
```

**Migration:** The `crm` string field was moved into the `custom_data` object. If you were reading `record.crm`, update your code to read `record.custom_data.crm` instead.

The `custom_data` object is designed to hold arbitrary user-provided key-value metadata (e.g. `crm`, `project_code`, and similar data that a user may want to attach to a call record).

---

## 3. `customer` query parameter now required for SSE (GET /records)

The `customer` query parameter is now **required** for SSE streaming requests (`Accept: text/event-stream`).

**Migration:** Add the `customer` query parameter (your K-number) to all SSE streaming requests.

**Example:**
```
GET /records?customer=K9999
Accept: text/event-stream
```

---

## 4. New fields on the `record` schema

The following fields have been added to the call record object:

| Field | Type | Description |
|-------|------|-------------|
| `call_id` | `string` | Optional internal PBX identifier for correlation. Note: this is NOT the SIP Call-ID header and cannot be used to match a call record to a SIP request. |
| `customer` | `string` | Customer number (K-number) |
| `extension` | `string` | Extension number |

**Migration:** These are additive changes. Ensure your client tolerates new unknown fields (as recommended in the API documentation). No action required unless you perform strict schema validation on responses.

---

## Summary

| Change | Impact | Action Required |
|--------|--------|-----------------|
| `destination_type` → `service` | 🔴 Breaking | Rename field references |
| `crm` → `custom_data.crm` | 🔴 Breaking | Update field path |
| `customer` param required for SSE | 🔴 Breaking | Add `customer` param to SSE requests |
| New record fields (`call_id`, `customer`,  `extension`) | 🟢 Non-breaking | Tolerate new fields |

---

## Spec Files

Both API specifications are available side by side in the `specs/` folder:

- [`openapi.yaml`](./specs/openapi.yaml) — Current version (v2.0-early-access)
- [`openapi-2.0-early-access.2.yaml`](./specs/openapi-2.0-early-access.2.yaml) — Upcoming version (v2.0-early-access.2)
