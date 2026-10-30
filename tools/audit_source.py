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
product = json.loads((ROOT / 'billing/product-metadata.json').read_text(encoding='utf-8-sig'))
fixture = json.loads((ROOT / 'billing/amazon.sdktester.json').read_text(encoding='utf-8-sig'))
sdk_sku = re.search(r'const val PREMIUM_SKU = "([^"]+)"', sources).group(1)
checks = {
    'all_referenced_strings_exist': not (refs - set(names)),
    'source_string_names_unique': len(names) == len(set(names)),
    'android_backup_disabled': application.get(android + 'allowBackup') == 'false',
    'cleartext_network_disabled': application.get(android + 'usesCleartextTraffic') == 'false',
    'rtl_declared': application.get(android + 'supportsRtl') == 'true',
    'amazon_iap_package_visibility': {'com.amazon.sdktestclient','com.amazon.venezia'}.issubset({e.get(android+'name') for e in manifest.findall('./queries/package')}),
    'amazon_drm_receiver': any(e.get(android+'name')=='com.amazon.device.drm.ResponseReceiver' and e.get(android+'exported')=='true' and e.get(android+'permission')=='com.amazon.drm.Permission.NOTIFY' and e.find('./intent-filter/action').get(android+'name')=='com.amazon.drm.NOTIFY' for e in application.findall('receiver')),
    'billing_sku_matches_store_metadata_and_isolated_fixture': product['sku'] == sdk_sku and set(fixture) == {sdk_sku},
    'iap_metadata_fields_within_console_limits': all(0 < len(m['title']) <= 128 and 0 < len(m['description']) <= 1200 for m in product['locales'].values()),
    'google_play_services_and_billing_not_declared': not re.search(r'com\.google\.android\.(?:gms|play)|com\.android\.billingclient', gradle),
    'package_identifier_configured': 'applicationId = "com.floratend.app"' in gradle,
    'version_configured': 'versionCode = 5' in gradle and 'versionName = "0.1.4"' in gradle,
    'fire_os_16_target_api': 'compileSdk = 36' in gradle and 'targetSdk = 36' in gradle,
    'no_recognised_source_secret_patterns': not any(re.search(pattern, sources + gradle) for pattern in patterns),
    'authentication_assets_are_public_keys': all(b'BEGIN PUBLIC KEY' in p.read_bytes() and b'PRIVATE KEY' not in p.read_bytes() for p in main.glob('assets/*.pem')),
    'no_background_microphone_service_declared': not manifest.findall('.//service'),
}
report = {'scope': 'Static source/configuration inspection only; runtime, dependency supply-chain and full security review remain pending.', 'checks': checks, 'missing_strings': sorted(refs - set(names))}
print(json.dumps(report, indent=2))
raise SystemExit(0 if all(checks.values()) else 1)
