import os
import unittest
from unittest.mock import patch
from verify_translation_provider import verify


class TranslationVerifierTests(unittest.TestCase):
    def test_missing_key_cannot_make_a_request_or_produce_pass_evidence(self):
        with patch.dict(os.environ, {'DEEPL_AUTH_KEY': ''}), patch('localize.urlopen') as request:
            with self.assertRaises(RuntimeError):
                verify()
            request.assert_not_called()

    def test_dropped_protected_tokens_cannot_produce_pass_evidence(self):
        # Synthetic failure contract only; never written as actual-provider evidence.
        with patch('verify_translation_provider.DeepL') as provider:
            provider.return_value.translate.return_value = 'Fehlende geschützte Angaben'
            with self.assertRaises(ValueError):
                verify()
            provider.assert_called_once_with(endpoint='https://api-free.deepl.com')


if __name__ == '__main__':
    unittest.main()
