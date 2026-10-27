# XNagram

Telegram için, [Nagram X](https://github.com/risin42/NagramX) tabanlı, kişisel kullanım amaçlı bir Android istemci forku. Resmi bir Telegram uygulaması değildir ve Telegram ile bağlantısı yoktur.

*English: a personal fork of Nagram X (Telegram client for Android) with a persistent translate button and its own update channel. Not affiliated with Telegram.*

## Neler farklı

- **Çeviri butonu:** Sohbette her zaman erişilebilir. Yeri ayarlanabilir: sohbet üst çubuğunda simge (varsayılan), ⋮ menüsünde, sabit banner veya Telegram'ın kendi banner'ı. Ayar: Çeviri ayarları → Çeviri butonu.
- **Hesap riski olan özellikler yok:** Silinen mesajları kaydetme gibi Telegram kullanım şartlarına aykırı olabilecek özellikler eklenmedi. Upstream'in "base" sürümüyle aynı yaklaşım.
- **Kendi güncelleme kaynağı:** Uygulama güncellemeleri bu reponun [Releases](https://github.com/avsessisozx/xnagram/releases) sayfasından kontrol edilir.
- **Ayrı paket adı:** `com.avsessisozx.xnagram`. Resmi Telegram veya Nagram X ile aynı telefonda yan yana kurulur.
- **Çökme raporu ve analitik kapalı.**

## Bilinen sınırlar

- **Anlık bildirimler (push) çalışmaz.** Bu özellik upstream'in Firebase projesine bağlıydı. Bildirim için kendi Firebase projesi gerekir.
- Yalnızca arm64-v8a APK üretilir.

## İndirme ve kurulum

1. [Releases](https://github.com/avsessisozx/xnagram/releases) sayfasından en son sürümün `arm64-v8a.apk` dosyasını indir.
2. Telefonda kur (bilinmeyen kaynaklardan yüklemeye izin vermen istenebilir).

APK imza sertifikasını doğrulamak için SHA-256 parmak izi:

```
3C:FC:5C:AC:E9:E2:62:0E:34:17:B8:DE:E9:9D:5A:A1:C7:81:09:93:1C:7E:EA:95:58:72:C0:B6:E7:13:52:F7
```

`test-*` adlı sürümler geçici anahtarla imzalanmış deneme derlemeleridir, günlük kullanım için değildir.

## Derleme

Derleme GitHub Actions ile yapılır (`.github/workflows/release.yml`). Kendi fork'unda derlemek için repo secret'ları olarak şunları ekle:

| Secret | Anlamı |
| --- | --- |
| `XN_KEYSTORE_B64` | İmza anahtarı dosyasının (`.jks`) base64 hali |
| `XN_KEYSTORE_PASS` | Anahtar deposu şifresi |
| `XN_KEY_ALIAS` | Anahtar takma adı |
| `XN_KEY_PASS` | Anahtar şifresi |

Secret yoksa derleme geçici bir anahtarla yapılır ve sürüm olarak yayınlanmaz. `main` dalına gönderilen ve kalıcı anahtarla derlenen sürüm otomatik olarak Releases'e eklenir.

Yerelde derlemek için:

1. `git clone --recursive --shallow-submodules https://github.com/avsessisozx/xnagram.git`
2. [Telegram geliştirici sayfasından](https://my.telegram.org/auth) kendi `TELEGRAM_APP_ID` ve `TELEGRAM_APP_HASH` değerlerini al. Proje köküne `local.properties` oluştur ve ekle:
   ```
   TELEGRAM_APP_ID=<app_id>
   TELEGRAM_APP_HASH=<app_hash>
   KEYSTORE_PASS=<şifre>
   ALIAS_NAME=<takma_ad>
   ALIAS_PASS=<şifre>
   ```
3. İmza için `TMessagesProj/release.keystore` dosyasını kendi anahtarınla oluştur, ardından Android Studio ile aç (NDK 27.2, JDK 21 gerekir).

## Kaynaklar ve teşekkür

Bu proje başkalarının açık kaynak çalışmasına dayanır:

- [Nagram X](https://github.com/risin42/NagramX) (risin42): doğrudan taban. Upstream arşivlenmiştir.
- [Nagram](https://github.com/NextAlone/Nagram) (NextAlone)
- [Nekogram](https://github.com/Nekogram/Nekogram)
- [Telegram Android](https://github.com/DrKLO/Telegram) (resmi Telegram'ın açık kaynak kodu)
- Nagram X'in kendi teşekkür listesi: [AyuGram](https://github.com/AyuGram/AyuGram4A), [Cherrygram](https://github.com/arsLan4k1390/Cherrygram), [NotSoAndroidAyuGram](https://github.com/Dr4iv3rNope/NotSoAndroidAyuGram), [exteraGram](https://github.com/exteraSquad/exteraGram), [OctoGram](https://github.com/OctoGramApp/OctoGram)

## Lisans

[GPL-3.0](LICENSE). Kaynak kod bu repodadır; değişiklikler commit geçmişinde görülebilir.
