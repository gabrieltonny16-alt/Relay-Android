# OmniBlast OS

OmniBlast OS is a staged, mobile-first messaging command center for contacts, consent-aware campaigns, local queues, templates, reporting, team workspaces, and future SMS, WhatsApp, and Telegram integrations.

## Current Stage 1 prototype
- Android WebView app with a local-first responsive interface.
- Local contacts with separate SMS, WhatsApp, and Telegram consent flags and suppression controls.
- CSV import/export; imported contacts never receive consent automatically.
- Campaign drafts, local queue, templates, schedule fields, pause/resume/cancel controls, and basic local reporting.
- Demo-only inbox, automation, integrations, workspace, and billing modules that explain which backend features are not connected yet.
- Android SMS action opens the system SMS composer for manual review; it does not send directly or dispatch bulk messages.
- Payments, automatic dispatch, and provider integrations remain inactive.

## Build
The development workflow builds a debug APK using GitHub Actions on the development branch. Download the OmniBlast-OS-debug-apk artifact from a successful workflow run.

## Important limitations
The Stage 1 app is a local prototype, not the finished multi-tenant service. Secure accounts, server-enforced tenant isolation, persistent scheduling workers, provider APIs, delivery callbacks, backups, and production monitoring must be implemented and tested before production use. Browser local storage is not cloud sync.
