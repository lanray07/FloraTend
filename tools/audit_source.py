"""Small reproducible configuration audit; not a penetration or runtime privacy test."""
from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
main = ROOT / 'app/src/main'
manifest = ET.parse(main / 'AndroidManifest.xml').getroot()
android = '{http://schemas.android.com/apk/res/android}'
application = manifest.find('application')
gradle = (ROOT / 'app/build.gradle.kts').read_text()
resources = ET.parse(main / 'res/values/strings.xml').getroot()
names = [e.attrib['name'] for e in resources if e.tag == 'string']
sources = '\n'.join(p.read_text(encoding='utf-8-sig') for p in main.glob('java/**/*.kt'))
refs = set(re.findall(r'R\.string\.(\w+)', sources))
patterns = [r'sk-proj-[A-Za-z0-9_-]{20,}', r'AKIA[A-Z0-9]{16}', r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----']
checks = {
    'all_referenced_strings_exist': not (refs - set(names)),
    'source_string_names_unique': len(names) == len(set(names)),
    'android_backup_disabled': application.get(android + 'allowBackup') == 'false',
    'cleartext_network_disabled': application.get(android + 'usesCleartextTraffic') == 'false',
    'rtl_declared': application.get(android + 'supportsRtl') == 'true',
    'google_play_services_and_billing_not_declared': not re.search(r'com\.google\.android\.(?:gms|play)|com\.android\.billingclient', gradle),
    'package_identifier_configured': 'applicationId = "com.floratend.app"' in gradle,
    'version_configured': 'versionCode = 1' in gradle and 'versionName = "0.1.0"' in gradle,
    'no_recognised_source_secret_patterns': not any(re.search(pattern, sources + gradle) for pattern in patterns),
    'no_app_authentication_key_checked_in': not list(main.glob('assets/*.pem')),
    'no_background_microphone_service_declared': not manifest.findall('.//service'),
}
report = {'scope': 'Static source/configuration inspection only; runtime, dependency supply-chain and full security review remain pending.', 'checks': checks, 'missing_strings': sorted(refs - set(names))}
print(json.dumps(report, indent=2))
raise SystemExit(0 if all(checks.values()) else 1)
