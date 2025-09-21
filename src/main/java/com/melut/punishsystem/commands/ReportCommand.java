package com.melut.punishsystem.commands;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReportCommand implements CommandExecutor, TabCompleter {

    private final DiscordPunishBot plugin;
    private final List<String> commonReasons = Arrays.asList(
        "hacking", "griefing", "harassment", "spam", "inappropriate_language", 
        "trolling", "cheating", "exploiting", "advertising", "other"
    );

    public ReportCommand(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                showUsage(player);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "list":
                    showPlayerReports(player);
                    break;
                case "status":
                    if (args.length < 2) {
                        player.sendMessage("§cKullanım: §f/report status <şikayet_id>");
                        return true;
                    }
                    showReportStatus(player, args[1]);
                    break;
                case "help":
                    showHelp(player);
                    break;
                case "anonymous":
                case "anon":
                    if (args.length < 3) {
                        player.sendMessage("§cKullanım: §f/report anonymous <oyuncu> <sebep> [açıklama]");
                        return true;
                    }
                    String anonDescription = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 3, args.length)) : "";
                    createReport(player, args[1], args[2], anonDescription, true);
                    break;
                default:
                    if (args.length < 2) {
                        player.sendMessage("§cKullanım: §f/report <oyuncu> <sebep> [açıklama]");
                        return true;
                    }
                    String description = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
                    createReport(player, args[0], args[1], description, false);
                    break;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("ReportCommand error: " + e.getMessage());
            e.printStackTrace();
            if (sender instanceof Player) {
                ((Player) sender).sendMessage("§c[ŞİKAYET] Bir hata oluştu! Lütfen admin ile iletişime geçin.");
            }
        }

        return true;
    }

    private void showUsage(Player player) {
        try {
            player.sendMessage("§e§l[ŞİKAYET] §7Kullanım Bilgileri:");
            player.sendMessage("§f/report <oyuncu> <sebep> [açıklama] §7- Şikayet oluştur");
            player.sendMessage("§f/report anonymous <oyuncu> <sebep> [açıklama] §7- Anonim şikayet");
            player.sendMessage("§f/report list §7- Şikayetlerini listele");
            player.sendMessage("§f/report status <şikayet_id> §7- Şikayet durumu");
            player.sendMessage("§f/report help §7- Yardım menüsü");
        } catch (Exception e) {
            player.sendMessage("§c[ŞİKAYET] Kullanım bilgisi gösterilirken hata oluştu.");
            plugin.getLogger().severe("showUsage error: " + e.getMessage());
        }
    }

    private void showPlayerReports(Player player) {
        try {
            if (!player.hasPermission("discordpunish.report.list") && !player.hasPermission("discordpunish.report.*") && !player.hasPermission("discordpunish.*")) {
                player.sendMessage("§c[ŞİKAYET] §7Bu komutu kullanma yetkiniz yok!");
                return;
            }

            player.sendMessage("§e[ŞİKAYET] §7Şikayetleriniz listeleniyor...");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Report> reports = plugin.getReportManager().getPlayerReports(player.getUniqueId().toString(), 10);
                    
                    if (reports.isEmpty()) {
                        player.sendMessage("§c[ŞİKAYET] §7Hiç şikayetiniz bulunmuyor.");
                        return;
                    }

                    player.sendMessage("§e[ŞİKAYET] §7" + player.getName() + " oyuncusunun şikayetleri:");
                    
                    for (Report report : reports) {
                        String message = String.format("§7%d. §fŞikayet Edilen: §e%s §7- §f%s §7- §f%s §7- §f%s §7(%s)",
                            report.getId(),
                            report.getReportedPlayerName(),
                            report.getReason(),
                            report.getStatusDisplay(),
                            report.getPriorityDisplay(),
                            report.getFormattedReportDate());
                        player.sendMessage(message);
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[ŞİKAYET] §7Şikayetler listelenirken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("showPlayerReports error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage("§c[ŞİKAYET] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("showPlayerReports outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showReportStatus(Player player, String reportIdStr) {
        try {
            int reportId = Integer.parseInt(reportIdStr);
            player.sendMessage("§e[ŞİKAYET] §7Şikayet durumu kontrol ediliyor...");
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Report report = plugin.getReportManager().getReport(reportId);
                    
                    if (report == null) {
                        player.sendMessage("§c[ŞİKAYET] §7Şikayet bulunamadı!");
                        return;
                    }

                    if (!report.getReporterUuid().equals(player.getUniqueId().toString()) &&
                        !player.hasPermission("discordpunish.report.viewall") && !player.hasPermission("discordpunish.*")) {
                        player.sendMessage("§c[ŞİKAYET] §7Bu şikayet size ait değil!");
                        return;
                    }

                    player.sendMessage("§e[ŞİKAYET] §7Şikayet Bilgileri:");
                    player.sendMessage("§7ID: §f" + report.getId());
                    player.sendMessage("§7Şikayet Eden: §f" + report.getDisplayReporterName());
                    player.sendMessage("§7Şikayet Edilen: §f" + report.getReportedPlayerName());
                    player.sendMessage("§7Sebep: §f" + report.getReason());
                    
                    if (report.getDescription() != null && !report.getDescription().isEmpty()) {
                        player.sendMessage("§7Açıklama: §f" + report.getDescription());
                    }
                    
                    // Hardcode Türkçe durumlar
                    String statusTurkish = getStatusInTurkish(report.getStatus());
                    String priorityTurkish = getPriorityInTurkish(report.getPriority());
                    
                    player.sendMessage("§7Durum: §f" + statusTurkish);
                    player.sendMessage("§7Öncelik: §f" + priorityTurkish);
                    player.sendMessage("§7Tarih: §f" + report.getFormattedReportDate());

                    if (report.getReviewerName() != null) {
                        player.sendMessage("§7İnceleyici: §f" + report.getReviewerName() + " §7(" + report.getFormattedReviewDate() + ")");
                    }

                    if (report.getAdminResponse() != null && !report.getAdminResponse().isEmpty()) {
                        player.sendMessage("§a[ADMIN YANITICI]: §f" + report.getAdminResponse());
                    }

                    if (report.getEvidence() != null && !report.getEvidence().isEmpty()) {
                        player.sendMessage("§7Kanıt: §f" + report.getEvidence());
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[ŞİKAYET] §7Şikayet durumu kontrol edilirken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("showReportStatus error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (NumberFormatException e) {
            player.sendMessage("§c[ŞİKAYET] §7Geçersiz şikayet ID!");
        } catch (Exception e) {
            player.sendMessage("§c[ŞİKAYET] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("showReportStatus outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createReport(Player player, String targetPlayerName, String reason, String description, boolean anonymous) {
        try {
            if (!player.hasPermission("discordpunish.report.create") && !player.hasPermission("discordpunish.report.*") && !player.hasPermission("discordpunish.*")) {
                player.sendMessage("§c[ŞİKAYET] §7Bu komutu kullanma yetkiniz yok!");
                return;
            }

            if (reason.trim().length() < 3) {
                player.sendMessage("§c[ŞİKAYET] §7Sebep çok kısa! (Min 3 karakter)");
                return;
            }

            if (description != null && description.length() > 500) {
                player.sendMessage("§c[ŞİKAYET] §7Açıklama çok uzun! (Max 500 karakter)");
                return;
            }

            player.sendMessage("§e[ŞİKAYET] §7Şikayet oluşturuluyor...");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (plugin.getReportManager().isOnCooldown(player.getUniqueId().toString())) {
                        long remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId().toString());
                        player.sendMessage("§c[ŞİKAYET] §7Çok sık şikayet gönderiyorsunuz! Kalan süre: §e" + remaining + " dakika");
                        return;
                    }

                    OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
                    if (targetPlayer == null) {
                        player.sendMessage("§c[ŞİKAYET] §7Oyuncu bulunamadı: §e" + targetPlayerName);
                        return;
                    }

                    if (targetPlayer.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage("§c[ŞİKAYET] §7Kendinizi şikayet edemezsiniz!");
                        return;
                    }

                    int recentReports = plugin.getReportManager().getPlayerReportCount(targetPlayer.getUniqueId().toString(), 1);
                    if (recentReports >= 5) {
                        player.sendMessage("§c[ŞİKAYET] §7Bu oyuncu hakkında çok fazla yakın zamanda şikayet var!");
                        return;
                    }

                    Report report = plugin.getReportManager().createReport(
                            player.getName(),
                            player.getUniqueId().toString(),
                            targetPlayer.getName(),
                            targetPlayer.getUniqueId().toString(),
                            reason,
                            description,
                            anonymous
                    );

                    if (report != null) {
                        player.sendMessage("§a[ŞİKAYET] §7Şikayet başarıyla oluşturuldu!");
                        player.sendMessage("§7Şikayet ID: §e" + report.getId());
                        player.sendMessage("§7Şikayet Edilen: §e" + targetPlayer.getName());
                        player.sendMessage("§7Anonim: §e" + (anonymous ? "Evet" : "Hayır"));
                        
                        plugin.getLogger().info("[ŞİKAYET] " + report.getDisplayReporterName() + " oyuncusu " + targetPlayer.getName() + " hakkında şikayet oluşturdu. Şikayet ID: " + report.getId() + ", Sebep: " + reason);
                    } else {
                        player.sendMessage("§c[ŞİKAYET] §7Şikayet oluşturulamadı! Lütfen tekrar deneyin.");
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[ŞİKAYET] §7Şikayet oluşturulurken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("createReport async error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage("§c[ŞİKAYET] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("createReport outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showHelp(Player player) {
        try {
            player.sendMessage("§e§l[ŞİKAYET] §7Yardım Menüsü:");
            player.sendMessage("§f/report <oyuncu> <sebep> [açıklama] §7- Şikayet oluştur");
            player.sendMessage("§f/report anonymous <oyuncu> <sebep> [açıklama] §7- Anonim şikayet");
            player.sendMessage("§f/report list §7- Şikayetlerinizi listele");
            player.sendMessage("§f/report status <şikayet_id> §7- Şikayet durumunu kontrol et");
            player.sendMessage("§a§lMevcut Sebepler:");
            
            String reasonsList = String.join(", ", commonReasons);
            player.sendMessage("§7" + reasonsList);
        } catch (Exception e) {
            player.sendMessage("§c[ŞİKAYET] Yardım bilgisi gösterilirken hata oluştu.");
            plugin.getLogger().severe("showHelp error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Report status'unu Türkçeye çevirir
     */
    private String getStatusInTurkish(Report.ReportStatus status) {
        if (status == null) return "§7Bilinmeyen";
        
        switch (status) {
            case PENDING:
                return "§eBekleniyor";
            case UNDER_REVIEW:
                return "§6İnceleniyor";
            case INVESTIGATING:
                return "§bAraştırılıyor";
            case RESOLVED:
                return "§aÇözüldü";
            case DISMISSED:
                return "§cReddedildi";
            case DUPLICATE:
                return "§7Tekrar Eden";
            default:
                return "§7Bilinmeyen";
        }
    }
    
    /**
     * Report priority'yi Türkçeye çevirir
     */
    private String getPriorityInTurkish(Report.ReportPriority priority) {
        if (priority == null) return "§7Bilinmeyen";
        
        switch (priority) {
            case LOW:
                return "§7Düşük";
            case MEDIUM:
                return "§eOrta";
            case HIGH:
                return "§6Yüksek";
            case URGENT:
                return "§cAcil";
            case CRITICAL:
                return "§4Kritik";
            default:
                return "§7Bilinmeyen";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "status", "help", "anonymous", "anon"));
            
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    completions.add(onlinePlayer.getName());
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("status")) {
                List<Report> reports = plugin.getReportManager().getPlayerReports(player.getUniqueId().toString(), 20);
                for (Report report : reports) {
                    completions.add(String.valueOf(report.getId()));
                }
            } else if (args[0].equalsIgnoreCase("anonymous") || args[0].equalsIgnoreCase("anon")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.equals(player)) {
                        completions.add(onlinePlayer.getName());
                    }
                }
            } else {
                completions.addAll(commonReasons);
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("anonymous") || args[0].equalsIgnoreCase("anon"))) {
            completions.addAll(commonReasons);
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }
}