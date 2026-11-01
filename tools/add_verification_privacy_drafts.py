"""Reproduce purchase-data disclosures. Draft copy requires qualified human review."""
import html,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
COPY={
'en':'Premium verification sends your Amazon app user ID and purchase receipt ID over HTTPS to the FloraTend server hosted by Cloudflare, which checks them with Amazon. Plant records, notes and photos are not included.',
'fr':'La vérification de Premium envoie votre identifiant utilisateur Amazon propre à cette application et l’identifiant du reçu d’achat par HTTPS au serveur FloraTend hébergé par Cloudflare, qui les vérifie auprès d’Amazon. Les fiches de plantes, notes et photos ne sont pas incluses.',
'es':'La verificación de Premium envía tu identificador de usuario de Amazon para esta aplicación y el identificador del recibo de compra mediante HTTPS al servidor de FloraTend alojado en Cloudflare, que los verifica con Amazon. No se incluyen registros de plantas, notas ni fotos.',
'de':'Zur Premium-Prüfung werden deine Amazon-Nutzerkennung für diese App und die Kennung des Kaufbelegs per HTTPS an den bei Cloudflare gehosteten FloraTend-Server gesendet und bei Amazon geprüft. Pflanzenaufzeichnungen, Notizen und Fotos werden nicht mitgesendet.',
'it':'La verifica di Premium invia il tuo identificativo utente Amazon per questa app e quello della ricevuta di acquisto tramite HTTPS al server FloraTend ospitato da Cloudflare, che li verifica con Amazon. Schede delle piante, note e foto non sono incluse.',
'pt':'A verificação do Premium envia o seu identificador de utilizador Amazon para esta aplicação e o identificador do recibo de compra por HTTPS ao servidor FloraTend alojado na Cloudflare, que os verifica junto da Amazon. Os registos de plantas, notas e fotografias não são incluídos.',
'pt-BR':'A verificação do Premium envia seu identificador de usuário Amazon para este aplicativo e o identificador do recibo de compra por HTTPS ao servidor FloraTend hospedado na Cloudflare, que os verifica com a Amazon. Registros de plantas, notas e fotos não são incluídos.',
'nl':'Voor Premium-verificatie worden je Amazon-gebruikers-ID voor deze app en de ID van je aankoopbewijs via HTTPS naar de FloraTend-server bij Cloudflare gestuurd en bij Amazon gecontroleerd. Plantgegevens, notities en foto’s worden niet meegestuurd.',
'pl':'Weryfikacja Premium wysyła Twój identyfikator użytkownika Amazon dla tej aplikacji i identyfikator potwierdzenia zakupu przez HTTPS do serwera FloraTend w Cloudflare, który sprawdza je w Amazon. Dane roślin, notatki i zdjęcia nie są wysyłane.',
'sv':'Premium-verifieringen skickar ditt Amazon-användar-ID för den här appen och inköpskvittots ID via HTTPS till FloraTends server hos Cloudflare, som kontrollerar dem med Amazon. Växtuppgifter, anteckningar och foton skickas inte med.',
'ar':'يرسل التحقق من Premium معرّف مستخدم Amazon الخاص بهذا التطبيق ومعرّف إيصال الشراء عبر HTTPS إلى خادم FloraTend المستضاف لدى Cloudflare للتحقق منهما لدى Amazon. لا تُرسل سجلات النباتات أو الملاحظات أو الصور.',
'hi':'Premium के सत्यापन के लिए इस ऐप का आपका Amazon उपयोगकर्ता आईडी और खरीद रसीद आईडी HTTPS के ज़रिए Cloudflare पर होस्ट किए गए FloraTend सर्वर को भेजे जाते हैं, जो Amazon से उनकी जाँच करता है। पौधों के रिकॉर्ड, नोट्स और फ़ोटो इसमें शामिल नहीं होते।',
'ja':'Premiumの確認では、このアプリ用のAmazonユーザーIDと購入レシートIDをHTTPSでCloudflare上のFloraTendサーバーに送り、Amazonに照会します。植物の記録、メモ、写真は送信しません。',
'ko':'Premium 확인을 위해 이 앱의 Amazon 사용자 ID와 구매 영수증 ID가 HTTPS를 통해 Cloudflare에서 호스팅하는 FloraTend 서버로 전송되며, 서버는 Amazon에 이를 확인합니다. 식물 기록, 메모 및 사진은 포함되지 않습니다.',
'zh-Hans':'Premium验证会通过HTTPS将此应用的Amazon用户ID和购买收据ID发送至由Cloudflare托管的FloraTend服务器，再由服务器向Amazon核验。不会发送植物记录、笔记或照片。',
'zh-Hant':'Premium驗證會透過HTTPS將此應用程式的Amazon使用者ID和購買收據ID傳送至由Cloudflare託管的FloraTend伺服器，再由伺服器向Amazon核驗。不會傳送植物紀錄、筆記或照片。',
'tr':'Premium doğrulaması, bu uygulamaya ait Amazon kullanıcı kimliğinizi ve satın alma makbuzu kimliğinizi HTTPS üzerinden Cloudflare üzerinde barındırılan FloraTend sunucusuna gönderir. Sunucu bunları Amazon ile doğrular. Bitki kayıtları, notlar ve fotoğraflar gönderilmez.',
'id':'Verifikasi Premium mengirim ID pengguna Amazon untuk aplikasi ini dan ID bukti pembelian Anda melalui HTTPS ke server FloraTend yang dihosting oleh Cloudflare, lalu memverifikasinya dengan Amazon. Catatan tanaman, catatan pribadi, dan foto tidak disertakan.'
}

def main():
    for locale in json.loads((ROOT/'localization/locales.json').read_text(encoding='utf-8')):
        tag=locale['locale']
        language='zh-Hans' if tag=='zh-CN' else 'zh-Hant' if tag=='zh-TW' else 'pt-BR' if tag=='pt-BR' else tag.split('-')[0]
        text=COPY[language].replace('\\','\\\\').replace('"','\\"').replace("'","\\'")
        xml='<resources>\n  <string name="purchase_verification_privacy">'+html.escape('"'+text+'"',quote=False)+'</string>\n</resources>\n'
        (ROOT/'app/src/main/res'/locale['android_folder']/'privacy.xml').write_text(xml,encoding='utf-8')

if __name__=='__main__':main()
