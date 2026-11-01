"""Compose eight localized draft store PNGs per actual console locale from real captures."""
from pathlib import Path
from io import BytesIO
import argparse,json,hashlib,sys,time
import xml.etree.ElementTree as ET
from PIL import Image,ImageDraw,ImageFont,ImageOps
from screenshots import validate_png
ROOT=Path(__file__).resolve().parents[1]
SCENES=['01-hero','02-watering','03-library','04-calendar','05-growth','06-voice','07-offline','08-insights']
MAPPING={'en-IN':'en-GB','es-MX':'es-419'}
CAPTION_KEYS=['tagline','check_first','search','care_reminders','journal_intro','command','local_records','insights_intro']
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()

def caption_text(value):
    # Android's outer XML string quotes preserve whitespace; they are not UI ink.
    if len(value)>=2 and value.startswith('"') and value.endswith('"'):
        value=value[1:-1]
    return value.replace("\\'", "'").replace('\\"','"').replace('\\n','\n')

def write_contact_sheets(out,locales):
    results=[]
    for locale in locales:
        sheet=Image.new('RGB',(1280,400),'#FFFFFF')
        for i,scene in enumerate(SCENES):
            with Image.open(out/locale/(scene+'.png')) as source:
                preview=source.copy();preview.thumbnail((320,200))
                sheet.paste(preview,((i%4)*320,(i//4)*200));preview.close()
        # Encode before replacing the last complete sheet. Direct truncation of
        # an existing Windows destination reproduced Errno22 for nl-NL.
        buffer=BytesIO();sheet.save(buffer,format='JPEG',quality=90);sheet.close()
        destination=out/locale/'contact-sheet.jpg'
        temporary=destination.with_name('contact-sheet.rendering.jpg')
        temporary.write_bytes(buffer.getvalue())
        for attempt in range(6):
            try:
                temporary.replace(destination)
                break
            except OSError as error:
                if attempt==5 or (getattr(error,'winerror',None)!=5 and error.errno!=22):raise
                time.sleep(.2*(2**attempt))
        results.append({'locale':locale,'replacementRetries':attempt})
    return results
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
    expected=len(json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))) if source['versionCode']>=10 else 19
    assert len(source['locales'])==expected,'All app locales must be captured successfully first'
    network=(captures/'network-state.txt').read_text(encoding='utf-8-sig')
    assert 'none' in network,'Offline claims require the recorded no-network condition'
    captured={r['locale']:{s['scene']:s for s in r['scenes']} for r in source['locales']}
    headlines=json.loads((ROOT/'assets/screenshots/headlines.json').read_text(encoding='utf-8'))['locales']
    folders={row['locale']:row['android_folder'] for row in json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8'))}
    rows=[]
    for locale,titles in headlines.items():
        assert len(titles)==8
        native=MAPPING.get(locale,locale)
        caption_source=ROOT/'app/src/main/res'/folders[native]/'strings.xml'
        strings={node.attrib['name']:caption_text(''.join(node.itertext())) for node in ET.parse(caption_source).getroot() if node.tag=='string'}
        for scene,title,caption_key in zip(SCENES,titles,CAPTION_KEYS):
            caption=strings[caption_key]
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
            caption_face=font(locale,36);caption_lines=wrap(draw,caption,caption_face,2240,locale)
            assert len(caption_lines)<=2,'Supporting caption requires editorial review to fit'
            caption_bounds=[]
            for n,line in enumerate(caption_lines):
                bounds=draw.textbbox((104,350+n*46),line,font=caption_face)
                # Some glyphs overhang the104px pen origin by1px; retain8px ink tolerance.
                assert bounds[0]>=96 and bounds[2]<=2456 and bounds[3]<450,'Supporting caption exceeds its reserved space'
                caption_bounds.append(list(bounds))
                draw.text((104,350+n*46),line,font=caption_face,fill='#46564A')
            draw.rectangle((106,450,226,458),fill='#C8795B')
            original=Image.open(raw).convert('RGB')
            screen=ImageOps.contain(original,(2360,1050),Image.Resampling.LANCZOS)
            x=(2560-screen.width)//2;y=480
            draw.rounded_rectangle((x-12,y-12,x+screen.width+12,y+screen.height+12),radius=20,fill='#315C45')
            canvas.paste(screen,(x,y))
            destination=out/locale/(scene+'.png');destination.parent.mkdir(parents=True,exist_ok=True)
            canvas.save(destination,optimize=True)
            exported=validate_png(destination);assert exported['store_dimensions_valid']
            rows.append({'listing_locale':locale,'native_app_locale':native,'scene':scene,'headline':title,'headline_lines':lines,'headline_font_px':face.size,'capture':str(raw.relative_to(ROOT)).replace('\\','/'),'capture_sha256':sha(raw),'export':str(destination.relative_to(ROOT)).replace('\\','/'),'export_sha256':sha(destination),'width':2560,'height':1600,'status':'DRAFT_PENDING_HUMAN_REVIEW','actual_rendered_ui':True,'offline_capture':True,'demonstration_records':True})
            rows[-1].update({'supporting_caption':caption,'caption_lines':caption_lines,'caption_line_bounds':caption_bounds,'caption_font_px':36,'caption_resource_key':caption_key,'caption_resource_source':caption_source.relative_to(ROOT).as_posix(),'caption_resource_sha256':sha(caption_source),'native_frame_bounds':[x,y,x+screen.width,y+screen.height],'native_frame_max_size':[2360,1050]})
    report={'version_code':source['versionCode'],'version_name':source['versionName'],'raw_app_locales':len(source['locales']),'store_locales':len(headlines),'images':len(rows),'human_language_approval':False,'region_mapping':MAPPING,'screenshots':rows}
    report['device']={'model':'KFTRWI','name':'Fire HD 10 (2021)','api':28,'capture_width':1920,'capture_height':1200}
    (out/'localized-manifest.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    # Contact sheets retain all eight exported scenes for visual review of each locale.
    write_contact_sheets(out,headlines)
    print(json.dumps({k:report[k] for k in ['raw_app_locales','store_locales','images','human_language_approval']},indent=2))
if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--captures',type=Path,required=True);parser.add_argument('--output',type=Path,default=ROOT/'assets/screenshots/localized');args=parser.parse_args()
    compose(args.captures.resolve(),args.output.resolve())
