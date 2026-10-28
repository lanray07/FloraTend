"""Evidence-guard regression tests. Temporary fixtures are not device evidence."""
import tempfile
import unittest
from pathlib import Path

from record_fire_results import observations, validate_suite


class FireResultGuardTests(unittest.TestCase):
    def test_exact_successful_count_is_required(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "suite.txt"
            path.write_text("OK (4 tests)\n", encoding="utf-8")
            self.assertEqual(4, validate_suite(path, 4, 0)["tests"])
            with self.assertRaises(ValueError):
                validate_suite(path, 8, 0)

    def test_failed_or_skipped_suite_cannot_hide_behind_ok(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "suite.txt"
            for failure in ["FAILURES!!!", "Error in test", "INSTRUMENTATION_FAILED", "Assumption failed", "Skipped: 1"]:
                path.write_text(failure + "\nOK (4 tests)\n", encoding="utf-8")
                with self.assertRaises(ValueError):
                    validate_suite(path, 4, 0)

    def test_old_log_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "suite.txt"
            path.write_text("OK (4 tests)\n", encoding="utf-8")
            with self.assertRaises(ValueError):
                validate_suite(path, 4, path.stat().st_mtime + 1)

    def test_missing_log_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            with self.assertRaises(ValueError):
                validate_suite(Path(folder) / "missing.txt", 4, 0)

    def test_mapping_does_not_approve_untested_release_gates(self):
        with tempfile.TemporaryDirectory() as folder:
            proposed = observations(Path(folder), {"execution_date": "2026-10-28"})
            self.assertIn("AC-03.01", proposed)
            self.assertIn("AC-04.09", proposed)
            for prefix in ["AC-07", "AC-08", "AC-12", "AC-17", "AC-18", "AC-19"]:
                self.assertFalse(any(key.startswith(prefix) for key in proposed))
            for key in ["AC-03.08", "AC-05.03", "AC-03.04", "AC-03.11", "AC-05.06", "AC-06.02", "AC-06.03", "AC-06.04", "AC-06.06", "AC-10.06"]:
                self.assertNotIn(key, proposed)


if __name__ == "__main__":
    unittest.main(verbosity=2)
