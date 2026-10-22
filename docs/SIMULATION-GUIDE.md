# Guide: Contributing a Mock for an API That Is Not Yet Implemented

This is the **contribution guide** for adding a new mock service to this repo
when the **real backend does not exist yet** - i.e. the team has decided on the
contract (or a draft of it) and needs a working stand-in so downstream
developers (frontend, integration, QA, automation) can build against it.

> This is NOT the "add a stub to the existing /users example" guide (see the
> top-level README - "How to Add a New Mock"). This one covers the **whole
> flow** for introducing a brand-new service: spec, stub, test, standalone
> server, README entry, PR review, and graduation to the real API.

---

## 1. When to Use This Guide

Use this guide when **any** of the following are true:

- A new service contract just got drafted in a design doc, and the real
  backend isn't built yet.
- You're integrating with an upstream team whose endpoint is weeks away.
- You need a stub for a **contract change** (new field, new error code, new
  transition) before the real endpoint ships.
- QA / automation needs a target before the implementation is done.

**Do NOT use this guide** when the endpoint already exists and is real - in
that case, extend the existing mock for the one new behavior you need, or
flip that one path to `passThrough(true)` in WireMock so real traffic goes
through to the real service.

---

## 2. Artifacts You Must Produce

A contribution is a **set of five artifacts**, not one PR-file. Review order
is roughly this; if a reviewer cannot see any piece, the PR is rejected.

| # | Artifact | Where it lives | What's in it |
|---|----------|----------------|---------------|
| 1 | API contract spec | docs/contracts/ (see below) | method, path, fields, responses, errors, state machine |
| 2 | Stub class | src/main/java/com/example/<service>/ | registerAll(WireMockServer) |
| 3 | Test class | src/test/java/com/example/<service>/ | consumes stub, asserts contract |
| 4 | Standalone main | src/main/java/com/example/<service>/ | fixed port, prints curls |
| 5 | README section | top of README.md | start, port, scenario, admin UI |

**The contract spec is the source of truth.** Change the stub without the
spec: rejected. Change the spec without the stub: CI fails. Both done together:
merge review.

---

## 3. Standing Rules (Non-Negotiable)

1. Do not invent a port. Ports live in the Port Table at the top of README.md.
   Reserved band for new services: 8080..8099.
2. Do not invent field names. The stub and the response body must use the
   exact names from the spec. Copy-paste, not retype.
3. Name every state in a stateful service (pending, accepted, settled,
   refunded, disputed...). Never S0..S6.
4. A referenceNumber is never reused across success and error paths.
5. server.stubFor(...) is never called from a test body. Tests call
   registerAll once in @BeforeAll and only assert behavior.
6. No new JSON lib, JUnit, or WireMock version without a single pinned entry
   in pom.xml properties.
7. Stubs live in src/main, tests in src/test. Never the other way.
8. A <TBD> in the spec beats a guess. Open the PR as Draft instead of
   inventing an unknown error shape.

---

## 4. Step-by-Step Walkthrough

### Step 1 - Confirm you have enough information

From the contract owner, in writing:

- HTTP method(s) and full path(s)
- Every required field: name, type, constraint
- Every optional field, and the difference between missing and explicitly null
- Every success response shape, including any header (Location, etc.)
- Every error: HTTP status, and the exact error-code string
- If stateful: every state, every transition, and the request that fires it

Anything missing is marked TBD in the spec and the PR opens as Draft.
Never guess.

### Step 2 - Branch

    git checkout -b feat/mock-<service>-<verb>

### Step 3 - Write the spec first

Create docs/contracts/<service-verb-resource>.md from the template in
Section 5. Save it, commit it. It is the PR headline.
