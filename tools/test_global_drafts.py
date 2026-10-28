"""Validate the five assistant-authored drafts; no API or device is exercised."""
import json
from pathlib import Path
import unittest

from localize import extract, validate

ROOT = Path(__file__).resolve().parents[1]
EXAMPLES = {
    "ja-JP": ["新しい植物を追加", "今日の水やりを確認する植物は", "私のMonsteraに水やり済みと記録して", "お手入れカレンダーを表示", "リマインダーを読み上げて", "リビングの植物を表示"],
    "hi-IN": ["नया पौधा जोड़ें", "आज किन पौधों की पानी देने की जाँच करनी है", "मेरे Monstera की सिंचाई पूरी दर्ज करें", "मेरी देखभाल का कैलेंडर दिखाएँ", "मेरे रिमाइंडर पढ़ें", "बैठक में पौधे दिखाएँ"],
    "ar": ["أضف نباتا جديدا", "ما النباتات التي تحتاج إلى فحص الري اليوم", "سجل سقي Monstera كمكتمل", "اعرض تقويم العناية", "اقرأ تذكيراتي", "اعرض النباتات في غرفة المعيشة"],
    "tr-TR": ["Yeni bir bitki ekle", "Bugün hangi bitkilerin sulaması kontrol edilmeli", "Monstera bitkimi sulanmış olarak işaretle", "Bakım takvimimi göster", "Hatırlatıcılarımı oku", "Oturma odasındaki bitkileri göster"],
    "id-ID": ["Tambahkan tanaman baru", "Tanaman mana yang perlu diperiksa untuk penyiraman hari ini", "Tandai Monstera sudah disiram", "Tampilkan kalender perawatan saya", "Bacakan pengingat saya", "Tampilkan tanaman di ruang tamu"],
}


def affix_capture(text, prefixes, suffixes):
    pairs = sorted(((prefix, suffix) for prefix in prefixes for suffix in suffixes if prefix or suffix), key=lambda pair: len(pair[0]) + len(pair[1]), reverse=True)
    for prefix, suffix in pairs:
        if text.startswith(prefix) and text.endswith(suffix):
            end = len(text) - len(suffix) if suffix else len(text)
            result = text[len(prefix):end].strip()
            if result:
                return result
    return None


class GlobalDraftTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = extract(ROOT / "app/src/main/res/values/strings.xml")
        cls.catalogue = json.loads((ROOT / "localization/locales.json").read_text(encoding="utf-8"))
        cls.terms = json.loads((ROOT / "localization/glossary.json").read_text(encoding="utf-8"))["protected_terms"]

    def test_all_five_xml_files_cover_source_and_preserve_placeholders_and_brand(self):
        for locale in EXAMPLES:
            with self.subTest(locale=locale):
                entry = next(item for item in self.catalogue if item["locale"] == locale)
                candidate = extract(ROOT / "app/src/main/res" / entry["android_folder"] / "strings.xml")
                self.assertEqual(set(self.source), set(candidate))
                self.assertEqual([], validate(self.source, candidate, self.terms, locale))

    def test_arabic_plural_categories_and_rtl_catalogue(self):
        candidate = extract(ROOT / "app/src/main/res/values-ar/strings.xml")
        for name in ["reminder_count", "collection_count"]:
            self.assertEqual({"zero", "one", "two", "few", "many", "other"}, set(candidate[name]["values"]))
        self.assertEqual("rtl", next(item for item in self.catalogue if item["locale"] == "ar")["direction"])

    def test_voice_dictionaries_cover_displayed_mandatory_examples_and_capture_names(self):
        locations = {"ja-JP": "リビング", "hi-IN": "बैठक", "ar": "غرفة المعيشة", "tr-TR": "Oturma odası", "id-ID": "ruang tamu"}
        for locale, examples in EXAMPLES.items():
            with self.subTest(locale=locale):
                dictionary = json.loads((ROOT / f"localization/voice/{locale}.json").read_text(encoding="utf-8"))
                raw = json.loads((ROOT / f"localization/drafts/global-{locale}.json").read_text(encoding="utf-8"))
                for example in examples:
                    self.assertIn(example, raw["voice_examples"])
                for key, example in zip(["add", "due", "calendar", "read"], [examples[0], examples[1], examples[3], examples[4]]):
                    self.assertIn(example, dictionary[key])
                self.assertEqual("Monstera", affix_capture(examples[2], dictionary["water_prefix"], dictionary["water_suffix"]))
                self.assertEqual(locations[locale], affix_capture(examples[5], dictionary["location_prefix"], dictionary.get("location_suffix", [""])))

    def test_metadata_never_implies_human_or_device_approval(self):
        report = json.loads((ROOT / "localization/drafts/group-global.json").read_text(encoding="utf-8"))
        self.assertFalse(report["release_approved"])
        self.assertEqual(5, len(report["locales"]))
        self.assertEqual([], report["validation_errors"])
        for locale in report["locales"]:
            self.assertFalse(locale["human_reviewed"])
            self.assertFalse(locale["device_UI_reviewed"])
            self.assertFalse(locale["spoken_command_tested"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
