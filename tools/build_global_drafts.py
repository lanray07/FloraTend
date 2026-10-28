"""Emit and validate assistant-written locale drafts without a translation API.

Does not modify source-English XML, the locale catalogue or the human-review
registry. The resulting files are working drafts, never release approvals.
"""
from __future__ import annotations

import hashlib
import json
from pathlib import Path

from localize import emit_resources, extract, validate

ROOT = Path(__file__).resolve().parents[1]
LOCALES = ["ja-JP", "hi-IN", "ar", "tr-TR", "id-ID"]


def main():
    source_path = ROOT / "app/src/main/res/values/strings.xml"
    source = extract(source_path)
    catalogue = json.loads((ROOT / "localization/locales.json").read_text(encoding="utf-8"))
    terms = json.loads((ROOT / "localization/glossary.json").read_text(encoding="utf-8"))["protected_terms"]
    report = {"authorship": "assistant_generated_translation_drafts", "provider": "none; no DeepL or other translation API calls", "status": "pending_human_linguistic_and_UI_review", "release_approved": False,
              "source_file": "app/src/main/res/values/strings.xml", "source_sha256": hashlib.sha256(source_path.read_bytes()).hexdigest(), "source_resource_count": len(source), "locales": []}
    failures = []
    for locale in LOCALES:
        translation_path = ROOT / f"localization/drafts/global-{locale}.json"
        translated = json.loads(translation_path.read_text(encoding="utf-8"))
        missing, extra = set(source) - set(translated), set(translated) - set(source)
        if missing or extra:
            failures.append(f"{locale}: missing={sorted(missing)}, extra={sorted(extra)}")
            continue
        records = {}
        for key, original in source.items():
            value = translated[key]
            if original["type"] == "plurals":
                if not isinstance(value, dict):
                    raise ValueError(f"{locale}/{key}: plural mapping required")
                records[key] = {"type": "plurals", "values": value}
            else:
                if not isinstance(value, str):
                    raise ValueError(f"{locale}/{key}: string required")
                records[key] = {"type": "string", "values": {"value": value}}
        errors = validate(source, records, terms, locale)
        if errors:
            failures.extend(f"{locale}: {error}" for error in errors)
            continue
        config = next(item for item in catalogue if item["locale"] == locale)
        output = ROOT / "app/src/main/res" / config["android_folder"] / "strings.xml"
        emit_resources(records, output)
        xml_errors = validate(source, extract(output), terms, locale)
        if xml_errors:
            failures.extend(f"{locale}: emitted XML: {error}" for error in xml_errors)
            continue
        voice_path = ROOT / f"localization/voice/{locale}.json"
        voice = json.loads(voice_path.read_text(encoding="utf-8"))
        required = {"add", "due", "calendar", "read", "water_prefix", "water_suffix", "location_prefix"}
        if not required.issubset(voice) or any(not isinstance(value, list) or not value or any(not isinstance(item, str) for item in value) for value in voice.values()):
            failures.append(f"{locale}: invalid voice dictionary schema")
            continue
        entry = {"locale": locale, "android_folder": config["android_folder"], "direction": config["direction"], "resource_file": output.relative_to(ROOT).as_posix(), "resource_count": len(records), "xml_and_placeholder_validation": "PASS", "voice_dictionary": voice_path.relative_to(ROOT).as_posix(), "voice_schema_validation": "PASS", "review_status": "assistant_draft_pending_human_linguistic_and_UI_review", "human_reviewed": False, "device_UI_reviewed": False, "spoken_command_tested": False}
        report["locales"].append(entry)
        print(f"{locale}: {len(records)} resources emitted; XML, placeholders, protected terms and dictionary schema PASS; human/UI/spoken review PENDING")
    report["validation_errors"] = failures
    (ROOT / "localization/drafts/group-global.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if failures:
        for failure in failures:
            print(failure)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
