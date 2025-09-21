# DiscordPunishBot v2.0.0

🇹🇷 **[Türkçe README için aşağı kaydırın](#türkçe-readme)** | 🇺🇸 **[English README](#english-readme)**

---

![bStats Servers](https://bstats.org/signatures/bukkit/Punish%20System.svg)


## 🇺🇸 English README

**A professional punishment, appeal, and report management system plugin for Minecraft servers with advanced Discord integration.**

## ✨ Key Features

### 🤖 **Advanced Discord Integration**
- **Interactive Button System**: Approve/reject appeals and reports directly from Discord
- **Rich Embed Messages**: Beautiful, detailed embed messages with status colors
- **Slash Commands**: Full Discord slash command support for all punishment types
- **Role-Based Permissions**: Assign specific Discord roles for different operations
- **Real-Time Notifications**: Instant Discord notifications for new appeals/reports
- **Multi-Channel Support**: Separate channels for logs, appeals, and reports

### ⚖️ **Complete Appeal System**
- **Appeal Commands**: `/appeal`, `/appeal list`, `/appeal status <id>`
- **Interactive Discord Buttons**: Approve, reject, or investigate appeals from Discord
- **Appeal Status Tracking**: Pending, Under Review, Approved, Rejected
- **Automatic Notifications**: Players get notified of appeal decisions
- **Cooldown System**: 24-hour cooldown between appeals
- **Admin Response System**: Admins can add responses to appeal decisions

### 📋 **Advanced Report System**
- **Report Commands**: `/report <player> <reason>`, `/report anonymous <player> <reason>`
- **Interactive Moderation**: Approve, reject, or investigate reports from Discord
- **Priority Levels**: Low, Medium, High, Urgent, Critical with color coding
- **Anonymous Reports**: Complete privacy protection for reporters
- **Status Tracking**: Pending, Under Review, Investigating, Resolved, Dismissed, Duplicate
- **Cooldown Management**: 5-minute cooldown between reports

### 🎁 **Configurable Reward System**
- **Console Command Rewards**: Execute any console command as reward
- **Flexible Configuration**: Different rewards for correct reports/appeals
- **Automatic Distribution**: Rewards given automatically upon approval
- **Custom Messages**: Configurable reward notification messages

### 🗄️ **Database & Performance**
- **Dual Database Support**: SQLite (default) and MySQL
- **Transaction Management**: Proper commit/rollback for data integrity
- **Connection Pool**: Optimized database connection management
- **Async Operations**: Non-blocking database operations for better performance
- **Error Recovery**: Automatic error handling and recovery mechanisms

## 🚀 Installation

### 1. Requirements
- **Java 17+**
- **Spigot/Paper 1.20+**
- **Essentials Plugin**
- **Discord Bot with permissions**

### 2. Plugin Setup
1. Download `DiscordPunishBot-2.0.0.jar`
2. Place in your `plugins` folder
3. Start server (config files will be generated)
4. Stop server and configure

### 3. Discord Bot Setup
1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create new application → Bot tab → Create bot
3. Copy bot token
4. OAuth2 → URL Generator:
   - **Scopes**: `bot`, `applications.commands`
   - **Permissions**: `Send Messages`, `Use Slash Commands`, `Read Message History`, `Manage Messages`
5. Add bot to your server using generated URL

### 4. Configuration

Edit `config.yml`:

```yaml
# Discord Configuration
discord:
  token: "YOUR_BOT_TOKEN_HERE"
  guild-id: "YOUR_GUILD_ID_HERE"
  
  # Channel Configuration
  log-channel-id: "LOG_CHANNEL_ID"           # General punishment logs
  appeal-channel-id: "APPEAL_CHANNEL_ID"     # Appeal notifications
  report-channel-id: "REPORT_CHANNEL_ID"     # Public report notifications  
  report-log-channel-id: "REPORT_LOG_ID"     # Admin report management
  
  # Admin Role Configuration
  report-admin-role-id: "REPORT_ADMIN_ROLE"  # Role for report management
  appeal-admin-role-id: "APPEAL_ADMIN_ROLE"  # Role for appeal management
  
  # Punishment Roles
  roles:
    mute: "MUTE_ROLE_ID"
    ban: "BAN_ROLE_ID"
    kick: "KICK_ROLE_ID"
    tempban: "TEMPBAN_ROLE_ID"
    jail: "JAIL_ROLE_ID"
    warn: "WARN_ROLE_ID"
    other: "OTHER_ROLE_ID"

# Reward System Configuration
reward-system:
  enabled: true
  
  # Commands to execute when a report is approved
  report-correct-commands:
    - "eco give %player% 100"
    - "tellraw %player% {\"text\":\"Thanks for the correct report! +100 coins\",\"color\":\"green\"}"
  
  # Commands to execute when an appeal is approved
  appeal-approved-commands:
    - "eco give %player% 50"
    - "tellraw %player% {\"text\":\"Your appeal was approved! +50 coins\",\"color\":\"green\"}"
  
  # Custom messages
  report-approved-message: "§a§l[REPORT] §7Your report was correct! You received a reward."
  appeal-approved-message: "§a§l[APPEAL] §7Your appeal was approved! You received a reward."
```

## 🎮 Commands

### Discord Slash Commands
- `/mute <player> [reason]` - Mute a player
- `/ban <player> [reason]` - Ban a player  
- `/kick <player> [reason]` - Kick a player
- `/tempban <player> [reason]` - Temporary ban
- `/jail <player> [reason]` - Jail a player
- `/warn <player> [reason]` - Warn a player
- `/diger <player> [reason]` - Other punishments
- `/ceza <player>` - View punishment history

### Minecraft Commands

#### General Commands
- `/ceza <player>` - View player's punishment history
- `/dpunish reload` - Reload configuration
- `/dpunish info` - Plugin information
- `/dpunish test <database/discord>` - Test connections
- `/dpunish stats` - Server statistics

#### Appeal Commands
- `/appeal` - Show available punishments to appeal
- `/appeal <punishment_id> <reason>` - Create new appeal
- `/appeal list` - List your appeals
- `/appeal status <appeal_id>` - Check appeal status

#### Report Commands
- `/report <player> <reason> [description]` - Create report
- `/report anonymous <player> <reason> [description]` - Anonymous report
- `/report list` - List your reports
- `/report status <report_id>` - Check report status

## 🔧 Discord Interactive Features

### Report Management Buttons
When a report is created, admins see these buttons in the report log channel:
- **✅ Approve & Manual Punishment** - Approve report, give rewards, requires manual punishment
- **❌ Reject** - Reject the report as invalid
- **🔍 Investigate** - Mark as under investigation (keeps buttons active)

### Appeal Management Buttons  
When an appeal is created, admins see these buttons in the appeal channel:
- **✅ Approve Appeal** - Approve appeal, remove punishment, give rewards
- **❌ Reject Appeal** - Reject the appeal as invalid
- **🔍 Investigate** - Mark as under investigation (keeps buttons active)

### Interactive Features
- **Real-time Updates**: Embed messages update automatically when status changes
- **Admin Tracking**: Shows which admin processed the request
- **Status Colors**: Different colors for pending, approved, rejected, investigating
- **Button States**: Buttons are disabled after action or remain active during investigation
- **Error Handling**: Graceful handling of deleted messages or invalid operations

## 📊 Permissions

```yaml
# General Permissions
discordpunish.*                    # All permissions
discordpunish.admin                # Admin permissions
discordpunish.reload               # Reload config
discordpunish.test                 # Test connections

# Punishment Permissions  
discordpunish.ceza                 # View punishment history

# Appeal Permissions
discordpunish.appeal.*             # All appeal permissions
discordpunish.appeal.create        # Create appeals
discordpunish.appeal.list          # List own appeals
discordpunish.appeal.viewall       # View all appeals (admin)

# Report Permissions
discordpunish.report.*             # All report permissions
discordpunish.report.create        # Create reports
discordpunish.report.list          # List own reports  
discordpunish.report.viewall       # View all reports (admin)
discordpunish.report.notify        # Receive report notifications
```

## 🛠️ Advanced Configuration

### Database Settings
```yaml
database:
  type: "sqlite"  # or "mysql"
  
  # MySQL Configuration (if using MySQL)
  host: "localhost"
  port: 3306
  database: "punishments"
  username: "your_username"
  password: "your_password"
```

### Language Settings
```yaml
language: "tr"  # "tr" for Turkish, "en" for English
```

### Customizing Punishments
```yaml
punishments:
  mute:
    spam:
      command: "essentials:mute %player% 30m Spam"
      display: "Spam (30 minutes)"
    toxic:
      command: "essentials:mute %player% 2h Toxic behavior"
      display: "Toxic Behavior (2 hours)"
```

## 🔧 Troubleshooting

### Common Issues

**Bot not responding to slash commands:**
1. Check bot token in config
2. Verify bot has `applications.commands` scope
3. Check role permissions in Discord
4. Use `/dpunish test discord` in-game

**Database errors:**
1. Check database type in config
2. For MySQL: verify connection details
3. For SQLite: check file permissions
4. Use `/dpunish test database` in-game

**Interactive buttons not working:**
1. Verify admin role IDs are correct
2. Check bot permissions in Discord channels
3. Ensure bot can manage messages
4. Check server logs for detailed errors

**Reports/Appeals not showing:**
1. Check channel IDs in config
2. Verify bot has access to channels  
3. Check user permissions
4. Use `/dpunish test discord` to verify setup

## 📈 Features in Detail

### Status System
- **Reports**: Pending → Under Review → Investigating → Resolved/Dismissed/Duplicate
- **Appeals**: Pending → Under Review → Approved/Rejected
- **Color Coding**: Each status has distinct colors in Discord embeds
- **Notifications**: Automatic player notifications for status changes

### Reward System
- **Configurable Commands**: Execute any console command as reward
- **Multiple Rewards**: Different rewards for different actions
- **Player Notifications**: Custom messages when rewards are given
- **Flexible Placeholders**: Use `%player%` in commands and messages

### Database Design
- **Punishments Table**: Core punishment data
- **Appeals Table**: Linked to punishments with foreign keys
- **Reports Table**: Independent reporting system
- **Indexes**: Optimized for performance
- **Transactions**: ACID compliance for data integrity

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/amhunter1/punishsystem/issues)
- **Discord Support**: [Join our Discord](https://discord.com/users/871721944268038175)
- **Email**: gfwilliamtr@gmail.com

---

## 🇹🇷 Türkçe README

**Minecraft sunucuları için Discord entegrasyonu ile gelişmiş ceza, itiraz ve şikayet yönetim sistemi.**

## ✨ Temel Özellikler

### 🤖 **Gelişmiş Discord Entegrasyonu**
- **İnteraktif Buton Sistemi**: İtiraz ve şikayetleri Discord'dan direkt onayla/reddet
- **Zengin Embed Mesajları**: Durum renkli güzel ve detaylı embed mesajları
- **Slash Komutları**: Tüm ceza türleri için Discord slash komut desteği
- **Rol Bazlı İzinler**: Farklı işlemler için özel Discord rolleri
- **Gerçek Zamanlı Bildirimler**: Yeni itiraz/şikayet için anında Discord bildirimleri
- **Multi-Kanal Desteği**: Log, itiraz ve şikayet için ayrı kanallar

### ⚖️ **Komple İtiraz Sistemi**
- **İtiraz Komutları**: `/appeal`, `/appeal list`, `/appeal status <id>`
- **İnteraktif Discord Butonları**: Discord'dan itirazları onayla, reddet veya incele
- **İtiraz Durum Takibi**: Beklemede, İnceleniyor, Onaylandı, Reddedildi
- **Otomatik Bildirimler**: Oyuncular itiraz kararlarından haberdar edilir
- **Cooldown Sistemi**: İtirazlar arası 24 saat bekleme
- **Admin Yanıt Sistemi**: Adminler itiraz kararlarına yanıt ekleyebilir

### 📋 **Gelişmiş Şikayet Sistemi**
- **Şikayet Komutları**: `/report <oyuncu> <sebep>`, `/report anonymous <oyuncu> <sebep>`
- **İnteraktif Moderasyon**: Discord'dan şikayetleri onayla, reddet veya incele
- **Öncelik Seviyeleri**: Düşük, Orta, Yüksek, Acil, Kritik - renk kodlamalı
- **Anonim Şikayetler**: Şikayetçi için tam gizlilik koruması
- **Durum Takibi**: Beklemede, İnceleniyor, Araştırılıyor, Çözüldü, Reddedildi, Tekrar
- **Cooldown Yönetimi**: Şikayetler arası 5 dakika bekleme

### 🎁 **Yapılandırılabilir Ödül Sistemi**
- **Konsol Komut Ödülleri**: Ödül olarak herhangi bir konsol komutu çalıştır
- **Esnek Yapılandırma**: Doğru şikayet/itiraz için farklı ödüller
- **Otomatik Dağıtım**: Onay sonrası ödüller otomatik verilir
- **Özel Mesajlar**: Yapılandırılabilir ödül bildirim mesajları

### 🗄️ **Veritabanı & Performans**
- **Çift Veritabanı Desteği**: SQLite (varsayılan) ve MySQL
- **Transaction Yönetimi**: Veri bütünlüğü için uygun commit/rollback
- **Bağlantı Havuzu**: Optimize edilmiş veritabanı bağlantı yönetimi
- **Async İşlemler**: Daha iyi performans için engelleyici olmayan işlemler
- **Hata Kurtarma**: Otomatik hata yönetimi ve kurtarma mekanizmaları

## 🚀 Kurulum

### 1. Gereksinimler
- **Java 17+**
- **Spigot/Paper 1.20+**
- **Essentials Plugin**
- **İzinli Discord Bot**

### 2. Plugin Kurulumu
1. `DiscordPunishBot-2.0.0.jar` dosyasını indirin
2. `plugins` klasörüne yerleştirin
3. Sunucuyu başlatın (config dosyaları oluşacak)
4. Sunucuyu durdurun ve yapılandırın

### 3. Discord Bot Kurulumu
1. [Discord Developer Portal](https://discord.com/developers/applications)'a gidin
2. Yeni uygulama oluşturun → Bot sekmesi → Bot oluştur
3. Bot token'ı kopyalayın
4. OAuth2 → URL Generator:
   - **Scope'lar**: `bot`, `applications.commands`
   - **İzinler**: `Send Messages`, `Use Slash Commands`, `Read Message History`, `Manage Messages`
5. Oluşan URL ile botu sunucunuza ekleyin

### 4. Yapılandırma

`config.yml` düzenleyin:

```yaml
# Discord Yapılandırması
discord:
  token: "BOT_TOKEN_BURAYA"
  guild-id: "SUNUCU_ID_BURAYA"
  
  # Kanal Yapılandırması
  log-channel-id: "LOG_KANAL_ID"           # Genel ceza logları
  appeal-channel-id: "ITIRAZ_KANAL_ID"     # İtiraz bildirimleri
  report-channel-id: "SIKAYET_KANAL_ID"    # Genel şikayet bildirimleri  
  report-log-channel-id: "SIKAYET_LOG_ID"  # Admin şikayet yönetimi
  
  # Admin Rol Yapılandırması
  report-admin-role-id: "SIKAYET_ADMIN_ROL"  # Şikayet yönetimi rolü
  appeal-admin-role-id: "ITIRAZ_ADMIN_ROL"   # İtiraz yönetimi rolü
  
  # Ceza Rolleri
  roles:
    mute: "MUTE_ROL_ID"
    ban: "BAN_ROL_ID"
    kick: "KICK_ROL_ID"
    tempban: "TEMPBAN_ROL_ID"
    jail: "JAIL_ROL_ID"
    warn: "WARN_ROL_ID"
    other: "OTHER_ROL_ID"

# Ödül Sistemi Yapılandırması
reward-system:
  enabled: true
  
  # Şikayet onaylandığında çalışacak komutlar
  report-correct-commands:
    - "eco give %player% 100"
    - "tellraw %player% {\"text\":\"Doğru şikayet için teşekkürler! +100 coin\",\"color\":\"green\"}"
  
  # İtiraz onaylandığında çalışacak komutlar  
  appeal-approved-commands:
    - "eco give %player% 50"
    - "tellraw %player% {\"text\":\"İtirazın onaylandı! +50 coin\",\"color\":\"green\"}"
  
  # Özel mesajlar
  report-approved-message: "§a§l[ŞİKAYET] §7Şikayetin doğruydu! Ödül aldın."
  appeal-approved-message: "§a§l[İTİRAZ] §7İtirazın onaylandı! Ödül aldın."
```

## 🎮 Komutlar

### Discord Slash Komutları
- `/mute <oyuncu> [sebep]` - Oyuncuyu sustur
- `/ban <oyuncu> [sebep]` - Oyuncuyu yasakla
- `/kick <oyuncu> [sebep]` - Oyuncuyu at
- `/tempban <oyuncu> [sebep]` - Geçici yasakla
- `/jail <oyuncu> [sebep]` - Oyuncuyu hapsset
- `/warn <oyuncu> [sebep]` - Oyuncuyu uyar
- `/diger <oyuncu> [sebep]` - Diğer cezalar
- `/ceza <oyuncu>` - Ceza geçmişini görüntüle

### Minecraft Komutları

#### Genel Komutlar
- `/ceza <oyuncu>` - Oyuncunun ceza geçmişini görüntüle
- `/dpunish reload` - Yapılandırmayı yeniden yükle
- `/dpunish info` - Plugin bilgileri
- `/dpunish test <database/discord>` - Bağlantıları test et
- `/dpunish stats` - Sunucu istatistikleri

#### İtiraz Komutları
- `/appeal` - İtiraz edilebilir cezaları göster
- `/appeal <ceza_id> <sebep>` - Yeni itiraz oluştur
- `/appeal list` - İtirazlarını listele
- `/appeal status <itiraz_id>` - İtiraz durumunu kontrol et

#### Şikayet Komutları
- `/report <oyuncu> <sebep> [açıklama]` - Şikayet oluştur
- `/report anonymous <oyuncu> <sebep> [açıklama]` - Anonim şikayet
- `/report list` - Şikayetlerini listele
- `/report status <sikayet_id>` - Şikayet durumunu kontrol et

## 🔧 Discord İnteraktif Özellikler

### Şikayet Yönetim Butonları
Şikayet oluşturulduğunda adminler şikayet log kanalında bu butonları görür:
- **✅ Onayla & Manuel Ceza** - Şikayeti onayla, ödül ver, manuel ceza gerekli
- **❌ Reddet** - Şikayeti geçersiz olarak reddet
- **🔍 İncelemeye Al** - İnceleme olarak işaretle (butonları aktif tutar)

### İtiraz Yönetim Butonları
İtiraz oluşturulduğunda adminler itiraz kanalında bu butonları görür:
- **✅ İtirazı Onayla** - İtirazı onayla, cezayı kaldır, ödül ver
- **❌ İtirazı Reddet** - İtirazı geçersiz olarak reddet
- **🔍 İncelemeye Al** - İnceleme olarak işaretle (butonları aktif tutar)

### İnteraktif Özellikler
- **Gerçek Zamanlı Güncellemeler**: Durum değiştiğinde embed mesajları otomatik güncellenir
- **Admin Takibi**: Hangi adminin işlemi yaptığını gösterir
- **Durum Renkleri**: Beklemede, onaylandı, reddedildi, inceleniyor için farklı renkler
- **Buton Durumları**: İşlem sonrası butonlar deaktive olur veya inceleme sırasında aktif kalır
- **Hata Yönetimi**: Silinmiş mesajlar veya geçersiz işlemler için zarif hata yönetimi

## 📊 İzinler

```yaml
# Genel İzinler
discordpunish.*                    # Tüm izinler
discordpunish.admin                # Admin izinleri
discordpunish.reload               # Config yenileme
discordpunish.test                 # Bağlantı testleri

# Ceza İzinleri
discordpunish.ceza                 # Ceza geçmişi görüntüleme

# İtiraz İzinleri
discordpunish.appeal.*             # Tüm itiraz izinleri
discordpunish.appeal.create        # İtiraz oluşturma
discordpunish.appeal.list          # Kendi itirazlarını listeleme
discordpunish.appeal.viewall       # Tüm itirazları görme (admin)

# Şikayet İzinleri
discordpunish.report.*             # Tüm şikayet izinleri
discordpunish.report.create        # Şikayet oluşturma
discordpunish.report.list          # Kendi şikayetlerini listeleme
discordpunish.report.viewall       # Tüm şikayetleri görme (admin)
discordpunish.report.notify        # Şikayet bildirimleri alma
```

## 📞 Destek

- **GitHub Issues**: [Hata raporu veya özellik isteği](https://github.com/amhunter1/punishsystem/issues)
- **Discord Destek**: [Discord'umuza katılın](https://discord.com/users/871721944268038175)
- **E-posta**: gfwilliamtr@gmail.com

---

**DiscordPunishBot v2.0.0** - Minecraft sunucunuz için profesyonel ceza, itiraz ve şikayet yönetim sistemi.

*Developed by Melut - 2025*
