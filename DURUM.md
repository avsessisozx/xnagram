# xnagram DURUM (durum dalı, main'de yok)

Son güncelleme: 2026-10-27 · Aşama 0 tamam; kod `gelistirme` dalında (henüz main'e alınmadı).

## Proje
- Fork: avsessisozx/xnagram ← risin42/NagramX (upstream 23.08.2026'da arşivlendi, son sürüm 12.9.2.1258).
- Varsayılan dal: `dev` (4.799 commit). Henüz sürüm yok.
- Hedef: ToS uyumlu (`_base` tarzı, riskli özellik YOK) kendi sürümümüz, kendi güncelleme kaynağımız = bu repo.
- Telefon (Mi 10T Pro, ADB serial 4d738514): `nu.gpu.nagramx` 12.9.2-fd39067 (kod 1260) kurulu. Resmi Telegram da kurulu.

## Bulgular (doğrulandı: kodda okundu)
- Çeviri butonu: `ChatActivity.createTranslateButton()` (~satır 10507), sohbet üst panelinde banner. `TranslateController.isFeatureAvailable()` değilse Premium penceresi açıyor; `dialog_show_translate_count` sayaçları butonu gizliyor. Menüde ayrıca `translateItem` (id 62) var, yalnızca çeviri gizliyken görünüyor.
- Güncelleme kontrolü: `helpers/remote/UpdateHelper.java`, GitHub'dan DEĞİL, Telegram kanalından (`BaseRemoteHelper.CHANNEL_METADATA_ID = 2477822904`) okuyor. Bizim repoya çevirmek = GitHub Releases API'sine yeniden yazmak. `EmojiHelper` de aynı kanalı kullanıyor.
- Paket adı `gradle.properties` → `APP_PACKAGE=nu.gpu.nagram`. `AndroidUtil.java:356` bu paket adına bağlı.
- `NekoAboutActivity.java:55` risin42/NagramX'e link veriyor.
- CI: `.github/workflows/release.yml` APK'yı Telegram botuna yolluyor (`Tools/scripts/upload.py`), GitHub Release oluşturmuyor. Secrets: LOCAL_PROPERTIES, APP_ID, APP_HASH, HELPER_BOT_*.
- Repoda `TMessagesProj/release.keystore` ve `google-services.json` upstream'den gelmiş. Bizim kendi anahtarımızla değiştirilmeli.
- Submodule'ler (ffmpeg, libvpx, dav1d, opus vb.) shallow clone'da boş; CI `submodules: recursive` ile çekiyor.

## Derleme ortamı (doğrulandı)
- Bu oturumun sandbox'ında Android SDK/Gradle/Google Maven indirilemiyor (dl.google.com, services.gradle.org, maven.google.com engelli; 2 CPU, 7 GB RAM). Yerelde derleme burada MÜMKÜN DEĞİL; derleme GitHub Actions'ta yapılacak.
- `gh api .../actions/runs` okunabiliyor (CI sonuçları izlenebilir). Actions secrets bu oturumdan okunamıyor/yazılamıyor (proxy 403): secrets'ı kullanıcı GitHub'dan eklemeli.
- build.gradle varsayılan API ID'si 6 (resmi Telegram Android'in herkese açık ID'si). Kendi ID/hash'imiz gelene kadar bununla test edilebilir.

## Doğrulanmadı
- Derleme henüz denenmedi (NDK 27.2, JDK 21, nasm gerekli).
- Telegram taban sürümünün (12.9.2) üstünde güncel upstream Telegram değişiklikleri incelenmedi.

## Yapılacaklar
1. Paket adı/uygulama adı, kendi keystore, kendi FCM dosyası (ya da FCM'siz), kendi Telegram API ID/hash (kullanıcıdan gerekli).
2. Workflow: Telegram'a yükleme yerine GitHub Release'e APK yükle.
3. UpdateHelper'ı GitHub Releases'e çevir, About linkini düzelt, helper-bot/kanal bağımlılıklarını kapat.
4. Çeviri butonu: konum seçenekli (sohbet üst bilgi çubuğu / yan menü / ayrı buton), sabitleme, Premium kapısı yok, tek dokunuşla aç/kapa.
5. Telegram taban güvenlik yamaları (2. sürüm).
6. Telefonda ADB ile kurup dene, sonra sürüm yayınla.

## Bilinen hatalar
- Yok (kod değişmedi).

## Yapılanlar (gelistirme dalı, doğrulanmadı: CI/telefon testi bekliyor)
- Paket adı com.avsessisozx.xnagram, uygulama adı XNagram, google-services.json'a bu paket için girdi eklendi (FCM push bizim için çalışmaz; kendi Firebase projesi gerekir).
- GithubUpdateHelper: son GitHub sürümünü okur, etiket `v<versionCode>` (örn. v1262), basit dialog + APK'yı tarayıcıyla indirir. Eski Telegram-kanalı UpdateHelper'ı artık LaunchActivity'den çağrılmıyor.
- CI: release.yml → arm64 APK, GitHub Release (sadece main + kalıcı imza varsa). Gizli: XN_KEYSTORE_B64, XN_KEYSTORE_PASS, XN_KEY_ALIAS, XN_KEY_PASS yoksa geçici imzayla yalnızca artifact üretir.
- Çeviri butonu: ayar `TranslateButtonMode` (0 varsayılan banner, 1 üst çubuk ikonu [varsayılan], 2 ⋮ menüsü, 3 sabit banner). Ayar yeri: Çeviri ayarları.
- Bilinen: verCode build.gradle'da elle (şu an 1261), her sürümde artırılmalı.

## Doğrulama durumu (2026-10-27 akşam)
Doğrulandı:
- CI derlemesi geçiyor (arm64, geçici imza). Test APK: release `test-3` (prerelease).
- APK telefona kuruldu (com.avsessisozx.xnagram 12.10.0-6e9e74e, kod 1261), NagramX'in yanında; açılışta çökme yok (logcat). Crashlytics/analitik kapalı (manifest + runtime).
Doğrulanmadı (hesap girişi gerekiyor, kullanıcı giriş yapmadan test edilemez):
- Çeviri butonu (4 mod) sohbette gerçekten çalışıyor mu, simge/alpha durumu.
- Güncelleme kontrolü (GitHub sürümü) ve dialog; henüz yayınlanmış `v<kod>` sürümü yok.
- Push bildirimi: FCM upstream projesine ait, bizim paket için çalışmaz.
Not: Test derlemeleri her seferinde yeni geçici anahtarla imzalanır; birbirinin üstüne kurulmaz. Kalıcı anahtar kullanıcıya verildi (XN_* secrets); eklenince main'e push = GitHub Release.

## Surum v1261 (2026-10-27)
Yayinlandi: GitHub Release v1261 (main, kalici imza, arm64 APK). CI gecti.
Dogrulandi: kalici anahtarla derleme; Release olustu; silinen mesaj kaydetme ayarlari gizlendi + kapaliya zorlandi (kodda, cihazda denenmedi).
Dogrulanmadi: ceviri butonu (giris gerekir), GitHub guncelleme dialogu (daha yeni surum yok), push bildirimi (calismaz).
Yapilacak: test-N on-surumlerini sil, 2. surum (Telegram taban yamalari).


## Telegram 13.2.1 birleştirme araştırması (2026-11-09, doğrulandı: git ile ölçüldü)
- Bizim taban: resmi Telegram 12.10.0 (7031), TL katmanı 229. Resmi master: 13.2.1 (7291, 07.11.2026), katman 233. (Resmi `dev` dalı 2021'den kalma, kullanılmaz; `master` kullanılır.)
- Yöntem: gelistirme ağacını resmi 12.10.0 commit'inin (4e1a61eca) üstüne sentetik commit yapıp `git merge upstream/master` denendi. Sonuç: ~127 içerik çakışması + ~59 sil/değiştir çakışması. En ağırlar: ChatActivity (26 parça), ChatMessageCell (23), SettingsActivity (12), ProfileActivity/DialogsActivity/ChatActivityEnterView (9).
- Resmi taraf yapıyı değiştirmiş: yeni alt modüller (boringssl, td, absl, wamr, openh264, libyuv, TMessagesProj_Modules/media), TLRPC artık buildSrc/tlrpc ile üretiliyor, native dizin düzeni farklı.
- Yerelde derleme yok (sandbox); doğrulama yalnız GitHub Actions'ta. Hiçbir şey push edilmedi, ana dallar değişmedi.
- Doğrulanmadı: kanal sorununun gerçek nedeni (katman farkı olması en olası, kanıtlanmadı).


## 2026-11-09 · Telegram 13.2.1 (7291, katman 233) birleştirildi — sürüm 13.2.1 / kod 1262
- Dal: `gelistirme` (commit ee72633). main'e ALINMADI; telefonda denenmedi.
- Doğrulandı: GitHub Actions derlemesi başarılı (test-16 ön sürümü, geçici imzalı arm64 APK).
- Doğrulanmadı: telefonda çalışma, kanalların açılması, giriş, medya oynatma (media3), arama.
- Derleme yığını resmî Telegram'a çekildi: AGP 8.13.2, Gradle 8.13, Kotlin 2.2.20, media3 alt modülü, yeni native alt modüller. compileSdk 36; AAR metadata kontrolü kapalı (unifiedpush/okhttp5/translator compileSdk 37 istiyor).
- Kaybolan/değişen Nagram özellikleri: ChatActivity yazı biçimi menüsünde Nagram'ın kod/bahsetme/çeviri girişleri; profil adı uzun basıp kopyalama; canlı bulanık (blur3) arka planlar (Bookmarks, AyuViewDeleted, NekoDelegateFragment sayfaları düz/cam kaynağa döndü); özel ses bitrate (voip) yaması; ProxyList'te aramalar için proxy sıfırlaması; resmî `webrtc_voice_engine.cc` kullanılıyor.
- Sonraki: telefonda dene → sorun varsa log; sonra main'e al ve kalıcı anahtarla yayınla.


## 2026-11-09 akşam · LOC_ERR düzeltmesi + CI doğrulaması (gelistirme, commit 45e351ca; sürüm 13.2.1 / kod 1262)
- Neden: Telegram 13.2.1'in yeni yazı sistemi yalnızca values/strings.xml ve values-*/strings.xml okuyor; Nagram'ın strings_na/nax/neko.xml dosyaları sözlüğe girmiyordu (Türkçe çeviriler dahil). Dil listesini üreten TelegramBuildPlugin de aynı filtreyi kullanıyordu.
- Düzeltme: TelegramBuildAppPlugin.kt ve TelegramBuildPlugin.kt filtreleri strings*.xml; TelegramStringsTask dosyaları sıralıyor; NagramX/SettingsNax/AccountSettingsNax (manifest/xml'den kullanılıyor) kaynak küçültmeden korunuyor.
- Doğrulandı (CI, gerçek APK): .github/scripts/verify_localization.py: localization_en.bin 12287/12287 anahtar, string_resource_ids.bin içinde hepsi, tr-TR 712 çeviri tamam, aapt2 etiketi XNagram. Emülatör (Android 12): uygulama çalışıyor, çökme yok, ekranda LOC_ERR yok (yalnız karşılama ekranı görülüyor).
- Doğrulanmadı: giriş yapılmış uygulamada Nagram ayar ekranları (hesap gerekir); telefonda deneme; kanal sorunu; telefon numarası/SMS girişi.
- APK: ön sürüm test-29, XNagram-13.2.1-45e351c-1262-arm64-v8a.apk (58,4 MB, geçici imza; v1262 üstüne kurulmaz).
- Açık: APK ~7,5 MB büyüdü (önceki test sürümleri ~50,9 MB), nedeni araştırılmadı. verCode 1262; main'e almadan 1263 yapılmalı. .github/scripts/__pycache__ yanlışlıkla commit'e girdi, silinecek.
- Giriş/emoji.pack: EmojiPack.java 13.2.1 ile yeni; sıkıştırılmış asset'te openFd hata verir, statik başlatma hatası Emoji.java:166'daki catch(Exception) ile yakalanmaz. Düzeltme 33a6ebd2 (noCompress); CI'da emoji.pack Stored. LoginActivity'de Nagram bağlantıları v1261 ile aynı (17). CI derlemesi APP_ID 6 kullanıyor (build.gradle:143), kendi API ID'miz yok.
- Devam noktası: sırada (1) APK boyut farkı, (2) kaybolan Nagram özellikleri tek tek (ChatActivity yazı biçimi menüsü, profil adı kopyalama, blur arka planlar, ses bitrate yaması, proxy sıfırlama), (3) girişi kullanıcı hesabıyla bir kez deneme.


## 2026-11-09 gece · Bütünlük denetimi (okuma) + dil eşleşmesi düzeltmesi (gelistirme, commit 2d59e0d6; ön sürüm test-30)
- Dil düzeltmesi: uygulama Türkçe için Locale("tr") kullanıyor, çeviri values-tr-rTR'de idi; LocalizationUtils "tr"yi bulamıyordu (Nagram ayarları İngilizce). GenerateLocalizationUtilsJavaTask artık dil-sadece yerel ayarı ilk bölge etiketine yönlendiriyor; TelegramStringsTask values-es-rES gibi bölge dosyalarını düz dil dosyasına da ekliyor. CI (gerçek APK, yerel ayar simülasyonu): tr→localization_tr_tr.bin 712/712, es 2005, pt→pt-BR 1946, ru, ja, zh tamam; emülatör: çökme yok, LOC_ERR yok. Doğrulanmadı: giriş yapılmış uygulamada ekran görüntüsü.
- Denetim yöntemi: resmî 12.10.0 (4e1a61eca6c9) ile v1261 farkı = Nagram yaması (596 değiştirilmiş + 1093 eklenmiş dosya); her eklenen satır ve ayar kullanımı güncel sürümde (resmî 13.2.1, 7be672ea) aranıyor. Eklenen Nagram dosyalarından yalnız gradle-daemon-jvm.properties eksik (önemsiz). Hiçbir Nagram ayarının kullanımı tamamen sıfırlanmamış (1228→1223 kullanım).
- KANITLI KAYIPLAR (v1261'de var, güncelde yok):
  1. ChatActivity metin seçim menüsü: kod/bahsetme/çeviri girişleri (v1261 satır 1801, 4414; text_mention 3→0). EditTextCaption'daki makeSelected* yöntemleri duruyor ama menüden çağrılmıyor.
  2. ProfileActivity: isme uzun basıp kopyalama (v1261 L5846) ve kendi emoji durumunu gösterme.
  3. QrActivity silindi (resmî kaldırdı); new QrActivity 7→0; TelegramQRCodeWriter'daki Nagram QR stili (23 satır) gitti.
  4. ProxyListActivity: "aramalar için proxy" seçeneği (useProxyForCalls, proxy_enabled_calls) tamamen yok (resmî de kaldırmış).
  5. Sesli arama özel bitrate: Instance.cpp L492 ve webrtc_voice_engine.cc L1151 yamaları yok; ayar arayüzde var, etkisi yok.
  6. BlurredBackgroundDrawable: StrokeOnViews ayarı 4 yerden 1'inde kaldı (v1261 L391); ProfileMusicView getDisableAvatarBlur 1 yer eksik (L108).
  7. LocaleController.fixContextLocale (Android kaynak dili) yok (v1261 L4595). CodeHighlighting thread-safety yaması (synchronized) yeni konumda yok (v1261 L223). VoIPController log yolu yaması yok. ExoPlayer WAV yamaları (media3'e geçildi) büyük olasılıkla yok, doğrulanmadı.
  8. MainTabsHelper kullanımı 75→65 (ContactsActivity, CallLogActivity, SettingsActivity, ProfileActivity): alt sekme boşluğu/yüksekliği ayarı bu ekranlarda uygulanmıyor olabilir, kontrol edilmeli.
  9. Görsel: blur3 arka planlar (BookmarkManagerActivity, NekoDelegateFragment) düz kaynağa döndü. SettingsActivity: "yeniden başlat" UndoView yok.
- Yanlış alarm çıkanlar (kayıp DEĞİL, resmî başka dosyaya taşımış ve Nagram yaması taşınmış): anket çevirisi (ChatMessagePoll), webp belge açma (ChatMessageLinkPreview), yükleme hızlandırma (FileUploadOperation, uploadBoost), güncelleme denetimi (GithubUpdateHelper), emoji paketi seçimi (Emoji.java).
- Açık: APK ~58,4 MB (önceki ~50,9 MB), nedeni bilinmiyor. verCode 1262 → main'e almadan 1263. Giriş/SMS, kanal sorunu doğrulanmadı.
- Devam noktası: kayıpları önem sırasıyla geri getirmek için kullanıcıdan plan onayı bekleniyor; sıra: 1 (metin menüsü), 2 (profil), 5 (bitrate native), 4 (proxy aramalar), 6-8, 3. Hiçbiri henüz yapılmadı.

## Kayıp Nagram özelliklerini geri getirme (test-31, e4b5c254)
- Kayıp 1 (metin seçim menüsü kod/bahsetme/çeviri): YANLIŞ ALARM. Menü 13.2.1'de R.id.menu_* ile zaten var (ChatActivity ~29829). Değişiklik yok.
- Kayıp 2: isme uzun basınca kopyalama GERİ EKLENDİ (ProfileActivity). Kendi emoji durumu zaten duruyor.
- Kayıp 4 (aramalar için proxy): Nagram değil, RESMİ özellikti; resmî 13.2.1 kaldırmış. Geri eklenmedi.
- Kayıp 6-8: fixContextLocale artık gereksiz (yeni yazı sistemi), CodeHighlighting synchronized var, ProfileMusicView var, StrokeOnViews kancası var. Eksik yok.
- AÇIK: Kayıp 5 (arama ses bitrate native yaması; tgcalls alt modül oldu, GroupInstanceCustomImpl yok, derleyip deneyemiyoruz) ve Kayıp 3 (Nagram QR stili; QrActivity resmî tarafça kaldırıldı).
- CI: Release Build + Emulator Test başarılı. verCode hâlâ 1262 (main sürümünden önce 1263 yapılmalı).
