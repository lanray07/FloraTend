"""Original FloraTend botanical vector drawings, authored as editable paths.

Exports matching SVG masters and Android vector drawables; no external artwork,
fonts, generation service, competitor assets or raster edits are involved.
"""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
FOREST, SAGE, CLAY = '#315C45', '#8FAE91', '#C8795B'
POT = (CLAY, 'M27,65 L69,65 L63,88 L33,88 Z M24,60 L72,60 L72,67 L24,67 Z')
STEM = (FOREST, 'M46,62 L50,62 L50,28 L46,28 Z')
ART = {
    'indoor': [POT, STEM, (FOREST,'M47,48 C17,50 14,18 38,14 C57,15 57,38 47,48 Z'), (SAGE,'M49,41 C48,17 77,12 82,25 C84,44 65,49 49,41 Z')],
    'outdoor': [POT, STEM, (SAGE,'M18,33 C7,10 37,5 48,21 C57,1 91,14 80,36 C96,59 61,68 48,51 C31,67 8,54 18,33 Z')],
    'succulent': [POT, (FOREST,'M48,64 C17,66 6,40 14,31 C30,31 43,43 48,64 Z M48,64 C79,66 90,40 82,31 C66,31 53,43 48,64 Z'), (SAGE,'M48,64 C23,49 27,18 48,9 C69,18 73,49 48,64 Z')],
    'cactus': [POT, (FOREST,'M39,62 L39,20 C39,6 57,6 57,20 L57,62 Z M41,46 L26,46 C16,46 13,39 13,32 L13,25 C13,17 23,17 23,25 L23,33 C23,36 27,37 41,36 Z M55,53 L71,53 C80,53 83,47 83,40 L83,31 C83,23 73,23 73,31 L73,40 C73,43 68,43 55,43 Z'), (SAGE,'M45,21 L48,21 L48,59 L45,59 Z')],
    'herb': [POT, STEM, (SAGE,'M48,54 C24,53 16,40 19,30 C35,27 47,39 48,54 Z M48,43 C49,23 66,16 79,21 C78,36 65,46 48,43 Z'), (FOREST,'M48,31 C29,27 29,12 36,7 C50,10 56,20 48,31 Z')],
    'flowering': [POT, STEM, (SAGE,'M47,59 C20,58 16,45 18,36 C34,34 47,44 47,59 Z M49,53 C50,37 69,31 79,36 C78,50 62,59 49,53 Z'), (CLAY,'M48,27 C28,17 36,2 48,13 C60,2 68,17 55,24 C77,26 66,45 53,34 C45,55 27,42 39,30 C16,32 20,12 40,19 Z'), (FOREST,'M42,24 A6,6 0 1,0 54,24 A6,6 0 1,0 42,24 Z')],
    'tree': [POT, (CLAY,'M44,64 L52,64 L51,30 L45,30 Z'), (FOREST,'M48,7 L23,35 L33,35 L15,55 L81,55 L63,35 L73,35 Z'), (SAGE,'M48,15 L48,50 L67,50 L54,34 L61,34 Z')],
    'other': [POT, STEM, (FOREST,'M47,43 C16,48 14,18 23,13 C40,16 51,29 47,43 Z'), (SAGE,'M50,34 C49,10 73,7 82,13 C83,30 66,41 50,34 Z')],
}
ART['splash'] = ART['other']
ART['premium'] = ART['indoor'] + [(SAGE,'M9,67 L11,67 L11,86 L30,86 L30,88 L9,88 Z M66,8 L87,8 L87,29 L85,29 L85,10 L66,10 Z')]


def main():
    output = ROOT/'assets/botanical-vectors'
    output.mkdir(parents=True,exist_ok=True)
    for name, paths in ART.items():
        svg = ET.Element('svg', xmlns='http://www.w3.org/2000/svg', width='96', height='96', viewBox='0 0 96 96')
        ET.SubElement(svg,'title').text=f'FloraTend original {name} botanical illustration'
        vector = ET.Element('vector', {'xmlns:android':'http://schemas.android.com/apk/res/android','android:width':'96dp','android:height':'96dp','android:viewportWidth':'96','android:viewportHeight':'96'})
        for color,path in paths:
            ET.SubElement(svg,'path', fill=color,d=path)
            ET.SubElement(vector,'path',{'android:fillColor':color,'android:pathData':path})
        ET.indent(svg);ET.indent(vector)
        ET.ElementTree(svg).write(output/f'{name}.svg',encoding='utf-8',xml_declaration=True)
        ET.ElementTree(vector).write(ROOT/f'app/src/main/res/drawable/art_{name}.xml',encoding='utf-8',xml_declaration=True)
    print(f'Exported {len(ART)} original SVG masters and matching native vector drawables')


if __name__ == '__main__':
    main()
