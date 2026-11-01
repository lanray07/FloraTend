"""Check draft listing field structure against documented Amazon limits; no linguistic approval."""
from pathlib import Path
import json,re
from datetime import date
ROOT=Path(__file__).resolve().parents[1]
ALLOWED={'locale','review_status','display_title','short_description','long_description','feature_bullets','keywords'}
def audit():
    rows=[]
    for path in sorted((ROOT/'submission/listings').glob('*.json')):
        data=json.loads(path.read_text(encoding='utf-8-sig'))
        errors=[]
        if set(data)!=ALLOWED:errors.append('Unexpected or missing draft fields')
        for key in ['display_title','short_description','long_description','keywords']:
            if not isinstance(data.get(key),str) or not data[key].strip():errors.append(f'Empty {key}')
        if len(data['short_description'].encode('utf-8'))>2000:errors.append('Short description exceeds2000UTF8bytes')
        if len(data['long_description'])>4000:errors.append('Long description exceeds4000characters')
        if not 3<=len(data['feature_bullets'])<=5 or any(not x.strip() for x in data['feature_bullets']):errors.append('Expected3–5nonemptybullets')
        if 'FloraTend' not in data['display_title']:errors.append('Missing brand')
        text=' '.join(data[k] for k in ['display_title','short_description','long_description','keywords'])+' '+' '.join(data['feature_bullets'])
        if re.search(r'B0[A-Z0-9]{8}|£|€|\$\s*\d|\b(?:GBP|USD|JPY)\s*\d',text):errors.append('Potential forbidden price/ASIN')
        terms=[x.strip().casefold() for x in data['keywords'].split(',')]
        if not all(terms) or len(set(terms))!=len(terms):errors.append('Empty/duplicated keyword terms')
        if data['review_status']!='draft_pending_human_review':errors.append('Unexpected review claim')
        rows.append({'locale':data['locale'],'title_characters':len(data['display_title']),'short_utf8_bytes':len(data['short_description'].encode('utf-8')),'long_characters':len(data['long_description']),'feature_bullets':len(data['feature_bullets']),'keyword_terms':len(terms),'errors':errors})
    return {'execution_date':date.today().isoformat(),'scope':'Draft fields/limits only; no human linguistic, rights, market terminology or overall policy approval. Display-title numeric maximum requires console validation.','sources':['https://developer.amazon.com/docs/app-submission/appstore-details.html','https://developer.amazon.com/docs/policy-center/listing-promo.html'],'human_approved':False,'locales':rows}
if __name__=='__main__':
    report=audit();print(json.dumps(report,ensure_ascii=False,indent=2))
    raise SystemExit(1 if not report['locales'] or any(row['errors'] for row in report['locales']) else 0)
