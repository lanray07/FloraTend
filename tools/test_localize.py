import tempfile
from pathlib import Path
import unittest

from localize import DeepL, emit_resources, extract, protect, restore, translate_records, validate


class FakeProvider:
    def __init__(self):
        self.calls = 0

    def translate(self, text, target):
        self.calls += 1
        return text.replace('Check', 'Prüfen')


class LocalizationTests(unittest.TestCase):
    def test_protects_repeated_positional_placeholders_and_brand(self):
        text = 'FloraTend: Check %1$s & %1$s {count}'
        protected, tokens = protect(text, ['FloraTend'])
        self.assertNotIn('FloraTend', protected)
        self.assertIn('&amp;', protected)
        self.assertEqual(restore(protected, tokens), text)

    def test_lost_or_duplicated_tokens_rejected(self):
        protected, tokens = protect('Check %1$s', [])
        with self.assertRaises(ValueError):
            restore(protected + '<x id="0" />', tokens)
        with self.assertRaises(ValueError):
            restore('Prüfen', tokens)

    def test_translation_cache(self):
        source = {'check': {'type': 'string', 'values': {'value': 'Check %1$s'}}}
        provider = FakeProvider()
        cache = {}
        first = translate_records(source, 'de-DE', 'DE', provider, cache, ['FloraTend'])
        second = translate_records(source, 'de-DE', 'DE', provider, cache, ['FloraTend'])
        self.assertEqual(first, second)
        self.assertEqual(provider.calls, 1)
        self.assertEqual(validate(source, first, [], 'de-DE'), [])
        source['check']['values']['value'] = 'Check %2$s'
        translate_records(source, 'de-DE', 'DE', provider, cache, ['FloraTend'])
        self.assertEqual(provider.calls, 2)

    def test_missing_and_invalid_placeholder(self):
        source = {'a': {'type': 'string', 'values': {'value': 'Check %1$s'}},
                  'b': {'type': 'string', 'values': {'value': 'Hello'}}}
        candidate = {'a': {'type': 'string', 'values': {'value': 'Prüfen %2$d'}}}
        errors = validate(source, candidate, [], 'de-DE')
        self.assertTrue(any('placeholder mismatch' in error for error in errors))
        self.assertTrue(any('missing translation' in error for error in errors))

    def test_plural_categories_and_other_required(self):
        source = {'tasks': {'type': 'plurals', 'values': {'other': '%d tasks'}}}
        self.assertTrue(any('plural categories' in error for error in validate(source, source, [], 'ar')))
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'strings.xml'
            path.write_text('<resources><plurals name="x"><item quantity="one">One</item></plurals></resources>')
            with self.assertRaises(ValueError):
                extract(path)

    def test_resource_round_trip_and_nontranslatable_exclusion(self):
        records = {'text': {'type': 'string', 'values': {'value': 'L\'arbre & "leaf" %1$s'}}}
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'strings.xml'
            emit_resources(records, path)
            self.assertEqual(validate(records, extract(path), [], 'en-GB'), [])
            path.write_text('<resources><string name="brand" translatable="false">FloraTend</string></resources>')
            self.assertEqual(extract(path), {})

    def test_protected_brand_must_remain(self):
        source = {'a': {'type': 'string', 'values': {'value': 'FloraTend check'}}}
        candidate = {'a': {'type': 'string', 'values': {'value': 'Flora soin'}}}
        self.assertTrue(any('protected term' in error for error in validate(source, candidate, ['FloraTend'], 'fr-FR')))

    def test_provider_requires_key_and_official_https(self):
        with self.assertRaises(ValueError):
            DeepL(endpoint='http://example.com')
        provider = DeepL()
        provider.key = None
        with self.assertRaises(RuntimeError):
            provider.translate('Hello', 'DE')

    def test_duplicate_resource_names_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / 'strings.xml'
            path.write_text('<resources><string name="x">A</string><string name="x">B</string></resources>')
            with self.assertRaises(ValueError):
                extract(path)


if __name__ == '__main__':
    unittest.main(verbosity=2)
