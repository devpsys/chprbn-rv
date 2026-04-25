# CHPRBN Field App — PowerPoint slide outline

Use this file to build **`CHPRBN_Field_App_User_Manual.pptx`** manually: **one slide per major heading below**.  
On each slide, leave a **large blank rectangle** or “Picture placeholder” for **`[SCREENSHOT]`** as noted.

**Tip:** In PowerPoint: *Home → New Slide → Title and Content*, paste bullets, then *Insert → Pictures → Placeholder* (or empty box labeled “Paste screenshot here”).

---

## Slide 1 — Title
- **Title:** CHPRBN Field Mobile App — User Manual  
- **Subtitle:** Field officers · License lookup · Verification · Sync  
- **[SCREENSHOT:]** App icon + device frame (optional)

---

## Slide 2 — Audience & scope
- End users: field officers (adhoc accounts)  
- Covers: login, lookup, verification, verified list, sync, profile  
- **[SCREENSHOT:]** Dashboard overview

---

## Slide 3 — High-level journey
- Splash → Login or Dashboard  
- Dashboard → Scan / Verified / Sync / Profile  
- Scan / Manual entry → Record detail → Verification → Verified list  
- **[SCREENSHOT:]** Simple flow diagram (draw in PPT) or composite of 3 screens

---

## Slide 4 — Splash states
- Initializing (branding + progress)  
- Valid session → Dashboard  
- Invalid / no session → Login  
- **[SCREENSHOT:]** Splash full screen

---

## Slide 5 — Login: happy path
- Username + password  
- Sign In → Dashboard after token + profile  
- **[SCREENSHOT:]** Login filled + success transition

---

## Slide 6 — Login: validation error
- **“Username and password are required.”**  
- **[SCREENSHOT:]** Login with inline error (empty fields)

---

## Slide 7 — Login: server & network errors
- API `message` from JSON when present  
- Fallbacks: “Login failed.”, “Invalid login response.”, profile errors  
- Offline: “No cached session available for offline login.”  
- **[SCREENSHOT:]** Login with generic/API error text visible

---

## Slide 8 — Dashboard states
- Loading (spinner)  
- Success (welcome + feature tiles)  
- Error banner + **Retry**  
- **[SCREENSHOT:]** Three small panels OR three slides (one state each)

---

## Slide 9 — QR scan
- Camera permission  
- Preview + frame + torch  
- Manual entry button  
- **[SCREENSHOT:]** Scan preview  
- **[SCREENSHOT:]** Permission dialog (optional second slide)

---

## Slide 10 — Manual license entry
- License number field  
- Verify → Record detail  
- **[SCREENSHOT:]** Manual entry screen

---

## Slide 11 — Record detail: loading
- Skeleton / “Verification in progress” style  
- **[SCREENSHOT:]** Loading state

---

## Slide 12 — Record detail: success
- Photo, name, status, **Proceed to Verification**  
- **[SCREENSHOT:]** Success — Active  
- **[SCREENSHOT:]** Success — non-active (optional)

---

## Slide 13 — Record detail: not found
- “No record found” + Try Again + Enter manually  
- **[SCREENSHOT:]** Not found state

---

## Slide 14 — Record detail: error (connection UI)
- “Connection lost” + Retry connection  
- Note: underlying message may differ in ViewModel  
- **[SCREENSHOT:]** Error layout

---

## Slide 15 — Verification form
- Read-only identity fields  
- Location + remarks + Mark Verified switch  
- **[SCREENSHOT:]** Form before save

---

## Slide 16 — Verification: validation errors
- “Verification location is required.”  
- “Officer remarks are required.”  
- “Practitioner must be marked as verified.”  
- “No license record found to verify.”  
- **[SCREENSHOT:]** Red error text visible

---

## Slide 17 — Verified list & filters
- All / Active / Expired / Pending Sync  
- Badges: Pending Sync, Failed, Synced  
- **[SCREENSHOT:]** List with mixed statuses

---

## Slide 18 — Sync hub
- Loading: “Loading sync status…”  
- Stats: Total, Synced, Pending, Failed  
- Sync all · Retry failed  
- **[SCREENSHOT:]** Sync main screen

---

## Slide 19 — Sync: batch result
- Summary: “Sync all: X ok, Y failed (Z attempted)”  
- “nothing to upload” when empty  
- **[SCREENSHOT:]** Summary line visible under actions

---

## Slide 20 — Sync: errors
- Screen-level error (red text)  
- Per-row **syncError** on Failed items  
- **[SCREENSHOT:]** Top error + list with failed row

---

## Slide 21 — Profile & logout
- Cached profile  
- Logout → Login  
- **[SCREENSHOT:]** Profile + Login after logout

---

## Slide 22 — Appendix / support
- API requires online login for real token  
- QR format `#: REGISTRATION`  
- Logcat tag **QrScan** for scan diagnostics  
- **[SCREENSHOT:]** Optional — settings or support contact (if available)

---

*End of outline — duplicate slides where you need more screenshot space.*
