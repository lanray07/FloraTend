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
localized = load("site_localized", ROOT / "website/localized.py")


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
    def test_localized_resource_provenance_ignores_checkout_line_endings(self):
        with tempfile.TemporaryDirectory() as directory:
            left, right = Path(directory) / 'lf.xml', Path(directory) / 'crlf.xml'
            left.write_bytes('<resources>\nمرحبا\n</resources>\n'.encode('utf-8'))
            right.write_bytes(b'\xef\xbb\xbf' + left.read_bytes().replace(b'\n', b'\r\n'))
            self.assertEqual(localized.source_digest(left), localized.source_digest(right))
            right.write_text('<resources>Changed</resources>', encoding='utf-8')
            self.assertNotEqual(localized.source_digest(left), localized.source_digest(right))

    def test_localized_overviews_are_real_translations_with_reciprocal_links_and_rtl(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            report = site.build(output, "https://example.org/floratend")
            self.assertEqual(22, len(report['localized_overviews']))
            self.assertFalse(report['localized_human_approved'])
            for locale, details in report['localized_overviews'].items():
                parser = validator.PageParser()
                parser.feed((output / details['path']).read_text(encoding='utf-8'))
                self.assertEqual(locale, parser.language)
                self.assertEqual(23, len(parser.alternates))
                self.assertIn(('es', 'https://example.org/floratend/locales/es-419/index.html'), parser.alternates)
                self.assertNotIn('es-419', dict(parser.alternates))
                self.assertEqual(details['direction'], parser.direction)
            arabic = (output / 'locales/ar/index.html').read_text(encoding='utf-8')
            self.assertIn('dir="rtl"', arabic)
            self.assertNotIn('A little care. A greener home.', arabic)
            self.assertIn('lang="en-GB"', arabic)  # English-only support is labelled honestly.
            self.assertEqual([], validator.validate(output, production=True)['errors'])

    def test_missing_or_misdirected_hreflang_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output, "https://example.org/floratend")
            path = output / 'locales/fr-FR/index.html'
            original = path.read_text(encoding='utf-8')
            for modified in [original.replace('hreflang="de-DE"', 'hreflang="de"'),
                             original.replace('https://example.org/floratend/locales/de-DE/index.html', 'https://example.org/floratend/index.html')]:
                path.write_text(modified, encoding='utf-8')
                self.assertTrue(any('hreflang' in error for error in validator.validate(output, production=True)['errors']))

    def test_sitemap_duplicate_cannot_hide_a_missing_localized_url(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            site.build(output, "https://example.org/floratend")
            sitemap = output / 'sitemap.xml'
            sitemap.write_text(sitemap.read_text(encoding='utf-8').replace('/locales/ar/index.html', '/locales/fr-FR/index.html'), encoding='utf-8')
            self.assertTrue(any('sitemap' in error for error in validator.validate(output, production=True)['errors']))

    def test_local_build_complete_and_nonindexable(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory)
            result = site.build(output)
            self.assertFalse(result["production_configured"])
            self.assertEqual(34, result["pages"])
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
