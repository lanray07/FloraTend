"""Compose eight localized draft store PNGs per actual console locale from real captures."""
from pathlib import Path
import argparse,json,hashlib,sys
from PIL import Image,ImageDraw,ImageFont,ImageOps
from screenshots import validate_png
ROOT=Path(__file__).resolve().parents[1]
SCENES=['01-hero','02-watering','03-library','04-calendar','05-growth','06-voice','07-offline','08-insights']
MAPPING={'en-IN':'en-GB','es-MX':'es-419'}
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def font(locale,size,bold=False):
    win=Path('C:/Windows/Fonts')
    candidates=[win/'msgothic.ttc'] if locale=='ja-JP' else [win/('georgiab.ttf' if bold else 'georgia.ttf'),win/'segoeui.ttf']
    candidates += [Path('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf')]
    for p in candidates:
        if p.exists():return ImageFont.truetype(str(p),size)
    raise RuntimeError('Install a suitable Unicode font before rendering')
def wrap(draw,text,face,width,locale):
    units=list(text) if locale=='ja-JP' else text.split()
    separator='' if locale=='ja-JP' else ' '
    lines=[];line=''
    for unit in units:
        trial=line+separator+unit if line else unit
        if draw.textlength(trial,font=face)>width and line:lines.append(line);line=unit
        else:line=trial
    if line:lines.append(line)
    return lines

def compose(captures,out):
    source=json.loads((captures/'capture-manifest.json').read_text(encoding='utf-8-sig'))
    assert len(source['locales'])==19,'All app locales must be captured successfully first'
    network=(captures/'network-state.txt').read_text(encoding='utf-8-sig')
    assert 'none' in network,'Offline claims require the recorded no-network condition'
    captured={r['locale']:{s['scene']:s for s in r['scenes']} for r in source['locales']}
    headlines=json.loads((ROOT/'assets/screenshots/headlines.json').read_text(encoding='utf-8'))['locales']
    rows=[]
    for locale,titles in headlines.items():
        assert len(titles)==8
        native=MAPPING.get(locale,locale)
        for scene,title in zip(SCENES,titles):
            record=captured[native][scene];raw=captures/record['file']
            rawcheck=validate_png(raw);assert rawcheck['store_dimensions_valid']
            canvas=Image.new('RGB',(2560,1600),'#FAF8F2');draw=ImageDraw.Draw(canvas)
            brand=font(locale,40);draw.text((104,54),'FloraTend',font=brand,fill='#315C45')
            draw.rounded_rectangle((2316,52,2456,114),radius=31,fill='#E4EBDF')
            draw.text((2352,61),scene[:2],font=font(locale,32),fill='#315C45')
            face=font(locale,84,True);lines=wrap(draw,title,face,2240,locale)
            while len(lines)>2:
                face=font(locale,face.size-4,True);lines=wrap(draw,title,face,2240,locale)
                assert face.size>=52,'Headline cannot fit without review'
            for n,line in enumerate(lines):draw.text((104,140+n*100),line,font=face,fill='#253E30')
            draw.rectangle((106,330,226,338),fill='#C8795B')
            original=Image.open(raw).convert('RGB')
            screen=ImageOps.contain(original,(2360,1150),Image.Resampling.LANCZOS)
            x=(2560-screen.width)//2;y=380
            draw.rounded_rectangle((x-12,y-12,x+screen.width+12,y+screen.height+12),radius=20,fill='#315C45')
            canvas.paste(screen,(x,y))
            destination=out/locale/(scene+'.png');destination.parent.mkdir(parents=True,exist_ok=True)
            canvas.save(destination,optimize=True)
            exported=validate_png(destination);assert exported['store_dimensions_valid']
            rows.append({'listing_locale':locale,'native_app_locale':native,'scene':scene,'headline':title,'headline_lines':lines,'headline_font_px':face.size,'capture':str(raw.relative_to(ROOT)).replace('\\','/'),'capture_sha256':sha(raw),'export':str(destination.relative_to(ROOT)).replace('\\','/'),'export_sha256':sha(destination),'width':2560,'height':1600,'status':'DRAFT_PENDING_HUMAN_REVIEW','actual_rendered_ui':True,'offline_capture':True,'demonstration_records':True})
    report={'version_code':source['versionCode'],'version_name':source['versionName'],'raw_app_locales':19,'store_locales':len(headlines),'images':len(rows),'human_language_approval':False,'region_mapping':MAPPING,'screenshots':rows}
    report['device']={'model':'KFTRWI','name':'Fire HD 10 (2021)','api':28,'capture_width':1920,'capture_height':1200}
    (out/'localized-manifest.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    # Contact sheets retain all eight exported scenes for visual review of each locale.
    for locale in headlines:
        sheet=Image.new('RGB',(1280,400),'#FFFFFF')
        for i,scene in enumerate(SCENES):
            preview=Image.open(out/locale/(scene+'.png'));preview.thumbnail((320,200))
            sheet.paste(preview,((i%4)*320,(i//4)*200))
        sheet.save(out/locale/'contact-sheet.jpg',quality=90)
    print(json.dumps({k:report[k] for k in ['raw_app_locales','store_locales','images','human_language_approval']},indent=2))
if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--captures',type=Path,required=True);parser.add_argument('--output',type=Path,default=ROOT/'assets/screenshots/localized');args=parser.parse_args()
    compose(args.captures.resolve(),args.output.resolve())
