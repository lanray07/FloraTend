import copy,json,tempfile,unittest
from pathlib import Path
from audit_metadata import audit,ROOT

class MetadataPolicyTests(unittest.TestCase):
 def test_current_fifteen_locales_pass_technical_rules(self):
  r=audit();self.assertEqual(15,len(r['locales']));self.assertTrue(all(not x['errors'] for x in r['locales']));self.assertFalse(r['human_approved'])
 def test_prohibited_content_is_rejected(self):
  baseline=json.loads((ROOT/'submission/listings/en-GB.json').read_text(encoding='utf-8'))
  cases=[('long_description','<b>Plant care</b>','HTML'),('display_title','FloraTend \U0001f331','Emoji'),('display_title','FLORATEND PLANT CARE','ALL CAPS'),('short_description','Broken \ufffd text','Unicode'),('long_description','Price GBP 4.99','price/ASIN'),('long_description','Product B012345678','price/ASIN')]
  with tempfile.TemporaryDirectory() as directory:
   p=Path(directory)/'en-GB.json'
   for field,value,message in cases:
    with self.subTest(message=message,value=value):
     d=copy.deepcopy(baseline);d[field]=value;p.write_text(json.dumps(d),encoding='utf-8');errors=audit(Path(directory))['locales'][0]['errors'];self.assertTrue(any(message in e for e in errors),errors)

if __name__=='__main__':unittest.main()
