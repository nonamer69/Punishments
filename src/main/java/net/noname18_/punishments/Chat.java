package net.noname18_.punishments;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Chat implements Listener {
    private final Punishments punishments;

    public Chat(Punishments punishments) {
        this.punishments = punishments;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getName());
        OfflinePlayer playerr = Bukkit.getOfflinePlayer(event.getPlayer().getName());

        if (punishments.ismuted(playerr) > System.currentTimeMillis()) {
            long timeLeft = punishments.ismuted(playerr) - System.currentTimeMillis();
            if (player.isOnline()) {
                player.sendMessage(String.format("§c§8§l[§c§lPUNISHMENTS§8§l] §cUh oh! Looks like you are §4muted§c! This means that you cannot talk §4at all §cwhile in this state.\n \n §cTime left until unmute: §4%s", punishments.formatDurations(timeLeft)));
                event.setCancelled(true);
                return;
            }
        } else {
            event.setCancelled(false);
        }
    }
}
