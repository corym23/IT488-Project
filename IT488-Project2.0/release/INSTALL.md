ATS Desktop — Install & Quick Start

Files included:

- `ATS Desktop-1.0.0.dmg` — macOS installer (unsigned)
- `ATS-Test-Summary.pdf` — test run summary and QA evidence
- `CHECKSUMS.txt` — SHA-256 checksums for the above files

macOS (install):

1. Open `ATS Desktop-1.0.0.dmg` by double-clicking it.
2. Drag the `ATS Desktop` app to your `Applications` folder.
3. First run: if macOS warns about an unidentified developer, right-click (control-click) the app and choose "Open", then confirm.

Windows (notes):

- Windows installer (.exe/.msi) is not included here. If provided, double-click the `.exe` and follow the installer.
- Unsigned installers will show SmartScreen warnings; proceed if instructor permits.

Security:

- Files are unsigned. For verification, compare SHA-256 checksums in `CHECKSUMS.txt` with locally computed sums.

Uninstall:

- macOS: delete the app from `Applications`.
- Windows: use Add/Remove Programs if installer provides one; otherwise delete the program folder.

Troubleshooting:

- If the app fails to start, check firewall settings — the app runs a local API bound to `localhost` (default port 4000). Ensure localhost is reachable.
- Report errors and include screenshots or console logs.
