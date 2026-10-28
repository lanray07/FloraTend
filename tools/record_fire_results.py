"""Inspect a complete Fire test run and optionally merge its narrowly scoped results.

Default is read-only. Run --apply only after reviewing the four current suite logs.
The updater refuses failed, missing or stale logs and never grants full speech,
accessibility, screenshot-localisation, billing or release approval.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
import hashlib
import json
from pathlib import Path
import re

from report_acceptance import generate

ROOT = Path(__file__).resolve().parents[1]
SUITES = {"physical-hd10": 8, "layout-7in-large-font": 4, "layout-8in": 4, "offline-hd10": 4}
BAD_OUTPUT = re.compile(r"FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|Error in |Assumption failed|\b(?:skipped|ignored)\s*[:=]\s*[1-9]|Process crashed", re.IGNORECASE)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig")


def validate_suite(path: Path, expected: int, not_before: float) -> dict:
    if not path.is_file():
        raise ValueError(f"Required current suite log missing: {path}")
    text = read(path)
    counts = re.findall(r"^OK \((\d+) tests?\)\s*$", text, re.MULTILINE)
    if counts != [str(expected)] or BAD_OUTPUT.search(text):
        raise ValueError(f"Suite is not an unambiguous {expected}-test pass: {path.name}")
    if path.stat().st_mtime < not_before:
        raise ValueError(f"Suite log predates the verified build: {path.name}")
    return {"tests": expected, "mtime_utc": datetime.fromtimestamp(path.stat().st_mtime, timezone.utc).isoformat(),
            "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def inspect_run(root: Path) -> dict:
    build = root / "evidence/device-build.log"
    if not build.is_file() or "BUILD SUCCESSFUL" not in read(build) or "BUILD FAILED" in read(build):
        raise ValueError("The current device-build.log must contain a successful complete build")
    apk = root / "app/build/outputs/apk/debug/app-debug.apk"
    test_apk = root / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
    if not apk.is_file() or not test_apk.is_file():
        raise ValueError("Both current app and instrumentation APKs must exist")
    inputs = list((root / "app/src").rglob("*.kt"))
    inputs += list((root / "app/src").rglob("*.xml"))
    inputs += [root / "tools/test_fire.ps1", root / "app/build.gradle.kts"]
    if any(path.is_file() and path.stat().st_mtime > build.stat().st_mtime for path in inputs):
        raise ValueError("Source/test/runner configuration changed after the completed build; rerun the full runner")
    threshold = max(build.stat().st_mtime, apk.stat().st_mtime, test_apk.stat().st_mtime)
    suites = {name: validate_suite(root / f"evidence/device/{name}.txt", count, threshold) for name, count in SUITES.items()}
    physical = read(root / "evidence/device/physical-hd10.txt")
    for name, count in [("PersistenceTest", 2), ("ReminderWorkerTest", 1), ("SpeechCapabilityTest", 1), ("UiSmokeTest", 4)]:
        if not re.search(rf"^com\.floratend\.app\.{name}:\.{{{count}}}\s*$", physical, re.MULTILINE):
            raise ValueError(f"Physical suite does not show all expected successful {name} cases")
    network = root / "evidence/device/offline-network-state.txt"
    if not network.is_file() or not re.search(r"Active default network:\s*none", read(network), re.IGNORECASE):
        raise ValueError("Offline suite requires observed no-active-default-network evidence")
    if network.stat().st_mtime < threshold:
        raise ValueError("Offline network observation is stale")
    return {"suites": suites, "apk_sha256": hashlib.sha256(apk.read_bytes()).hexdigest(), "build_log_sha256": hashlib.sha256(build.read_bytes()).hexdigest(),
            "execution_date": datetime.fromtimestamp((root / "evidence/device/physical-hd10.txt").stat().st_mtime, timezone.utc).date().isoformat()}


def observations(root: Path, run: dict) -> dict:
    result = {}
    common = ["evidence/device/physical-hd10.txt", "evidence/device/offline-hd10.txt", "docs/FIRE_TEST_PLAN.md"]
    source_ui = "app/src/androidTest/java/com/floratend/app/UiSmokeTest.kt"
    source_db = "app/src/androidTest/java/com/floratend/app/PersistenceTest.kt"

    def add(key, name, actual, files=(), action="Retain the tested-device scope and execute remaining locale/device/workflow scenarios separately."):
        result[key] = {"test_name": name, "execution_date": run["execution_date"],
                       "environment": "Physical Amazon KFTRWI Fire HD 10 (2021), API 28, en-GB; separately named display overrides and no-network suite",
                       "status": "PASS", "actual_result": actual, "evidence": list(dict.fromkeys(common + list(files))), "corrective_action": action}

    add("AC-01.09", "Core navigation and startup smoke scenarios", "All four current suites passed. The tested create/edit/calendar/journal/voice-text/insights/settings and rotation scenarios completed without an uncaught crash. This is bounded smoke coverage, not all-device reliability.", [source_ui])
    add("AC-02.01", "Portrait and landscape navigation assertions", "UiSmokeTest explicitly rotated the physical HD10 between portrait and landscape and asserted the primary Add plant control remained displayed. Other locale/font/device layouts remain separate.", [source_ui, "evidence/device/layout-7in-large-font.txt", "evidence/device/layout-8in.txt"])
    add("AC-02.06", "Core navigation smoke flow", "The tested Garden, Calendar, Journal, Voice, Insights and Settings paths passed on the physical HD10 and both display overrides. The voice path used text entry when recognition was unavailable.", [source_ui])
    add("AC-03.01", "Plant creation through Compose UI", "Created a plant with nickname, species and location; asserted the resulting collection record and count. The same UI test passed with no active default network.", [source_ui])
    add("AC-03.02", "Plant edit through Compose UI", "Changed the saved species and awaited the updated visible value. The flow passed online and in the no-network suite.", [source_ui])
    add("AC-03.03", "Confirmed plant deletion", "Opened the plant delete confirmation and confirmed; awaited absence of that plant from the DAO. UI deletion and separate cascading integrity tests passed.", [source_ui, source_db])
    add("AC-03.08", "Room close/reopen plant persistence", "PersistenceTest closed and reopened a file-backed Room database and verified both saved plants and related records. Activity rotation also retained the tested collection. This is not a full OS force-stop/kill-restart matrix.", [source_db])
    add("AC-03.09", "Related-row cascade and unrelated-plant preservation", "Saved two plants, care and journal rows, deleted one plant, and verified the unrelated plant survived while only the deleted plant's related rows were removed.", [source_db])
    add("AC-03.10", "Transactional free plant limit policy", "The device DAO test accepted five free plant inserts and rejected the sixth, while still allowing an existing record edit. This verifies the authoritative storage policy, not purchase processing.", [source_db])
    add("AC-04.02", "Confirmed typed-command watering record", "An English typed Mark my plant as watered command showed confirmation; no history row existed before confirmation, then one existed after it. The next check advanced seven days. No recognised-speech-input claim is made.", [source_ui])
    add("AC-04.04", "Care schedule pause and resume", "Pause switched the action to Resume and disabled Record care; resume restored the enabled routine before later completion. Tested through Compose online and without an active default network.", [source_ui])
    add("AC-04.06", "Persisted care completion history", "File-backed Room close/reopen retained the care completion row; the UI command test separately awaited one history row after confirmation.", [source_db, source_ui])
    add("AC-04.07", "Persisted next-check recurrence", "Device tests verified the next date advances by the configured seven days after completion and survives database reopen.", [source_db, source_ui])
    add("AC-04.09", "Aggregate notification deduplication and completion refresh", "ReminderWorkerTest ran real WorkManager jobs twice, verified one notification ID100, checked the current due-count message, then verified completion updates or cancels it. This covers the tested API28 notification settings; exact-time delivery and Android13 permission handling remain unverified.", ["app/src/androidTest/java/com/floratend/app/ReminderWorkerTest.kt"])
    add("AC-05.01", "Dated journal entry through Compose UI", "Created and displayed a journal note using the default current-date entry; file-backed persistence tests retained saved journal data. Custom-date input boundaries remain outside this scenario.", [source_ui, source_db])
    add("AC-05.03", "Saved growth height measurement", "Stored a real test-fixture height of23.5cm and verified the exact value after Room close/reopen. This verifies saved measurement fidelity; regional formatting and measurement-entry boundary cases remain separate.", [source_db])
    add("AC-05.04", "Journal edit and confirmed deletion", "Edited a saved journal note, awaited the new visible text, opened and confirmed deletion, then awaited absence of the entry from the database.", [source_ui])
    add("AC-05.07", "Missing saved photograph fallback", "Imported and bounded a generated image into private storage, deleted that saved file, then asserted the journal shows the explicit missing-photograph message. External picker and camera permission flows were not exercised.", [source_ui])
    add("AC-05.09", "Local journal workflow without default network", "The offline UI suite created, displayed, edited and deleted journal notes while dumpsys connectivity reported no active default network; separate file-backed database reopen verified persisted journal data.", [source_ui, source_db, "evidence/device/offline-network-state.txt"])
    speech_file = root / "evidence/device/physical-hd10/speech-engine-test.json"
    voice_file = root / "evidence/device/physical-hd10/voice-capabilities.json"
    if speech_file.is_file() and voice_file.is_file():
        speech = json.loads(read(speech_file)); voice = json.loads(read(voice_file))
        if speech.get("recognitionAvailable") is False and voice.get("recognitionAvailable") is False and voice.get("microphonePermission") == -1:
            add("AC-06.10", "Unavailable recognizer touch/text fallback", "This physical Fire reported no SpeechRecognizer service. The test asserted Push to talk is disabled, a clear unavailable message is displayed, microphone permission remains denied, and a typed Add a new plant command opens the real editor. This does not test recognition on a capable device.", [source_ui, str(speech_file.relative_to(root)).replace("\\", "/"), str(voice_file.relative_to(root)).replace("\\", "/")])
    for key, name, actual in [
        ("AC-10.01", "Saved plant display without active network", "The offline UI suite displayed locally created/saved plant records and retained them through activity rotation."),
        ("AC-10.02", "Plant creation and edit without active network", "The offline suite created a plant, edited its saved species and awaited the updated UI value without an active default network."),
        ("AC-10.03", "Saved care schedule view without active network", "The offline suite created a saved soil-check schedule and displayed its real care actions without an active default network."),
        ("AC-10.04", "Care completion without active network", "The offline suite confirmed a typed watering-record command, then verified its history row and advanced next date without an active default network."),
        ("AC-10.05", "Journal note creation without active network", "The offline suite created and displayed a local journal note without an active default network."),
        ("AC-10.08", "Core no-account no-network smoke workflows", "The tested core plant, schedule, completion and journal actions required neither an account nor an active default network. This is bounded workflow coverage, not a full process-kill or saved-photo offline matrix.")]:
        add(key, name, actual, [source_ui, "evidence/device/offline-network-state.txt"])
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--apply", action="store_true", help="Merge only after review of the current complete logs")
    args = parser.parse_args()
    try:
        run = inspect_run(args.root)
        updates = observations(args.root, run)
        if args.apply:
            path = args.root / "evidence/acceptance-observations.json"
            current = json.loads(read(path)) if path.exists() else {}
            current.update(updates)
            # Validate and regenerate first; do not save observations on validation failure.
            rows = generate(args.root, current)
            path.write_text(json.dumps(current, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            (args.root / "evidence/device/recorded-run.json").write_text(json.dumps(run, indent=2) + "\n", encoding="utf-8")
            passed = sum(row["status"] == "PASS" for row in rows)
            print(f"Merged {len(updates)} scoped device observations; {passed}/{len(rows)} passed. Release remains BLOCKED while mandatory criteria are incomplete.")
        else:
            print(json.dumps({"mode": "read-only; no observations changed", "run": run, "proposed_requirement_ids": sorted(updates)}, indent=2))
    except (ValueError, OSError, json.JSONDecodeError) as error:
        parser.exit(1, f"No acceptance update: {error}\n")


if __name__ == "__main__":
    main()
