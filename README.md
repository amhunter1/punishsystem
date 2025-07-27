# DiscordPunishBot

Discord üzerinden Minecraft sunucunuzda ceza sistemi yönetebileceğiniz profesyonel bir plugin.

## ✨ Özellikler

- **Discord Entegrasyonu**: Discord slash komutları ile ceza verme
- **Rol Bazlı Yetkilendirme**: Farklı ceza türleri için farklı roller
- **Gelişmiş Veritabanı**: SQLite veya MySQL desteği
- **Ceza Geçmişi**: Oyuncuların detaylı ceza kayıtları
- **Özelleştirilebilir**: Tüm ceza türleri ve süreleri config'ten ayarlanabilir
- **İstatistikler**: Sunucu ceza istatistikleri

## 📸 Önizleme

![Oyun içi /ceza <oyuncu> örneği](assets/screenshots/demo1.png)
![Kullanıcı Paneli](assets/screenshots/demo2.png)
![Kullanıcı Paneli2](assets/screenshots/demo3.png)
![Kullanıcı Paneli3](assets/screenshots/demo4.png)
![Discord log mesajı örneği](assets/screenshots/demo5.png)
![Sunucu konsol mesaj örneği](assets/screenshots/demo6.png)



## 🚀 Kurulum

### 1. Gereksinimler
- Java 17+
- Spigot/Paper 1.20+
- Essentials plugin
- Discord botu

### 2. Plugin Kurulumu
1. `DiscordPunishBot.jar` dosyasını `plugins` klasörüne atın
2. Sunucuyu başlatın (ilk başlatmada config dosyaları oluşacak)
3. Sunucuyu durdurun

### 3. Discord Bot Kurulumu
1. [Discord Developer Portal](https://discord.com/developers/applications)'a gidin
2. Yeni bir uygulama oluşturun
3. "Bot" sekmesine gidin ve bot oluşturun
4. Bot token'ını kopyalayın
5. "OAuth2" > "URL Generator" sekmesinden:
   - Scopes: `bot`, `applications.commands`
   - Bot Permissions: `Send Messages`, `Use Slash Commands`, `Read Message History`
6. Oluşan URL ile botu sunucunuza ekleyin

### 4. Konfigürasyon
`config.yml` dosyasını düzenleyin:

```yaml
discord:
  token: "BOT_TOKEN_BURAYA"
  guild-id: "SUNUCU_ID_BURAYA"
  log-channel-id: "LOG_KANAL_ID_BURAYA"  # Ceza loglarının gönderileceği kanal
  roles:
    mute: "MUTE_YETKISI_ROL_ID"
    ban: "BAN_YETKISI_ROL_ID"
    other: "DIGER_YETKISI_ROL_ID"
```

### 5. Kanal ve Rol ID'lerini Alma
1. Discord'da Developer Mode'u aktifleştirin (User Settings > Advanced > Developer Mode)
2. Role veya kanala sağ tıklayıp "Copy ID" seçin
3. ID'leri config.yml'e yapıştırın

**Log Kanalı**: Ceza loglarının otomatik olarak gönderileceği kanal ID'sini `log-channel-id` kısmına yazın.

## 🎮 Kullanım

### Discord Komutları
- `/mute <oyuncu> [sebep]` - Oyuncuyu sustur
- `/ban <oyuncu> [sebep]` - Oyuncuyu yasakla
- `/diger <oyuncu> [sebep]` - Diğer ceza türleri
- `/ceza <oyuncu>` - Ceza geçmişini görüntüle

### Minecraft Komutları
- `/ceza <oyuncu>` - Oyuncunun ceza geçmişini görüntüle
- `/dpunish reload` - Konfigürasyonu yeniden yükle
- `/dpunish info` - Plugin bilgileri
- `/dpunish test <database/discord>` - Bağlantıları test et
- `/dpunish stats` - Sunucu istatistikleri

## ⚙️ Ceza Türleri Özelleştirme

Config'te ceza türlerini özelleştirebilirsiniz:

```yaml
punishments:
  mute:
    yeni-sebep:
      command: "essentials:mute %player% 5h %reason%"
      display: "Yeni Sebep"
```

### Placeholder'lar
- `%player%` - Cezalı oyuncu adı
- `%reason%` - Ceza sebebi
- `%admin%` - Cezayı veren yetkili

## 🗃️ Veritabanı

### SQLite (Varsayılan)
```yaml
database:
  type: "sqlite"
```

### MySQL
```yaml
database:
  type: "mysql"
  host: "localhost"
  port: 3306
  database: "punishments"
  username: "kullanici_adi"
  password: "sifre"
```

## 🛠️ Geliştirme

### Proje Yapısı
```
src/main/java/com/example/discordpunish/
├── DiscordPunishBot.java          # Ana plugin sınıfı
├── commands/
│   ├── CezaCommand.java           # /ceza komutu
│   └── DPunishCommand.java        # /dpunish komutu
├── database/
│   └── DatabaseManager.java      # Veritabanı yönetimi
├── discord/
│   └── DiscordBot.java           # Discord bot
├── managers/
│   ├── ConfigManager.java        # Konfigürasyon yönetimi
│   └── PunishmentManager.java    # Ceza sistemi
└── models/
    └── Punishment.java           # Ceza modeli
```

### Build Etme
```bash
mvn clean package
```

## 📊 İstatistikler

Plugin aşağıdaki istatistikleri tutar:
- Toplam ceza sayısı
- Ceza türlerine göre dağılım
- Günlük ceza sayısı
- Cezalı oyuncu sayısı
- Oyuncu başına ceza geçmişi

## 🔧 Sorun Giderme

### Bot Bağlanmıyor
1. Token'ın doğru olduğundan emin olun
2. Bot'un sunucuya eklendiğini kontrol edin
3. Bot'un gerekli izinlere sahip olduğunu kontrol edin

### Komutlar Çalışmıyor
1. Rol ID'lerinin doğru olduğundan emin olun
2. Kullanıcının gerekli role sahip olduğunu kontrol edin
3. `/dpunish test discord` ile bağlantıyı test edin

### Veritabanı Hataları
1. `/dpunish test database` ile bağlantıyı test edin
2. MySQL kullanıyorsanız bağlantı bilgilerini kontrol edin
3. Dosya izinlerini kontrol edin (SQLite için)

## 📝 Değişiklik Geçmişi

### v1.0.0
- İlk sürüm yayınlandı
- Discord slash komutları
- Ceza geçmişi sistemi
- SQLite/MySQL desteği
- Özelleştirilebilir ceza türleri

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🤝 Katkıda Bulunma

1. Bu repository'yi fork edin
2. Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -am 'Yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/yeni-ozellik`)
5. Pull Request oluşturun

## 📞 Destek

Herhangi bir sorun yaşarsanız:
- GitHub Issues kullanın
- [Discord sunucumuza katılın](https://discord.com/users/871721944268038175)
- [E-posta ile iletişime geçin](gfwilliamtr@gmail.com)

---

**DiscordPunishBot** - Minecraft sunucunuz için profesyonel ceza yönetim sistemi.
