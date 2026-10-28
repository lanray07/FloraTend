"""Generate an evidence-gated release matrix from the preserved user specification.

Run: python tools/report_acceptance.py [--observations evidence/acceptance-observations.json]
Observations are keyed by AC-XX.YY; see docs/TEST_REPORTING.md. No test is inferred
from the presence of implementation code. PASS requires a dated executed test and
at least one existing evidence file. Run after each development phase.
"""
from __future__ import annotations

import argparse
import csv
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STATES = {"PASS", "FAIL", "BLOCKED", "NOT TESTED"}
PROCEDURES = {
    "AC-01": "Run Gradle debug/release builds, unit tests and lint; inspect dependencies and migration tests.",
    "AC-02": "Exercise UI on 7/8/10-inch tablet configurations, both orientations, themes and enlarged fonts; retain screenshots.",
    "AC-03": "Execute create/edit/duplicate/delete/search/filter and restart workflows; verify related records and free/Premium limits.",
    "AC-04": "Test recurrence, task completion, snooze/pause, persistence, timezone and DST boundaries and notification deduplication.",
    "AC-05": "Create/edit/delete dated entries and real photos, compare saved photos, restart offline and verify measurements.",
    "AC-06": "Run mandatory translated commands on recognition-capable hardware and unavailable-service/permission-denied fallbacks; inspect TTS.",
    "AC-07": "Validate each approved locale's resources, placeholders and plurals; inspect RTL/text expansion and record human review.",
    "AC-08": "Capture eight implemented screens per approved listing locale/device variant; validate exported dimensions, language, cropping and manifest.",
    "AC-09": "Run pipeline tests, process a new source string, validate generated resources, and test configured provider using private credentials.",
    "AC-10": "Enable airplane mode, force-stop/restart and execute saved plant, care, journal and photo workflows.",
    "AC-11": "Select/capture a photo; test consent, actual configured engine, uncertainty, correction, failure and manual entry.",
    "AC-12": "Use Amazon App Tester or Live App Testing for product lookup, purchase, cancel, failure, restore and offline entitlement scenarios.",
    "AC-13": "Inspect actual asset files, variants, optimisation and documented original creation/licensing.",
    "AC-14": "Review each approved listing locale against current official Amazon metadata limits and implemented product behaviour.",
    "AC-15": "Build website, parse crawlable HTML, validate metadata, sitemap, robots, structured data, locale links and internal links.",
    "AC-16": "Inspect permissions and network behaviour; exercise deletion/export; scan source and release artifact for secrets and compare disclosures.",
    "AC-17": "Run screen-reader, enlarged-font, contrast and target-size checks on representative devices, including touch-only operation.",
    "AC-18": "Measure startup, memory and scrolling on representative Fire hardware; exercise large photo sets and restart persistence.",
    "AC-19": "Audit signed release artifacts, device support, permissions, store assets/metadata, billing evidence and submission checklist.",
}
REPORT_GROUPS = {
    "VOICE_TEST_REPORT.md": ["AC-06"],
    "SCREENSHOT_VALIDATION_REPORT.md": ["AC-08"],
    "AMAZON_BILLING_TEST_REPORT.md": ["AC-12"],
    "FIRE_DEVICE_COMPATIBILITY.md": ["AC-02", "AC-17", "AC-18"],
    "SECURITY_AND_PRIVACY_REPORT.md": ["AC-16"],
}


def parse_requirements(source: str) -> list[dict]:
    group = None
    counts: dict[str, int] = {}
    rows = []
    for line in source.splitlines():
        heading = re.match(r"## (AC-\d{2})\s+[—-]\s+(.+)", line)
        if heading:
            group, title = heading.groups()
            counts[group] = 0
        checkbox = re.match(r"- \[ \] (.+)", line)
        if checkbox and group:
            counts[group] += 1
            rows.append({"requirement_id": f"{group}.{counts[group]:02}", "group": group,
                         "feature": title, "expected_result": checkbox.group(1),
                         "test_procedure": PROCEDURES[group]})
    if not rows or len(counts) != 19:
        raise ValueError("Expected all 19 acceptance groups and their checkbox criteria")
    return rows


def validate_observation(observation: dict, root: Path) -> None:
    state = observation.get("status", "NOT TESTED")
    if state not in STATES:
        raise ValueError(f"Invalid status: {state}")
    if state in {"PASS", "FAIL"}:
        for key in ("execution_date", "environment", "test_name", "actual_result", "evidence"):
            if not observation.get(key):
                raise ValueError(f"Executed {state} observation requires {key}")
        if not re.fullmatch(r"\d{4}-\d{2}-\d{2}(?:T.*)?", observation["execution_date"]):
            raise ValueError("Execution date must use ISO 8601")
        for reference in observation["evidence"]:
            path = (root / reference).resolve()
            if not path.is_relative_to(root.resolve()) or not path.is_file():
                raise ValueError(f"Evidence does not exist inside the project: {reference}")


def md(value: str) -> str:
    return str(value).replace("|", "\\|").replace("\n", " ")


def render_table(rows: list[dict]) -> str:
    text = "| Requirement | Test name | Expected behaviour | Date / environment | Actual result | Status | Evidence | Corrective action |\n|---|---|---|---|---|---|---|---|\n"
    for row in rows:
        text += "| " + " | ".join(md(row[key]) for key in (
            "requirement_id", "test_name", "expected_result", "execution_context", "actual_result",
            "status", "evidence", "corrective_action")) + " |\n"
    return text


def generate(root: Path, observations: dict) -> list[dict]:
    source_path = root / "docs/spec/RELEASE_QUALITY_GATES.md"
    rows = parse_requirements(source_path.read_text(encoding="utf-8-sig"))
    known_ids = {row["requirement_id"] for row in rows}
    unknown = set(observations) - known_ids
    if unknown:
        raise ValueError(f"Unknown requirement IDs: {sorted(unknown)}")
    for row in rows:
        observation = observations.get(row["requirement_id"], {})
        validate_observation(observation, root)
        row.update({"test_name": observation.get("test_name", row["feature"]),
                    "execution_date": observation.get("execution_date", ""),
                    "environment": observation.get("environment", "Not executed"),
                    "actual_result": observation.get("actual_result", "No verified test result recorded."),
                    "status": observation.get("status", "NOT TESTED"),
                    "evidence": "; ".join(observation.get("evidence", [])),
                    "corrective_action": observation.get("corrective_action", "Execute the stated procedure and attach evidence."),
                    "test_procedure": observation.get("test_procedure", row["test_procedure"])})
        row["execution_context"] = (row["execution_date"] + " / " + row["environment"]).strip()
    fields = ["requirement_id", "test_name", "execution_date", "environment", "expected_result", "test_procedure", "actual_result", "status", "evidence", "corrective_action"]
    with (root / "ACCEPTANCE_TEST_RESULTS.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)
    master = (root / "docs/spec/MASTER_BUILD_PROMPT.md").read_text(encoding="utf-8-sig")
    sections = re.findall(r"^#{1,3} (\d+\..+)$", master, re.MULTILINE)
    criteria = "# FloraTend acceptance criteria\n\nGenerated from the user's preserved [quality gates](docs/spec/RELEASE_QUALITY_GATES.md). Each checkbox is one mandatory criterion; locale-wide criteria require all approved locales, not one sample. The [master build specification](docs/spec/MASTER_BUILD_PROMPT.md) also remains authoritative. Statuses measure executed verification, not source-code completeness.\n\n"
    for group in PROCEDURES:
        group_rows = [row for row in rows if row["group"] == group]
        criteria += f"## {group} — {group_rows[0]['feature']}\n\nProcedure: {PROCEDURES[group]}\n\n" + render_table(group_rows) + "\n"
    criteria += "## Master specification sections\n\n" + "\n".join("- " + section for section in sections) + "\n"
    (root / "ACCEPTANCE_CRITERIA.md").write_text(criteria, encoding="utf-8")
    passed = sum(row["status"] == "PASS" for row in rows)
    counts = {state: sum(row["status"] == state for row in rows) for state in sorted(STATES)}
    release = "READY FOR SUBMISSION" if passed == len(rows) else "BLOCKED"
    readiness = f"# Release readiness\n\n**{release}**\n\nVerified acceptance rate: **{passed} / {len(rows)} = {passed / len(rows) * 100:.2f}%**. BLOCKED and NOT TESTED criteria count as incomplete.\n\n"
    readiness += "| Status | Mandatory criteria |\n|---|---:|\n" + "".join(f"| {state} | {count} |\n" for state, count in counts.items())
    readiness += "\nThe criterion-level evidence, expected behaviour and corrective actions are in [ACCEPTANCE_TEST_RESULTS.csv](ACCEPTANCE_TEST_RESULTS.csv) and [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md). This decision is conservative: no release is approved while any mandatory criterion remains unverified. Publication additionally requires explicit user authorisation.\n\n"
    readiness += "## Incomplete gates\n\n"
    for group in PROCEDURES:
        pending = [row for row in rows if row["group"] == group and row["status"] != "PASS"]
        if pending:
            readiness += f"- {group} — {pending[0]['feature']}: {len(pending)} criteria incomplete.\n"
    (root / "RELEASE_READINESS.md").write_text(readiness, encoding="utf-8")
    for filename, groups in REPORT_GROUPS.items():
        content = f"# {filename.removesuffix('.md').replace('_', ' ').title()}\n\nGenerated criterion-level verification report. No unrecorded result is inferred. For detailed test procedures consult [ACCEPTANCE_CRITERIA.md](ACCEPTANCE_CRITERIA.md).\n\n"
        content += render_table([row for row in rows if row["group"] in groups])
        (root / filename).write_text(content, encoding="utf-8")
    return rows


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--observations", type=Path, default=ROOT / "evidence/acceptance-observations.json")
    args = parser.parse_args()
    observations = json.loads(args.observations.read_text(encoding="utf-8")) if args.observations.exists() else {}
    rows = generate(args.root, observations)
    passed = sum(row["status"] == "PASS" for row in rows)
    print(f"Generated {len(rows)} mandatory criteria; {passed} passed; {passed / len(rows) * 100:.2f}% verified.")


if __name__ == "__main__":
    main()
