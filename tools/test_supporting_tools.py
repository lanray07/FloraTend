"""Meaningful regression tests for evidence gating and static-site output."""
from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def load(name, file):
    spec = importlib.util.spec_from_file_location(name, file)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


reporting = load("reporting", ROOT / "tools/report_acceptance.py")
site = load("site_build", ROOT / "website/build.py")
validator = load("site_validate", ROOT / "website/validate.py")


class ReportingTests(unittest.TestCase):
    def test_real_spec_has_unique_ids_and_all_groups(self):
        source = (ROOT / "docs/spec/RELEASE_QUALITY_GATES.md").read_text(encoding="utf-8-sig")
        rows = reporting.parse_requirements(source)
        self.assertEqual(19, len({row["group"] for row in rows}))
        self.assertEqual(len(rows), len({row["requirement_id"] for row in rows}))
        self.assertEqual(source.count("- [ ]"), len(rows))

    def test_pass_without_execution_evidence_rejected(self):
        with self.assertRaises(ValueError):
            reporting.validate_observation({"status": "PASS"}, ROOT)

    def test_missing_and_outside_project_evidence_rejected(self):
        for evidence in ["does-not-exist.txt", "../outside.txt"]:
            with self.assertRaises(ValueError):
                reporting.validate_observation({"status": "PASS", "test_name": "Fixture", "execution_date": "2026-10-28", "environment": "Unit test fixture", "actual_result": "Fixture result", "evidence": [evidence]}, ROOT)

    def test_invalid_status_rejected(self):
        with self.assertRaises(ValueError):
            reporting.validate_observation({"status": "PROBABLY FINE"}, ROOT)

    def test_nonexecuted_blocker_does_not_require_fake_date(self):
        reporting.validate_observation({"status": "BLOCKED", "actual_result": "No service configured"}, ROOT)

    def test_default_report_never_infers_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "docs/spec").mkdir(parents=True)
            for filename in ["MASTER_BUILD_PROMPT.md", "RELEASE_QUALITY_GATES.md"]:
                (root / "docs/spec" / filename).write_text((ROOT / "docs/spec" / filename).read_text(encoding="utf-8-sig"), encoding="utf-8")
            rows = reporting.generate(root, {})
            self.assertTrue(all(row["status"] == "NOT TESTED" for row in rows))
            self.assertIn("0.00%", (root / "RELEASE_READINESS.md").read_text(encoding="utf-8"))
            self.assertIn("**BLOCKED**", (root / "RELEASE_READINESS.md").read_text(encoding="utf-8"))


class WebsiteTests(unittest.TestCase):
    def test_local_build_complete_and_nonindexable(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            result = site.build(output)
            self.assertFalse(result["production_configured"])
            self.assertEqual(12, result["pages"])
            self.assertIn("Disallow: /", (output / "robots.txt").read_text())
            self.assertEqual([], validator.validate(output)["errors"])

    def test_configured_domain_generates_canonicals_and_sitemap(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output, "https://example.org/floratend")
            self.assertEqual([], validator.validate(output, production=True)["errors"])
            self.assertIn("https://example.org/floratend/index.html", (output / "sitemap.xml").read_text())

    def test_unsafe_origins_rejected(self):
        for url in ["http://example.org", "https://user:secret@example.org", "https://example.org?query=1", "javascript:alert(1)"]:
            with self.assertRaises(ValueError):
                site.validate_origin(url)

    def test_no_invented_amazon_link(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output)
            self.assertNotIn("amazon.com/dp/", (output / "index.html").read_text(encoding="utf-8"))
            with self.assertRaises(ValueError):
                site.build(output, appstore_url="https://example.org/dp/fake")

    def test_broken_link_fails_validation(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output)
            (output / "support.html").unlink()
            self.assertTrue(validator.validate(output)["errors"])

    def test_unconfigured_site_fails_production_validation(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output)
            self.assertTrue(validator.validate(output, production=True)["errors"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
