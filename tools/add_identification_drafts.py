"""Reproducible assistant-authored identification copy; requires qualified review."""
from pathlib import Path
import html
import json

ROOT=Path(__file__).resolve().parents[1]
KEYS=['identify_photo','send_photo','identifying_photo','identification_suggestions','identification_score','identification_no_match','identification_limit','identification_error','identification_terms','identification_consent','identification_settings']
COPY={
'en':[
'Identify photo','Send photo','Looking for plant matches…','Suggestions can be wrong. Choose a species to fill the editable field, then check it before saving.','Match score: %1$s','No match found. Try a clearer plant photo or enter the species manually.','The daily identification allowance is used up. Try tomorrow or enter the species manually.','Identification is unavailable. Try again or enter the species manually.','Pl@ntNet · Terms and privacy',
'Send this selected photo to Pl@ntNet for suggestions? FloraTend forwards it through its server without saving it there. Pl@ntNet processes the image and retains request metadata. No upload occurs until you choose Send photo.'],
'fr':[
'Identifier la photo','Envoyer la photo','Recherche de plantes similaires…','Les suggestions peuvent être erronées. Choisissez une espèce pour remplir le champ modifiable, puis vérifiez avant de sauvegarder.','Score de correspondance : %1$s','Aucun résultat. Essayez une photo plus nette ou saisissez l’espèce.','Le quota quotidien est épuisé. Réessayez demain ou saisissez l’espèce.','Identification indisponible. Réessayez ou saisissez l’espèce.','Pl@ntNet · Conditions et confidentialité',
'Envoyer cette photo à Pl@ntNet pour obtenir des suggestions ? FloraTend la transmet via son serveur sans l’y enregistrer. Pl@ntNet traite l’image et conserve les métadonnées de la requête. Aucun envoi avant votre choix Envoyer la photo.'],
'es':[
'Identificar foto','Enviar foto','Buscando plantas similares…','Las sugerencias pueden ser incorrectas. Elige una especie para rellenar el campo editable y revísala antes de guardar.','Puntuación de coincidencia: %1$s','Sin coincidencias. Prueba una foto más clara o introduce la especie.','Se agotó el cupo diario. Vuelve mañana o introduce la especie.','Identificación no disponible. Inténtalo de nuevo o introduce la especie.','Pl@ntNet · Condiciones y privacidad',
'¿Enviar esta foto a Pl@ntNet para obtener sugerencias? FloraTend la transmite mediante su servidor sin guardarla allí. Pl@ntNet procesa la imagen y conserva metadatos de la solicitud. No se envía nada hasta que elijas Enviar foto.'],
'de':[
'Foto bestimmen','Foto senden','Ähnliche Pflanzen werden gesucht…','Vorschläge können falsch sein. Wähle eine Art für das bearbeitbare Feld und prüfe sie vor dem Speichern.','Übereinstimmungswert: %1$s','Kein Treffer. Versuche ein schärferes Foto oder gib die Art selbst ein.','Das Tageskontingent ist aufgebraucht. Versuche es morgen oder gib die Art selbst ein.','Bestimmung nicht verfügbar. Versuche es erneut oder gib die Art selbst ein.','Pl@ntNet · Bedingungen und Datenschutz',
'Dieses Foto für Vorschläge an Pl@ntNet senden? FloraTend leitet es über seinen Server weiter, ohne es dort zu speichern. Pl@ntNet verarbeitet das Bild und bewahrt Anfragemetadaten auf. Erst mit Foto senden wird es hochgeladen.'],
'it':[
'Identifica foto','Invia foto','Ricerca di piante simili…','I suggerimenti possono essere errati. Scegli una specie per compilare il campo modificabile e controllala prima di salvare.','Punteggio di corrispondenza: %1$s','Nessun risultato. Prova una foto più nitida o inserisci la specie.','Quota giornaliera esaurita. Riprova domani o inserisci la specie.','Identificazione non disponibile. Riprova o inserisci la specie.','Pl@ntNet · Condizioni e privacy',
'Inviare questa foto a Pl@ntNet per ricevere suggerimenti? FloraTend la inoltra tramite il proprio server senza salvarla lì. Pl@ntNet elabora l’immagine e conserva i metadati della richiesta. Nessun invio finché non scegli Invia foto.'],
'pt':[
'Identificar foto','Enviar foto','A procurar plantas semelhantes…','As sugestões podem estar erradas. Escolha uma espécie para preencher o campo editável e verifique antes de guardar.','Pontuação de correspondência: %1$s','Sem resultados. Experimente uma foto mais nítida ou introduza a espécie.','A quota diária esgotou-se. Tente amanhã ou introduza a espécie.','Identificação indisponível. Tente novamente ou introduza a espécie.','Pl@ntNet · Termos e privacidade',
'Enviar esta foto ao Pl@ntNet para obter sugestões? O FloraTend encaminha-a pelo seu servidor sem a guardar aí. O Pl@ntNet processa a imagem e conserva metadados do pedido. Nada é enviado até escolher Enviar foto.'],
'pt-BR':[
'Identificar foto','Enviar foto','Buscando plantas semelhantes…','As sugestões podem estar erradas. Escolha uma espécie para preencher o campo editável e confira antes de salvar.','Pontuação de correspondência: %1$s','Nenhum resultado. Tente uma foto mais nítida ou digite a espécie.','A cota diária acabou. Tente amanhã ou digite a espécie.','Identificação indisponível. Tente novamente ou digite a espécie.','Pl@ntNet · Termos e privacidade',
'Enviar esta foto ao Pl@ntNet para obter sugestões? O FloraTend a encaminha pelo servidor sem salvá-la lá. O Pl@ntNet processa a imagem e mantém metadados da solicitação. Nada é enviado até você escolher Enviar foto.'],
'nl':[
'Foto herkennen','Foto verzenden','Vergelijkbare planten zoeken…','Suggesties kunnen onjuist zijn. Kies een soort voor het bewerkbare veld en controleer deze voordat je opslaat.','Overeenkomstscore: %1$s','Geen resultaat. Probeer een scherpere foto of voer de soort zelf in.','Het dagquotum is op. Probeer morgen opnieuw of voer de soort zelf in.','Herkenning niet beschikbaar. Probeer opnieuw of voer de soort zelf in.','Pl@ntNet · Voorwaarden en privacy',
'Deze foto naar Pl@ntNet sturen voor suggesties? FloraTend stuurt deze via zijn server door zonder deze daar op te slaan. Pl@ntNet verwerkt de afbeelding en bewaart verzoekmetadata. Er wordt pas geüpload als je Foto verzenden kiest.'],
'pl':[
'Rozpoznaj zdjęcie','Wyślij zdjęcie','Wyszukiwanie podobnych roślin…','Sugestie mogą być błędne. Wybierz gatunek do edytowalnego pola i sprawdź go przed zapisaniem.','Wynik dopasowania: %1$s','Brak wyniku. Spróbuj wyraźniejszego zdjęcia lub wpisz gatunek.','Dzienny limit został wykorzystany. Spróbuj jutro lub wpisz gatunek.','Rozpoznawanie jest niedostępne. Spróbuj ponownie lub wpisz gatunek.','Pl@ntNet · Warunki i prywatność',
'Wysłać to zdjęcie do Pl@ntNet, aby uzyskać sugestie? FloraTend przekazuje je przez swój serwer bez zapisywania go tam. Pl@ntNet przetwarza obraz i zachowuje metadane żądania. Wysyłanie następuje dopiero po wybraniu Wyślij zdjęcie.'],
'ar':[
'التعرّف على الصورة','إرسال الصورة','جارٍ البحث عن نباتات مشابهة…','قد تكون الاقتراحات خاطئة. اختر نوعًا لملء الحقل القابل للتعديل وتحقّق منه قبل الحفظ.','درجة التطابق: %1$s','لم يُعثر على تطابق. جرّب صورة أوضح أو أدخل النوع يدويًا.','نفدت الحصة اليومية. حاول غدًا أو أدخل النوع يدويًا.','التعرّف غير متاح. حاول مجددًا أو أدخل النوع يدويًا.','Pl@ntNet · الشروط والخصوصية',
'هل تريد إرسال هذه الصورة إلى Pl@ntNet للحصول على اقتراحات؟ يمرّرها FloraTend عبر خادمه دون حفظها فيه. يعالج Pl@ntNet الصورة ويحتفظ بالبيانات الوصفية للطلب. لن تُرفع الصورة حتى تختار إرسال الصورة.'],
'hi':[
'फ़ोटो पहचानें','फ़ोटो भेजें','मिलते-जुलते पौधे खोजे जा रहे हैं…','सुझाव गलत हो सकते हैं। संपादन योग्य फ़ील्ड भरने के लिए प्रजाति चुनें और सहेजने से पहले जाँचें।','मिलान स्कोर: %1$s','कोई मिलान नहीं मिला। साफ़ फ़ोटो आज़माएँ या प्रजाति स्वयं लिखें।','दैनिक सीमा पूरी हो गई है। कल कोशिश करें या प्रजाति स्वयं लिखें।','पहचान उपलब्ध नहीं है। फिर कोशिश करें या प्रजाति स्वयं लिखें।','Pl@ntNet · शर्तें और गोपनीयता',
'सुझावों के लिए यह फ़ोटो Pl@ntNet को भेजें? FloraTend इसे अपने सर्वर से बिना वहाँ सहेजे आगे भेजता है। Pl@ntNet चित्र को संसाधित करता है और अनुरोध का मेटाडेटा रखता है। फ़ोटो भेजें चुनने तक कोई अपलोड नहीं होता।'],
'ja':[
'写真から調べる','写真を送信','似ている植物を検索中…','候補は正確とは限りません。選ぶと編集可能な種名欄に入力されます。保存前に確認してください。','一致スコア：%1$s','候補が見つかりません。鮮明な写真で再試行するか、種名を入力してください。','本日の利用上限に達しました。明日再試行するか、種名を入力してください。','識別できません。再試行するか、種名を入力してください。','Pl@ntNet · 利用規約とプライバシー',
'候補を調べるため、この写真をPl@ntNetに送信しますか？FloraTendのサーバーを経由しますが、そこで写真は保存されません。Pl@ntNetは画像を処理し、リクエストのメタデータを保持します。「写真を送信」を選ぶまでアップロードされません。'],
'ko':[
'사진으로 식별','사진 보내기','비슷한 식물을 찾는 중…','추천 결과는 틀릴 수 있습니다. 종을 선택하면 수정 가능한 입력란에 채워집니다. 저장하기 전에 확인하세요.','일치 점수: %1$s','일치 결과가 없습니다. 선명한 사진으로 다시 시도하거나 종을 직접 입력하세요.','일일 한도에 도달했습니다. 내일 다시 시도하거나 종을 직접 입력하세요.','식별할 수 없습니다. 다시 시도하거나 종을 직접 입력하세요.','Pl@ntNet · 약관 및 개인정보',
'추천을 받기 위해 이 사진을 Pl@ntNet에 보낼까요? FloraTend는 서버를 통해 사진을 전달하며 그곳에 저장하지 않습니다. Pl@ntNet는 이미지를 처리하고 요청 메타데이터를 보관합니다. 사진 보내기를 선택하기 전에는 업로드되지 않습니다.'],
'zh-Hans':[
'识别照片','发送照片','正在查找相似植物…','建议可能有误。选择物种后会填入可编辑字段，请在保存前核对。','匹配分数：%1$s','未找到匹配。请尝试更清晰的照片或手动输入物种。','已用完每日额度。请明天重试或手动输入物种。','暂时无法识别。请重试或手动输入物种。','Pl@ntNet · 条款和隐私',
'将这张照片发送至Pl@ntNet以获取建议？FloraTend通过自己的服务器转发照片，但不会在服务器上保存。Pl@ntNet会处理图像并保留请求元数据。只有选择“发送照片”后才会上传。'],
'zh-Hant':[
'辨識照片','傳送照片','正在尋找相似植物…','建議可能有誤。選擇物種後會填入可編輯欄位，請在儲存前核對。','比對分數：%1$s','找不到符合結果。請嘗試更清晰的照片或手動輸入物種。','每日額度已用完。請明天再試或手動輸入物種。','暫時無法辨識。請重試或手動輸入物種。','Pl@ntNet · 條款與私隱',
'將這張照片傳送至Pl@ntNet以取得建議？FloraTend透過自己的伺服器轉發照片，但不會在伺服器上儲存。Pl@ntNet會處理影像並保留請求中繼資料。只有選擇「傳送照片」後才會上傳。'],
'tr':[
'Fotoğrafı tanımla','Fotoğrafı gönder','Benzer bitkiler aranıyor…','Öneriler yanlış olabilir. Düzenlenebilir alanı doldurmak için bir tür seçin ve kaydetmeden önce kontrol edin.','Eşleşme puanı: %1$s','Eşleşme bulunamadı. Daha net bir fotoğraf deneyin veya türü elle girin.','Günlük kota doldu. Yarın deneyin veya türü elle girin.','Tanımlama kullanılamıyor. Yeniden deneyin veya türü elle girin.','Pl@ntNet · Koşullar ve gizlilik',
'Öneriler için bu fotoğraf Pl@ntNet’e gönderilsin mi? FloraTend fotoğrafı sunucusunda kaydetmeden iletir. Pl@ntNet görüntüyü işler ve istek meta verilerini saklar. Fotoğrafı gönder seçilene kadar yükleme yapılmaz.'],
'id':[
'Identifikasi foto','Kirim foto','Mencari tanaman serupa…','Saran bisa salah. Pilih spesies untuk mengisi kolom yang dapat diedit, lalu periksa sebelum menyimpan.','Skor kecocokan: %1$s','Tidak ada kecocokan. Coba foto lebih jelas atau masukkan spesies secara manual.','Kuota harian habis. Coba besok atau masukkan spesies secara manual.','Identifikasi tidak tersedia. Coba lagi atau masukkan spesies secara manual.','Pl@ntNet · Ketentuan dan privasi',
'Kirim foto ini ke Pl@ntNet untuk mendapatkan saran? FloraTend meneruskannya melalui server tanpa menyimpannya di sana. Pl@ntNet memproses gambar dan menyimpan metadata permintaan. Foto tidak diunggah sebelum Anda memilih Kirim foto.'],
'sv':[
'Identifiera foto','Skicka foto','Söker efter liknande växter…','Förslagen kan vara fel. Välj en art för att fylla i det redigerbara fältet och kontrollera innan du sparar.','Matchningspoäng: %1$s','Ingen träff. Prova ett tydligare foto eller ange arten själv.','Dagens kvot är slut. Försök i morgon eller ange arten själv.','Identifiering är inte tillgänglig. Försök igen eller ange arten själv.','Pl@ntNet · Villkor och integritet',
'Skicka detta foto till Pl@ntNet för förslag? FloraTend vidarebefordrar det via sin server utan att spara det där. Pl@ntNet behandlar bilden och behåller begärans metadata. Inget laddas upp förrän du väljer Skicka foto.'],
}

SETTINGS={
'en':'Optional photo identification uses Pl@ntNet only after you choose Send photo. You can enter the species manually.',
'fr':'L’identification facultative utilise Pl@ntNet uniquement après votre choix Envoyer la photo. Vous pouvez saisir l’espèce manuellement.',
'es':'La identificación opcional usa Pl@ntNet solo cuando eliges Enviar foto. Puedes introducir la especie manualmente.',
'de':'Die optionale Fotobestimmung nutzt Pl@ntNet erst nach deiner Wahl Foto senden. Du kannst die Art selbst eingeben.',
'it':'L’identificazione facoltativa usa Pl@ntNet solo dopo la scelta Invia foto. Puoi inserire la specie manualmente.',
'pt':'A identificação opcional usa o Pl@ntNet apenas após escolher Enviar foto. Pode introduzir a espécie manualmente.',
'pt-BR':'A identificação opcional usa o Pl@ntNet apenas depois de escolher Enviar foto. Você pode digitar a espécie manualmente.',
'nl':'Optionele fotoherkenning gebruikt Pl@ntNet pas nadat je Foto verzenden kiest. Je kunt de soort zelf invoeren.',
'pl':'Opcjonalne rozpoznawanie używa Pl@ntNet dopiero po wybraniu Wyślij zdjęcie. Gatunek można wpisać ręcznie.',
'ar':'يستخدم التعرّف الاختياري Pl@ntNet فقط بعد اختيار إرسال الصورة. يمكنك إدخال النوع يدويًا.',
'hi':'वैकल्पिक फ़ोटो पहचान में Pl@ntNet का उपयोग फ़ोटो भेजें चुनने के बाद ही होता है। आप प्रजाति स्वयं लिख सकते हैं।',
'ja':'任意の写真識別では「写真を送信」を選んだ場合のみPl@ntNetを使用します。種名は手動でも入力できます。',
'ko':'선택 사항인 사진 식별은 사진 보내기를 선택한 후에만 Pl@ntNet를 사용합니다. 종을 직접 입력할 수도 있습니다.',
'zh-Hans':'可选的照片识别仅在您选择“发送照片”后使用Pl@ntNet。您也可以手动输入物种。',
'zh-Hant':'選用的照片辨識僅在您選擇「傳送照片」後使用Pl@ntNet。您也可以手動輸入物種。',
'tr':'İsteğe bağlı fotoğraf tanımlama, yalnızca Fotoğrafı gönder seçildikten sonra Pl@ntNet’i kullanır. Türü elle de girebilirsiniz.',
'id':'Identifikasi foto opsional memakai Pl@ntNet hanya setelah Anda memilih Kirim foto. Anda juga dapat memasukkan spesies secara manual.',
'sv':'Valfri fotoidentifiering använder Pl@ntNet först när du väljer Skicka foto. Du kan också ange arten själv.',
}

def main():
    locales=json.loads((ROOT/'localization/locales.json').read_text())
    for locale in locales:
        tag=locale['locale']
        language=('zh-Hans' if tag=='zh-CN' else 'zh-Hant' if tag=='zh-TW' else 'pt-BR' if tag=='pt-BR' else tag.split('-')[0])
        values=COPY[language]+[SETTINGS[language]]
        assert len(values)==len(KEYS)
        def escape(value):return html.escape('"'+value.replace('\\','\\\\').replace('"','\\"').replace("'","\\'")+'"',quote=False)
        xml='<resources>\n'+'\n'.join(f'  <string name="{key}">{escape(value)}</string>' for key,value in zip(KEYS,values))+'\n</resources>\n'
        (ROOT/'app/src/main/res'/locale['android_folder']/'identification.xml').write_text(xml,encoding='utf-8')

if __name__=='__main__':main()
