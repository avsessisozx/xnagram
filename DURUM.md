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
