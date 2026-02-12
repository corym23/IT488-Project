## ATS Desktop — Release Notes

- **Version:** 1.0.0
- **Date:** 2026-02-11

## Summary

This release packages the ATS web client as a cross-platform Electron desktop app and includes a full Selenium test report. The macOS installer (`ATS Desktop-1.0.0.dmg`) and QA artifacts are provided in this `release/` directory.

## Changes

- Added inline validation on the Attendance page with stable message id for automated tests.
- Added/updated Selenium test suite (19 tests) and refactored to a shared `TestBase` for multi-browser runs.
- Generated HTML and PDF test summaries (`ATS-Test-Summary.pdf`).
- Created Dockerfiles and `docker-compose.yml` for containerized deployment.
- Packaged Electron desktop app (macOS `.dmg` produced; Windows `.exe` can be built via CI).

## Artifacts

- macOS installer: `ATS Desktop-1.0.0.dmg` ([release/ATS%20Desktop-1.0.0.dmg](release/ATS%20Desktop-1.0.0.dmg))
- Test summary PDF: `ATS-Test-Summary.pdf` ([release/ATS-Test-Summary.pdf](release/ATS-Test-Summary.pdf))
- Checksums: `CHECKSUMS.txt` ([release/CHECKSUMS.txt](release/CHECKSUMS.txt))

## Notes

- Installers are unsigned. The CI workflow will produce unsigned macOS and Windows artifacts and attach them to a GitHub Release when a tag (e.g., `v1.0.0`) is pushed.
- For professor review: download the DMG (or CI-produced EXE), verify SHA-256 in `CHECKSUMS.txt`, and consult `ATS-Test-Summary.pdf` for test evidence.

## Contact

If anything is broken or you need a signed installer, let me know and I can add CI signing steps or provide Windows build instructions.
