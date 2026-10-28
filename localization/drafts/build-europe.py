"""Reproduce assistant-authored translation drafts; does not call a translation API."""
from pathlib import Path
from collections import Counter
from xml.sax.saxutils import escape
import xml.etree.ElementTree as ET
import json
import hashlib
import re

ROOT = Path(__file__).resolve().parents[2]
LOCALES = ['it-IT', 'pt-BR', 'pt-PT', 'nl-NL', 'pl-PL', 'sv-SE']
FOLDERS = {x['locale']: x['android_folder'] for x in json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))}
SOURCE = ROOT/'app/src/main/res/values/strings.xml'
TREE = ET.parse(SOURCE).getroot()
PH = re.compile(r'%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]')
rows = [line.split('|') for line in (ROOT/'localization/drafts/group-europe.tsv').read_text(encoding='utf-8-sig').splitlines() if line.strip()]
assert all(len(row) == 7 for row in rows), 'Invalid translation table row'
assert len({r[0] for r in rows}) == len(rows), 'Duplicate translation key'
translations = {locale: {row[0]: row[i+1] for row in rows} for i,locale in enumerate(LOCALES)}

plurals = {
 'it-IT': {
  'reminder_count': {'one': '%1$d controllo di cura da esaminare.', 'other': '%1$d controlli di cura da esaminare.'},
  'collection_count': {'one': 'La tua collezione · %1$d pianta', 'other': 'La tua collezione · %1$d piante'}},
 'pt-BR': {
  'reminder_count': {'one': '%1$d verificação de cuidados para revisar.', 'other': '%1$d verificações de cuidados para revisar.'},
  'collection_count': {'one': 'Sua coleção · %1$d planta', 'other': 'Sua coleção · %1$d plantas'}},
 'pt-PT': {
  'reminder_count': {'one': '%1$d verificação de cuidados para rever.', 'other': '%1$d verificações de cuidados para rever.'},
  'collection_count': {'one': 'A sua coleção · %1$d planta', 'other': 'A sua coleção · %1$d plantas'}},
 'nl-NL': {
  'reminder_count': {'one': '%1$d verzorgingscontrole om te bekijken.', 'other': '%1$d verzorgingscontroles om te bekijken.'},
  'collection_count': {'one': 'Je verzameling · %1$d plant', 'other': 'Je verzameling · %1$d planten'}},
 'pl-PL': {
  'reminder_count': {'one': '%1$d kontrola pielęgnacji do przejrzenia.', 'few': '%1$d kontrole pielęgnacji do przejrzenia.', 'many': '%1$d kontroli pielęgnacji do przejrzenia.', 'other': '%1$d kontroli pielęgnacji do przejrzenia.'},
  'collection_count': {'one': 'Twoja kolekcja · %1$d roślina', 'few': 'Twoja kolekcja · %1$d rośliny', 'many': 'Twoja kolekcja · %1$d roślin', 'other': 'Twoja kolekcja · %1$d rośliny'}},
 'sv-SE': {
  'reminder_count': {'one': '%1$d skötselkontroll att granska.', 'other': '%1$d skötselkontroller att granska.'},
  'collection_count': {'one': 'Din samling · %1$d växt', 'other': 'Din samling · %1$d växter'}}}

voice = {
 'it-IT': {'add': ['aggiungi una nuova pianta', 'aggiungi pianta'], 'due': ['quali piante hanno bisogno di acqua oggi', 'quali piante devo controllare oggi'], 'calendar': ['mostra il mio calendario di cura', 'apri il calendario di cura'], 'read': ['leggi i miei promemoria', 'leggi i promemoria di cura'], 'water_prefix': ['segna la mia ', 'segna il mio ', 'segna ', 'contrassegna la mia '], 'water_suffix': [' come annaffiata', ' come annaffiato'], 'location_prefix': ['mostra le piante in ', 'mostra piante in ', 'mostra le piante nel ', 'mostra le piante nella ']},
 'pt-BR': {'add': ['adicionar uma nova planta', 'adicione uma nova planta', 'adicionar planta'], 'due': ['quais plantas precisam de água hoje', 'quais plantas devo verificar hoje'], 'calendar': ['mostrar meu calendário de cuidados', 'mostre meu calendário de cuidados'], 'read': ['ler meus lembretes', 'leia meus lembretes'], 'water_prefix': ['marcar minha ', 'marque minha ', 'marcar meu ', 'marque meu ', 'marcar '], 'water_suffix': [' como regada', ' como regado'], 'location_prefix': ['mostrar plantas na ', 'mostrar plantas no ', 'mostre plantas na ', 'mostre plantas no ', 'mostrar plantas em ']},
 'pt-PT': {'add': ['adicionar uma nova planta', 'adiciona uma nova planta', 'adicionar planta'], 'due': ['que plantas precisam de água hoje', 'quais plantas precisam de água hoje', 'que plantas devo verificar hoje'], 'calendar': ['mostrar o meu calendário de cuidados', 'mostra o meu calendário de cuidados'], 'read': ['ler os meus lembretes', 'lê os meus lembretes'], 'water_prefix': ['marcar a minha ', 'marca a minha ', 'marcar o meu ', 'marca o meu ', 'marcar '], 'water_suffix': [' como regada', ' como regado'], 'location_prefix': ['mostrar plantas na ', 'mostrar plantas no ', 'mostra plantas na ', 'mostra plantas no ', 'mostrar plantas em ']},
 'nl-NL': {'add': ['voeg een nieuwe plant toe', 'voeg een plant toe', 'plant toevoegen'], 'due': ['welke planten hebben vandaag water nodig', 'welke planten moet ik vandaag controleren'], 'calendar': ['toon mijn verzorgingskalender', 'open mijn verzorgingskalender'], 'read': ['lees mijn herinneringen', 'lees mijn herinneringen voor'], 'water_prefix': ['markeer mijn ', 'markeer '], 'water_suffix': [' als bewaterd', ' als water gegeven'], 'location_prefix': ['toon planten in de ', 'toon planten in het ', 'toon planten in ']},
 'pl-PL': {'add': ['dodaj nową roślinę', 'dodaj roślinę'], 'due': ['które rośliny potrzebują dzisiaj wody', 'które rośliny trzeba dzisiaj podlać', 'które rośliny mam dzisiaj sprawdzić'], 'calendar': ['pokaż mój kalendarz pielęgnacji', 'pokaż kalendarz pielęgnacji'], 'read': ['przeczytaj moje przypomnienia', 'odczytaj moje przypomnienia'], 'water_prefix': ['oznacz roślinę ', 'oznacz moją ', 'oznacz mój ', 'oznacz '], 'water_suffix': [' jako podlaną', ' jako podlany'], 'location_prefix': ['pokaż rośliny w ', 'pokaż rośliny na ']},
 'sv-SE': {'add': ['lägg till en ny växt', 'lägg till en växt'], 'due': ['vilka växter behöver vatten idag', 'vilka växter ska jag kontrollera idag'], 'calendar': ['visa min skötselkalender', 'öppna min skötselkalender'], 'read': ['läs mina påminnelser', 'läs upp mina påminnelser'], 'water_prefix': ['markera min ', 'markera mitt ', 'markera '], 'water_suffix': [' som vattnad', ' som vattnat'], 'location_prefix': ['visa växter i ', 'visa växterna i ']}}

def android(text):
    return escape(text.replace('\\', '\\\\').replace("'", "\\'").replace('"', '\\"'))

results = []
source_strings = {n.get('name'): n.text or '' for n in TREE.findall('string')}
for locale in LOCALES:
    mapping = translations[locale]
    missing = sorted(set(source_strings)-set(mapping))
    extra = sorted(set(mapping)-set(source_strings))
    # Root adds locale-picker resources centrally; don't invent English fallbacks.
    assert not extra, (locale, 'extra source keys', extra)
    lines = ["<?xml version='1.0' encoding='utf-8'?>", '<resources>', '  <!-- Assistant-generated translation draft. Pending human review; not release-approved. -->']
    for node in TREE:
        name = node.get('name')
        if node.tag == 'string':
            if name not in mapping:
                continue
            text = mapping[name]
            assert Counter(PH.findall(source_strings[name])) == Counter(PH.findall(text)), (locale,name,'placeholder mismatch')
            lines.append(f'  <string name="{name}">{android(text)}</string>')
        elif node.tag == 'plurals':
            forms = plurals[locale][name]
            source_tokens = Counter(PH.findall(next(iter(node)).text or ''))
            assert 'other' in forms
            if locale == 'pl-PL': assert set(forms)=={'one','few','many','other'}
            lines.append(f'  <plurals name="{name}">')
            for quantity,text in forms.items():
                assert source_tokens==Counter(PH.findall(text)), (locale,name,quantity,'placeholder mismatch')
                lines.append(f'    <item quantity="{quantity}">{android(text)}</item>')
            lines.append('  </plurals>')
    lines.append('</resources>')
    folder=ROOT/'app/src/main/res'/FOLDERS[locale]
    folder.mkdir(parents=True,exist_ok=True)
    file=folder/'strings.xml'
    file.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    ET.parse(file)
    voice_file=ROOT/'localization/voice'/f'{locale}.json'
    voice_file.parent.mkdir(parents=True,exist_ok=True)
    voice_file.write_text(json.dumps(voice[locale],ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    assert all(isinstance(v,list) and v and all(isinstance(x,str) and x for x in v) for v in voice[locale].values())
    results.append({'locale':locale,'resource_file':str(file.relative_to(ROOT)).replace('\\','/'),'strings':len(mapping),'plurals':len(plurals[locale]),'missing_current_source_keys':missing,'xml_parse':'PASS','placeholder_counts':'PASS','human_review':'PENDING','device_voice_test':'NOT TESTED','voice_dictionary':str(voice_file.relative_to(ROOT)).replace('\\','/')})

metadata={'created':'2026-10-28','provenance':'assistant-generated from own linguistic ability','translation_api_used':False,'provider':'none; not DeepL','release_approved':False,'review_status':'pending_human_review','source_sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'protected_terms':['FloraTend','FloraTend Premium','Premium','Amazon','Android','CSV','JSON','Monstera','cm','YYYY-MM-DD'],'source_meanings_coordinated_with_root':['language_notice','language_pending','voice_unknown'],'editable_draft':'localization/drafts/group-europe.tsv','reproduction':'python localization/drafts/build-europe.py','results':results,'limits':['XML and placeholder validation do not establish linguistic approval.','All six locales require native-speaker human review and rendered layout checks.','All voice locales require actual speech-engine and TTS verification.','Voice examples preserve stored plant nicknames; inflected nicknames may require touch selection.']}
(ROOT/'localization/drafts/group-europe.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(results,ensure_ascii=False,indent=2))
