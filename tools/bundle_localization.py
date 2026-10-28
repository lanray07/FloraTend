"""Bundle checked-in assistant translation drafts and locale voice properties.

This does not confer human review or release approval and makes no provider calls.
"""
from pathlib import Path
import hashlib
import json
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'app/src/main/res'
LABELS={
'en-GB':('Language','Device language','No installed voice supports this language. Written feedback is still available.'),
'en-US':('Language','Device language','No installed voice supports this language. Written feedback is still available.'),
'en-AU':('Language','Device language','No installed voice supports this language. Written feedback is still available.'),
'es-ES':('Idioma','Idioma del dispositivo','Ninguna voz instalada admite este idioma. La respuesta escrita sigue disponible.'),
'es-419':('Idioma','Idioma del dispositivo','Ninguna voz instalada admite este idioma. La respuesta escrita sigue disponible.'),
'fr-FR':('Langue','Langue de l’appareil','Aucune voix installée ne prend en charge cette langue. Les réponses écrites restent disponibles.'),
'fr-CA':('Langue','Langue de l’appareil','Aucune voix installée ne prend en charge cette langue. Les réponses écrites restent disponibles.'),
'de-DE':('Sprache','Gerätesprache','Keine installierte Stimme unterstützt diese Sprache. Schriftliche Rückmeldungen bleiben verfügbar.'),
'it-IT':('Lingua','Lingua del dispositivo','Nessuna voce installata supporta questa lingua. Le risposte scritte restano disponibili.'),
'pt-BR':('Idioma','Idioma do dispositivo','Nenhuma voz instalada é compatível com este idioma. As respostas por escrito continuam disponíveis.'),
'pt-PT':('Idioma','Idioma do dispositivo','Nenhuma voz instalada é compatível com este idioma. As respostas escritas continuam disponíveis.'),
'nl-NL':('Taal','Apparaattaal','Geen geïnstalleerde stem ondersteunt deze taal. Geschreven feedback blijft beschikbaar.'),
'pl-PL':('Język','Język urządzenia','Żaden zainstalowany głos nie obsługuje tego języka. Odpowiedzi tekstowe pozostają dostępne.'),
'sv-SE':('Språk','Enhetens språk','Ingen installerad röst stöder det här språket. Skriftliga svar är fortfarande tillgängliga.'),
'ja-JP':('言語','端末の言語','この言語に対応した音声がインストールされていません。文字による応答は引き続き利用できます。'),
'hi-IN':('भाषा','डिवाइस की भाषा','इंस्टॉल की गई कोई आवाज़ इस भाषा का समर्थन नहीं करती। लिखित जवाब उपलब्ध रहेंगे।'),
'ar':('اللغة','لغة الجهاز','لا يدعم أي صوت مثبت هذه اللغة. تظل الردود النصية متاحة.'),
'tr-TR':('Dil','Cihaz dili','Yüklü hiçbir ses bu dili desteklemiyor. Yazılı yanıtlar kullanılabilir.'),
'id-ID':('Bahasa','Bahasa perangkat','Tidak ada suara terpasang yang mendukung bahasa ini. Tanggapan tertulis tetap tersedia.'),
}
SOURCE_REVISIONS={
'language_notice':'FloraTend follows your device language. You can change it in settings.',
'language_pending':'Translations are previews and may contain mistakes. Choose a language you understand well.',
'voice_unknown':'Command not recognised. Try one of the examples below, or use the app controls.',
}

def android(value):
    return value.replace("'","\\'")

def write_xml(path,root):
    ET.indent(root)
    path.parent.mkdir(parents=True,exist_ok=True)
    ET.ElementTree(root).write(path,encoding='utf-8',xml_declaration=True)

def main():
    config=json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))
    source_path=RES/'values/strings.xml'
    source=ET.parse(source_path).getroot()
    for entry in source:
        if entry.get('name') in SOURCE_REVISIONS:entry.text=android(SOURCE_REVISIONS[entry.get('name')])
    write_xml(source_path,source)
    # Source-English also gets a concrete dictionary, rather than relying on fallback.
    en=ROOT/'localization/voice/en-GB.json'
    if not en.exists():
        en.parent.mkdir(parents=True,exist_ok=True)
        en.write_text(json.dumps({'add':['add a new plant','add plant'],'due':['which plants need watering today','which plants need a check today'],'calendar':['show my care calendar','show my plant-care calendar','show my plant care calendar'],'read':['read my reminders','read my care reminders'],'water_prefix':['mark my '],'water_suffix':[' as watered'],'location_prefix':['show plants in ']},indent=2)+'\n',encoding='utf-8')
    asset_dir=ROOT/'app/src/main/assets/voice';asset_dir.mkdir(parents=True,exist_ok=True)
    for locale in config:
        tag=locale['locale'];path=RES/locale['android_folder']/'strings.xml'
        root=ET.parse(path).getroot()
        existing={e.get('name'):e for e in root}
        for key,value in zip(('language','system_language','voice_output_unavailable'),LABELS[tag]):
            if key not in existing:existing[key]=ET.SubElement(root,'string',name=key)
            existing[key].text=android(value)
        if tag=='nl-NL':existing['pot_value'].text='Potdiameter: %1$.1f cm'
        write_xml(path,root)
        commands=json.loads((ROOT/f'localization/voice/{tag}.json').read_text(encoding='utf-8'))
        lines=['# Assistant-generated command draft; linguistic/device review pending.']
        for key,values in commands.items():
            for index,value in enumerate(values):
                escaped=value.replace('\\','\\\\').replace('\n','\\n').replace('\r','\\r').replace('\t','\\t')
                # Preserve boundary spaces in Java Properties.load(Reader).
                if escaped.startswith(' '):escaped='\\ '+escaped[1:]
                lines.append(f'{key}.{index}={escaped}')
        (asset_dir/f'{tag}.properties').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    final_hash=hashlib.sha256(source_path.read_bytes()).hexdigest()
    reviews={x['locale']:{'status':'assistant_draft_pending_human_review','source_sha256':final_hash,'reviewer':None,'reviewed_at':None,'origin':'assistant-authored; no translation-provider calls'} for x in config}
    (ROOT/'localization/review.json').write_text(json.dumps(reviews,indent=2)+'\n',encoding='utf-8')
    glossary_path=ROOT/'localization/glossary.json';glossary=json.loads(glossary_path.read_text(encoding='utf-8'))
    glossary['protected_terms']=[x for x in glossary['protected_terms'] if x!='FloraTend Premium']
    glossary['branded_values']=['FloraTend Premium']
    glossary_path.write_text(json.dumps(glossary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Bundled 19 UI locales and 19 voice dictionaries; all linguistic approvals remain pending.')

if __name__=='__main__':main()
