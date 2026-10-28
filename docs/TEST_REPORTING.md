# Evidence-backed acceptance reporting

Run `python tools/report_acceptance.py` after each development phase. The two original user attachments are preserved in `docs/spec/`; the build prompt ended mid-sentence in its Phase 5 section when supplied.

The generator assigns stable criterion IDs in source order, e.g. `AC-01.01`. Every checkbox is mandatory. An absent observation becomes NOT TESTED. PASS and FAIL require execution date, environment, actual result, test name and existing project evidence files. A source file is evidence of implementation only; it is not evidence that a user workflow passed.

Record observations in `evidence/acceptance-observations.json` (a JSON object keyed by criterion ID). Example structure, not a test result:

```json
{
  "AC-01.02": {
    "test_name": "Gradle debug compilation",
    "execution_date": "2026-10-28",
    "environment": "Windows / Java 17 / Android SDK",
    "status": "NOT TESTED",
    "actual_result": "Awaiting execution",
    "evidence": [],
    "corrective_action": "Run the Gradle task and retain its output"
  }
}
```

The tool rejects unknown IDs, invalid states, undated executed results and missing evidence paths. It cannot establish that an attached file proves a result; reviewers must assess that correspondence. Never use synthetic test fixtures as live service/device evidence. Live Amazon purchase tests, human translation review, locale screenshots and representative hardware results remain incomplete until actually performed.

`LOCALIZATION_TEST_REPORT.md` is managed by the translation pipeline. Other required specialised reports are regenerated from criterion observations. Keep deeper execution detail in evidence files and link those from the observations rather than manually editing generated reports.
