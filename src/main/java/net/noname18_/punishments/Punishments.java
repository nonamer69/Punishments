package net.noname18_.punishments;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.noname18_.punishments.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.plugin.java.JavaPlugin;

public class Punishments implements Listener {

    public final Map<UUID, Long> mutedplayers = new HashMap<>();
    public final Map<UUID, Long> bannedplayers = new HashMap<>();

    private final Main plugin;
    private final File file;
    private final org.bukkit.configuration.file.FileConfiguration data;

    public Punishments(Main plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        file = new File(plugin.getDataFolder(), "punishments.yml");
        data = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);

        loadData();
    }

    public void saveData() {
        data.set("mutedplayers", null);
        data.set("bannedplayers", null);

        for (Map.Entry<UUID, Long> entry : mutedplayers.entrySet()) {
            data.set(
                    "mutedplayers." + entry.getKey(),
                    entry.getValue()
            );
        }

        for (Map.Entry<UUID, Long> entry : bannedplayers.entrySet()) {
            data.set(
                    "bannedplayers." + entry.getKey(),
                    entry.getValue()
            );
        }

        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save punishments.yml!");
            e.printStackTrace();
        }
    }

    public void loadData() {
        if (data.getConfigurationSection("mutedplayers") != null) {
            for (String uuidString : data.getConfigurationSection("mutedplayers").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long expiration = data.getLong(
                            "mutedplayers." + uuidString
                    );

                    if (expiration > System.currentTimeMillis()) {
                        mutedplayers.put(uuid, expiration);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        if (data.getConfigurationSection("bannedplayers") != null) {
            for (String uuidString : data.getConfigurationSection("bannedplayers").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    long expiration = data.getLong(
                            "bannedplayers." + uuidString
                    );

                    if (expiration > System.currentTimeMillis()) {
                        bannedplayers.put(uuid, expiration);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        saveData();
    }

    public void kick(Player player, String reason) {
        Component kickReason = Component.text(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l]\n§cYou have been kicked!\n \n§cReason: §4%s",
                        reason
                )
        );

        player.kick(kickReason);

        Bukkit.broadcastMessage(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l] §4%s §chas been kicked for §4%s§c!",
                        player.getName(),
                        reason
                )
        );
    }

    public long parseDuration(String duration) {
        duration = duration.toLowerCase().trim();

        if (duration.endsWith(" seconds") || duration.endsWith(" second")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 1000L;

        } else if (duration.endsWith(" minutes") || duration.endsWith(" minute")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 60000L;

        } else if (duration.endsWith(" hours") || duration.endsWith(" hour")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 3600000L;

        } else if (duration.endsWith(" days") || duration.endsWith(" day")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 86400000L;

        } else if (duration.endsWith(" weeks") || duration.endsWith(" week")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 604800000L;

        } else if (duration.endsWith(" months") || duration.endsWith(" month")) {
            String amount = duration.split(" ")[0];
            return Long.parseLong(amount) * 2592000000L;
        } else {
            throw new IllegalArgumentException(
                    "§8§l[§c§lPUNISHMENTS§8§l] Invalid duration: " + duration
            );
        }
    }

    public void mute(OfflinePlayer player, String duration, String reason) {
        long expiration = System.currentTimeMillis() + parseDuration(duration);

        mutedplayers.put(
                player.getUniqueId(),
                expiration
        );

        for (Player plyer : Bukkit.getOnlinePlayers()) {
            plyer.playSound(
                    plyer,
                    Sound.ENTITY_WITHER_SPAWN,
                    100,
                    1
            );
        }

        Bukkit.broadcastMessage(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l] §4%s §chas been muted for §4%s §cfor §4%s",
                        player.getName(),
                        duration,
                        reason
                )
        );

        saveData();
    }

    public void unmute(OfflinePlayer player) {
        mutedplayers.remove(player.getUniqueId());

        Bukkit.broadcastMessage(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l] §a%s has been unmuted!",
                        player.getName()
                )
        );

        saveData();
    }

    public long ismuted(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();

        Long expiration = mutedplayers.get(uuid);

        if (expiration == null) {
            return 0L;
        }

        if (expiration <= System.currentTimeMillis()) {
            mutedplayers.remove(uuid);
            saveData();
            return 0L;
        }

        return expiration;
    }

    public long isBanned(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();

        Long expiration = bannedplayers.get(uuid);

        if (expiration == null) {
            return 0L;
        }

        if (expiration <= System.currentTimeMillis()) {
            bannedplayers.remove(uuid);
            saveData();
            return 0L;
        }

        return expiration;
    }

    public void ban(OfflinePlayer player, String duration, String reason, String broadcastMsg) {
        long actualDuration = parseDuration(duration);
        long expiration = System.currentTimeMillis() + actualDuration;

        bannedplayers.put(
                player.getUniqueId(),
                expiration
        );

        for (Player plyer : Bukkit.getOnlinePlayers()) {
            plyer.playSound(
                    plyer,
                    Sound.ENTITY_ENDER_DRAGON_DEATH,
                    100,
                    1
            );
        }

        Bukkit.broadcastMessage(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l] §4%s §chas been banned for §4%s §cfor §4%s",
                        player.getName(),
                        duration,
                        reason
                )
        );

        player.ban(
                broadcastMsg,
                Duration.ofMillis(actualDuration),
                null
        );

        saveData();
    }

    public void unban(OfflinePlayer player) {
        bannedplayers.remove(player.getUniqueId());

        Bukkit.broadcastMessage(
                String.format(
                        "§8§l[§c§lPUNISHMENTS§8§l] §a%s has been unbanned!",
                        player.getName()
                )
        );

        saveData();
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Long expiration = bannedplayers.get(uuid);

        if (expiration == null) {
            return;
        }

        long timeLeft = expiration - System.currentTimeMillis();

        if (timeLeft <= 0) {
            bannedplayers.remove(uuid);
            saveData();
            return;
        }

        Component message = LegacyComponentSerializer.legacySection().deserialize(
                "§8§l[§c§lPUNISHMENTS§8§l]\n\n" +
                        "§cYou are §4banned§c!\n\n" +
                        "§cTime left: §4" + formatDurations(timeLeft)
        );

        event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
    }

    public String formatDurations(long duration) {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / 60000) % 60;
        long hours = (duration / 3600000) % 24;
        long days = (duration / 86400000) % 7;
        long weeks = (duration / 604800000) % 4;
        long months = duration / 2592000000L;

        StringBuilder result = new StringBuilder();

        if (months > 0) {
            result.append(months).append(" months ");
        }

        if (weeks > 0) {
            result.append(weeks).append(" weeks ");
        }

        if (days > 0) {
            result.append(days).append(" days ");
        }

        if (hours > 0) {
            result.append(hours).append(" hours ");
        }

        if (minutes > 0 && seconds > 0) {
            result.append(minutes).append(" minutes and ");
        } else if (minutes > 0) {
            result.append(minutes).append(" minutes.");
        }

        if (seconds > 0) {
            result.append(seconds).append(" seconds.");
        }

        if (result.length() == 0) {
            result.append("0 seconds.");
        }

        return result.toString();
    }
}