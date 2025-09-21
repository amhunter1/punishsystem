package com.melut.punishsystem.commands;

import com.melut.punishsystem.DiscordPunishBot;
import com.melut.punishsystem.models.Appeal;
import com.melut.punishsystem.models.Punishment;
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

public class AppealCommand implements CommandExecutor, TabCompleter {

    private final DiscordPunishBot plugin;

    public AppealCommand(DiscordPunishBot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getLanguageManager().getColoredMessage("general.player-only"));
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                showAvailablePunishments(player);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "list":
                    showPlayerAppeals(player);
                    break;
                case "status":
                    if (args.length < 2) {
                        player.sendMessage(plugin.getLanguageManager().getColoredMessage("appeal.usage-status"));
                        return true;
                    }
                    showAppealStatus(player, args[1]);
                    break;
                case "help":
                    showHelp(player);
                    break;
                default:
                    if (args.length < 2) {
                        player.sendMessage(plugin.getLanguageManager().getColoredMessage("appeal.usage-create"));
                        return true;
                    }
                    createAppeal(player, args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                    break;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("AppealCommand error: " + e.getMessage());
            e.printStackTrace();
            if (sender instanceof Player) {
                ((Player) sender).sendMessage("§c[İTİRAZ] Bir hata oluştu! Lütfen admin ile iletişime geçin.");
            }
        }

        return true;
    }

    private void showAvailablePunishments(Player player) {
        try {
            // Önce basit bir mesaj gönder
            player.sendMessage("§e[İTİRAZ] §7Ceza kayıtlarınız kontrol ediliyor...");
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Punishment> punishments = plugin.getPunishmentManager().getPlayerPunishments(player.getName(), 10);
                    
                    if (punishments.isEmpty()) {
                        player.sendMessage("§e[İTİRAZ] §7Aktif cezanız bulunmuyor.");
                        return;
                    }

                    player.sendMessage("§e[İTİRAZ] §7İtiraz edebileceğiniz cezalar:");
                    
                    boolean hasAppealablePunishments = false;
                    for (Punishment punishment : punishments) {
                        if (punishment.isActive() && !hasActiveAppeal(player.getUniqueId().toString(), punishment.getId())) {
                            hasAppealablePunishments = true;
                            String message = String.format("§7%d. §f%s §7- §e%s §7(%s) §8- §7Yetkili: §f%s",
                                punishment.getId(),
                                punishment.getType(),
                                punishment.getReason(),
                                punishment.getFormattedDate(),
                                punishment.getAdminName());
                            player.sendMessage(message);
                        }
                    }
                    
                    if (!hasAppealablePunishments) {
                        player.sendMessage("§c[İTİRAZ] §7İtiraz edebileceğiniz aktif ceza bulunmuyor.");
                        return;
                    }

                    player.sendMessage("§a/appeal <ceza_id> <itiraz_sebebi> §7- İtiraz oluştur");
                    
                } catch (Exception e) {
                    player.sendMessage("§c[İTİRAZ] §7Ceza kayıtları alınırken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("showAvailablePunishments error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage("§c[İTİRAZ] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("showAvailablePunishments outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showPlayerAppeals(Player player) {
        try {
            player.sendMessage("§e[İTİRAZ] §7İtirazlarınız listeleniyor...");
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<Appeal> appeals = plugin.getAppealManager().getPlayerAppeals(player.getUniqueId().toString(), 10);
                    
                    if (appeals.isEmpty()) {
                        player.sendMessage("§c[İTİRAZ] §7Hiç itirazınız bulunmuyor.");
                        return;
                    }

                    player.sendMessage("§e[İTİRAZ] §7" + player.getName() + " oyuncusunun itirazları:");
                    
                    for (Appeal appeal : appeals) {
                        String message = String.format("§7%d. §fCeza ID: §e%d §7- §f%s §7(%s)",
                            appeal.getId(),
                            appeal.getPunishmentId(),
                            getStatusInTurkish(appeal.getStatus()),
                            appeal.getFormattedAppealDate());
                        player.sendMessage(message);
                        
                        if (appeal.getAdminResponse() != null && !appeal.getAdminResponse().isEmpty()) {
                            String response = String.format("§a[ADMIN] §7%s: §f%s §7(%s)",
                                appeal.getReviewerName(),
                                appeal.getAdminResponse(),
                                appeal.getFormattedReviewDate());
                            player.sendMessage(response);
                        }
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[İTİRAZ] §7İtirazlar listelenirken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("showPlayerAppeals error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            player.sendMessage("§c[İTİRAZ] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("showPlayerAppeals outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAppealStatus(Player player, String appealIdStr) {
        try {
            int appealId = Integer.parseInt(appealIdStr);
            player.sendMessage("§e[İTİRAZ] §7İtiraz durumu kontrol ediliyor...");
            
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    Appeal appeal = plugin.getAppealManager().getAppeal(appealId);
                    
                    if (appeal == null) {
                        player.sendMessage("§c[İTİRAZ] §7İtiraz bulunamadı!");
                        return;
                    }

                    if (!appeal.getPlayerUuid().equals(player.getUniqueId().toString()) &&
                        !player.hasPermission("discordpunish.admin") && !player.hasPermission("discordpunish.*")) {
                        player.sendMessage("§c[İTİRAZ] §7Bu itiraz size ait değil!");
                        return;
                    }

                    player.sendMessage("§e[İTİRAZ] §7İtiraz Bilgileri:");
                    player.sendMessage("§7ID: §f" + appeal.getId());
                    player.sendMessage("§7Ceza ID: §f" + appeal.getPunishmentId());
                    player.sendMessage("§7Sebep: §f" + appeal.getAppealReason());
                    String statusTurkish = getStatusInTurkish(appeal.getStatus());
                    player.sendMessage("§7Durum: §f" + statusTurkish);
                    player.sendMessage("§7Tarih: §f" + appeal.getFormattedAppealDate());

                    if (appeal.getReviewerName() != null) {
                        player.sendMessage("§7İnceleyici: §f" + appeal.getReviewerName() + " §7(" + appeal.getFormattedReviewDate() + ")");
                    }

                    if (appeal.getAdminResponse() != null && !appeal.getAdminResponse().isEmpty()) {
                        player.sendMessage("§a[ADMIN YANITICI]: §f" + appeal.getAdminResponse());
                    }
                } catch (Exception e) {
                    player.sendMessage("§c[İTİRAZ] §7İtiraz durumu kontrol edilirken hata oluştu: " + e.getMessage());
                    plugin.getLogger().severe("showAppealStatus error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (NumberFormatException e) {
            player.sendMessage("§c[İTİRAZ] §7Geçersiz itiraz ID!");
        } catch (Exception e) {
            player.sendMessage("§c[İTİRAZ] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("showAppealStatus outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAppeal(Player player, String punishmentIdStr, String reason) {
        try {
            if (reason.trim().length() < 10) {
                player.sendMessage("§c[İTİRAZ] §7İtiraz sebebi çok kısa! (Min 10 karakter)");
                return;
            }

            if (reason.length() > 500) {
                player.sendMessage("§c[İTİRAZ] §7İtiraz sebebi çok uzun! (Max 500 karakter)");
                return;
            }

            try {
                int punishmentId = Integer.parseInt(punishmentIdStr);
                player.sendMessage("§e[İTİRAZ] §7İtiraz oluşturuluyor...");
                
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        if (plugin.getAppealManager().isOnCooldown(player.getUniqueId().toString())) {
                            long remaining = plugin.getAppealManager().getRemainingCooldown(player.getUniqueId().toString());
                            player.sendMessage("§c[İTİRAZ] §7Çok sık itiraz gönderiyorsunuz! Kalan süre: §e" + remaining + " saat");
                            return;
                        }

                        if (hasActiveAppeal(player.getUniqueId().toString(), punishmentId)) {
                            player.sendMessage("§c[İTİRAZ] §7Bu ceza için zaten aktif bir itiraz var!");
                            return;
                        }

                        Appeal appeal = plugin.getAppealManager().createAppeal(
                                player.getName(),
                                player.getUniqueId().toString(),
                                punishmentId,
                                reason
                        );

                        if (appeal != null) {
                            player.sendMessage("§a[İTİRAZ] §7İtiraz başarıyla oluşturuldu!");
                            player.sendMessage("§7İtiraz ID: §e" + appeal.getId());
                            player.sendMessage("§7Ceza ID: §e" + punishmentId);
                            
                            plugin.getLogger().info("[İTİRAZ] " + player.getName() + " oyuncusu " + punishmentId + " ID'li ceza için itiraz oluşturdu. İtiraz ID: " + appeal.getId());
                        } else {
                            player.sendMessage("§c[İTİRAZ] §7İtiraz oluşturulamadı! Lütfen tekrar deneyin.");
                        }
                    } catch (Exception e) {
                        player.sendMessage("§c[İTİRAZ] §7İtiraz oluşturulurken hata oluştu: " + e.getMessage());
                        plugin.getLogger().severe("createAppeal async error: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
            } catch (NumberFormatException e) {
                player.sendMessage("§c[İTİRAZ] §7Geçersiz ceza ID!");
            }
        } catch (Exception e) {
            player.sendMessage("§c[İTİRAZ] §7Bir hata oluştu: " + e.getMessage());
            plugin.getLogger().severe("createAppeal outer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showHelp(Player player) {
        try {
            player.sendMessage("§e§l[İTİRAZ] §7Yardım Menüsü:");
            player.sendMessage("§f/appeal §7- İtiraz edilebilir cezaları listele");
            player.sendMessage("§f/appeal <ceza_id> <sebep> §7- Yeni itiraz oluştur");
            player.sendMessage("§f/appeal status <itiraz_id> §7- İtiraz durumunu kontrol et");
            player.sendMessage("§f/appeal list §7- Tüm itirazlarını listele");
        } catch (Exception e) {
            player.sendMessage("§c[İTİRAZ] Yardım bilgisi alınırken hata oluştu.");
            plugin.getLogger().severe("showHelp error: " + e.getMessage());
        }
    }

    private boolean hasActiveAppeal(String playerUuid, int punishmentId) {
        return plugin.getAppealManager().hasActivePunishmentAppeal(playerUuid, punishmentId);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "status", "help"));
            
            Player player = (Player) sender;
            List<Punishment> punishments = plugin.getPunishmentManager().getPlayerPunishments(player.getName(), 10);
            for (Punishment punishment : punishments) {
                if (punishment.isActive() && !hasActiveAppeal(player.getUniqueId().toString(), punishment.getId())) {
                    completions.add(String.valueOf(punishment.getId()));
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            Player player = (Player) sender;
            List<Appeal> appeals = plugin.getAppealManager().getPlayerAppeals(player.getUniqueId().toString(), 10);
            for (Appeal appeal : appeals) {
                completions.add(String.valueOf(appeal.getId()));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .sorted()
                .toList();
    }
    
    private String getStatusInTurkish(Appeal.AppealStatus status) {
        if (status == null) return "§7Bilinmeyen";
        
        switch (status) {
            case PENDING:
                return "§eBekleniyor";
            case UNDER_REVIEW:
                return "§6İnceleniyor";
            case APPROVED:
                return "§aOnaylandı";
            case REJECTED:
                return "§cReddedildi";
            default:
                return "§7Bilinmeyen";
        }
    }
}