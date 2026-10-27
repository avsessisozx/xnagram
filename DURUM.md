# xnagram DURUM (durum dalı, main'de yok)

Son güncelleme: 2026-10-27 · Aşama 0 (inceleme) tamam, kod değişikliği yok.

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
