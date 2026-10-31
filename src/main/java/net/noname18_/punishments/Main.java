package net.noname18_.punishments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {
    private Punishments punishments;

    private OfflinePlayer targetPlyr;

    Map<Player, Integer> durationIndexes = new HashMap<>();

    Map<Player, ArrayList<OfflinePlayer>> punishmentData = new HashMap<>();

    Map<Player, String> punishmentType = new HashMap<>();

    Map<Player, String> reasons = new HashMap<>();

    Map<Player, String> durations = new HashMap<>();

    @Override
    public void onEnable() {
        punishments = new Punishments(this);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(punishments, this);
        getServer().getPluginManager().registerEvents(new Chat(punishments), this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mute")) {
            if (!sender.hasPermission("mute.use")) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cYou have no permission to use this!");
                return true;
            }
            if (args.length != 1) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid arguments! Usage: /mute <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (!(sender instanceof Player player)) {
                return false;
            }

            punishmentType.put(player, "mute");

            Inventory mutemenu = Bukkit.createInventory(player, 45, "Mute " + target.getName());

            ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = borderPane.getItemMeta();
            meta.setDisplayName(" ");
            borderPane.setItemMeta(meta);

            int rows = 5;
            int columns = 9;

            for (int slot = 0; slot < rows * columns; slot++) {
                int row = slot / columns;
                int column = slot % columns;

                if (row == 0 || row == rows - 1 || column == 0 || column == columns - 1) {
                    mutemenu.setItem(slot, borderPane);
                }
            }

            ItemStack cancel = new ItemStack(Material.BARRIER);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName("§cCancel");
            cancelMeta.setLore(List.of("§7Click to close this gui"));
            cancel.setItemMeta(cancelMeta);
            mutemenu.setItem(40, cancel);

            ItemStack custom = new ItemStack(Material.MAP);
            ItemMeta customMeta = custom.getItemMeta();
            customMeta.setDisplayName("§cCustom punishment");
            customMeta.setLore(List.of("§7Click to set a custom punishment"));
            custom.setItemMeta(customMeta);
            mutemenu.setItem(4, custom);

            String[] reasons = {
                    "Spamming",
                    "Inappropriate Behavior",
                    "Racism / Hate speech",
                    "Advertising",
                    "Insufferable",
                    "Toxicity"
            };

            int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };

            String[] durations = {
                    "10 seconds",
                    "30 seconds",
                    "1 minute",
                    "5 minutes",
                    "10 minutes",
                    "30 minutes",
                    "1 hour",
                    "5 hours",
                    "1 day",
                    "5 days",
                    "1 week",
                    "2 weeks",
                    "3 weeks",
                    "1 month",
                    "3 months",
                    "6 months",
                    "12 months",
                    "24 months"
            };

            for (int i = 0; i < reasons.length; i++) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.setDisplayName("§c" + reasons[i]);
                itemMeta.setLore(List.of(
                        "§7Left click to punish",
                        "§7Right click to cycle duration",
                        "",
                        "§7Duration: §c" + durations[0]
                ));

                item.setItemMeta(itemMeta);
                mutemenu.setItem(slots[i], item);
            }

            targetPlyr = target;
            player.openInventory(mutemenu);

            ArrayList<OfflinePlayer> data = new ArrayList<>();
            data.add(target);
            punishmentData.put(player, data);

            //punishments.mute(target, args[1], args[2]);
        } else if (command.getName().equalsIgnoreCase("unmute")) {
            if (!sender.hasPermission("mute.use")) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cYou have no permission to use this!");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid arguments! Usage: /unmute <player>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid player! /unmute <player>");
                return true;
            }
            if (punishments.ismuted(target) <= 0) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cThis player is not muted!");
                return true;
            }
            punishments.unmute(target);
        } else if (command.getName().equalsIgnoreCase("kick")) {
            if (!sender.hasPermission("kick.use")) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cYou have no permission to use this!");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid arguments! /kick <player> <reason>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid player! /kick <player> <reason>");
                return true;
            }
            punishments.kick(target, args[1]);
        } else if (command.getName().equalsIgnoreCase("ban")) {
            if (!sender.hasPermission("ban.use")) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cYou have no permission to use this!");
                return true;
            }

            if (args.length != 1) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid arguments! Usage: /ban <player>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (!(sender instanceof Player player)) {
                return false;
            }

            punishmentType.put(player, "ban");

            Inventory banmenu = Bukkit.createInventory(player, 45, "Ban " + target.getName());

            ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = borderPane.getItemMeta();
            meta.setDisplayName(" ");
            borderPane.setItemMeta(meta);

            int rows = 5;
            int columns = 9;

            for (int slot = 0; slot < rows * columns; slot++) {
                int row = slot / columns;
                int column = slot % columns;

                if (row == 0 || row == rows - 1 || column == 0 || column == columns - 1) {
                    banmenu.setItem(slot, borderPane);
                }
            }

            String[] reasons = {
                    "Cheating / Unfair advantages",
                    "Harassment(Extreme)",
                    "Racism / Hate speech(Extreme)",
                    "Advertising(Extreme)",
                    "Misleading staff(Extreme)",
                    "Inappropriate Behavior(Extreme)"
            };

            int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };

            String[] durations = {
                    "10 seconds",
                    "30 seconds",
                    "1 minute",
                    "5 minutes",
                    "10 minutes",
                    "30 minutes",
                    "1 hour",
                    "5 hours",
                    "1 day",
                    "5 days",
                    "1 week",
                    "2 weeks",
                    "3 weeks",
                    "1 month",
                    "3 months",
                    "6 months",
                    "12 months",
                    "24 months"
            };

            for (int i = 0; i < reasons.length; i++) {
                ItemStack item = new ItemStack(Material.NETHERITE_INGOT);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.setDisplayName("§c" + reasons[i]);
                itemMeta.setLore(List.of(
                        "§7Left click to punish",
                        "§7Right click to cycle duration",
                        "",
                        "§7Duration: §c" + durations[0]
                ));

                item.setItemMeta(itemMeta);
                banmenu.setItem(slots[i], item);
            }

            ItemStack cancel = new ItemStack(Material.BARRIER);
            ItemMeta cancelMeta = cancel.getItemMeta();
            cancelMeta.setDisplayName("§cCancel");
            cancelMeta.setLore(List.of("§7Click to close this gui"));
            cancel.setItemMeta(cancelMeta);
            banmenu.setItem(40, cancel);

            ItemStack custom = new ItemStack(Material.MAP);
            ItemMeta customMeta = custom.getItemMeta();
            customMeta.setDisplayName("§cCustom punishment");
            customMeta.setLore(List.of("§7Click to set a custom punishment"));
            custom.setItemMeta(customMeta);
            banmenu.setItem(4, custom);

            targetPlyr = target;
            player.openInventory(banmenu);

            ArrayList<OfflinePlayer> data = new ArrayList<>();
            data.add(target);
            punishmentData.put(player, data);

        } else if (command.getName().equalsIgnoreCase("unban")) {
            if (!sender.hasPermission("unban.use")) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cYou have no permission to use this! /unban <player>");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid arguments! /unban <player>");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cInvalid player! /unban <player>");
                return true;
            }

            if (punishments.isBanned(target) <= 0) {
                sender.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cThis player is not banned!");
                return true;
            }

            punishments.unban(target);
        }

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Mute") || event.getView().getTitle().contains("Ban")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                event.getView().close();
            }

            String[] reasons = {
                    "Spamming",
                    "Inappropriate Behavior",
                    "Racism / Hate speech",
                    "Advertising",
                    "Insufferable",
                    "Toxicity"
            };

            String[] banReasons = {
                    "Cheating / Unfair advantages",
                    "Harassment(Extreme)",
                    "Racism / Hate speech(Extreme)",
                    "Advertising(Extreme)",
                    "Misleading staff(Extreme)",
                    "Inappropriate Behavior(Extreme)"
            };

            int[] slots = {
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            };

            String[] durations = {
                    "10 seconds",
                    "30 seconds",
                    "1 minute",
                    "5 minutes",
                    "10 minutes",
                    "30 minutes",
                    "1 hour",
                    "5 hours",
                    "1 day",
                    "5 days",
                    "1 week",
                    "2 weeks",
                    "3 weeks",
                    "1 month",
                    "3 months",
                    "6 months",
                    "12 months",
                    "24 months"
            };

            ArrayList<OfflinePlayer> data = punishmentData.get(player);

            if (data == null) {
                return;
            }

            OfflinePlayer target = data.get(0);

            for (int i = 0; i < slots.length; i++) {
                if (slot == slots[i]) {
                    if (event.getClick().isRightClick()) {

                        int index = durationIndexes.getOrDefault(player, 0);

                        index++;

                        if (index >= durations.length) {
                            index = 0;
                        }

                        durationIndexes.put(player, index);

                        String duration = durations[index];

                        ItemStack item = event.getView().getTopInventory().getItem(slot);

                        if (item != null && item.hasItemMeta()) {
                            ItemMeta meta = item.getItemMeta();

                            meta.setLore(List.of(
                                    "§7Left click to punish",
                                    "§7Right click to cycle duration",
                                    "",
                                    "§7Duration: §c" + duration
                            ));

                            item.setItemMeta(meta);
                        }

                        return;
                    }

                    if (event.getClick().isLeftClick()) {
                        int index = durationIndexes.getOrDefault(player, 0);
                        String duration = durations[index];

                        if (event.getView().getTitle().contains("Mute")) {
                            punishments.mute(
                                    target,
                                    duration,
                                    reasons[i]
                            );

                            punishmentType.remove(player);

                            event.getView().close();
                        } else if (event.getView().getTitle().contains("Ban")) {
                            String message = (
                                    "§8§l[§c§lPUNISHMENTS§8§l]\n\n" +
                                            "§cYou are §4banned§c!\n\n" +
                                            "§cReason: §4" + banReasons[i] +
                                            "\n§cTime left: §4" + punishments.formatDurations(
                                            punishments.parseDuration(duration)
                                    )
                            );

                            punishments.ban(
                                    target,
                                    duration,
                                    banReasons[i],
                                    message
                            );

                            punishmentType.remove(player);
                            event.getView().close();
                            return;
                        }

                    }
                }
            }
        }
        if (event.getClick().isLeftClick()) {
            Player player = (Player) event.getWhoClicked();
            if (event.getView().getTitle().contains("Mute") || event.getView().getTitle().contains("Ban")) {
                if (event.getCurrentItem() != null && event.getRawSlot() < event.getView().getTopInventory().getSize() && event.getCurrentItem().getType() == Material.MAP) {

                    Inventory customPunishment = Bukkit.createInventory(player, 27, "Custom Punishment");

                    ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

                    ItemMeta meta = borderPane.getItemMeta();
                    meta.setDisplayName(" ");
                    borderPane.setItemMeta(meta);

                    for (int customSlot = 0; customSlot < 27; customSlot++) {
                        int row = customSlot / 9;
                        int column = customSlot % 9;

                        if (row == 0 || row == 2 || column == 0 || column == 8) {
                            customPunishment.setItem(customSlot, borderPane);
                        }
                    }

                    ItemStack back = new ItemStack(Material.BARRIER);
                    ItemMeta cancelMeta = back.getItemMeta();
                    cancelMeta.setDisplayName("§cCancel");
                    back.setItemMeta(cancelMeta);
                    customPunishment.setItem(18, back);

                    String duration = durations.get(player);

                    if (duration == null) {
                        duration = " ";
                    }

                    String reason = reasons.get(player);

                    if (reason == null) {
                        reason = " ";
                    }

                    ItemStack customR = new ItemStack(Material.WRITABLE_BOOK);
                    ItemMeta customRmeta = customR.getItemMeta();
                    customRmeta.setDisplayName("§cSet reason");
                    customRmeta.setLore(List.of("§7Click to set a custom reason", "", "§7Current reason: §c" + reason));
                    customR.setItemMeta(customRmeta);
                    customPunishment.setItem(12, customR);

                    ItemStack customD = new ItemStack(Material.CLOCK);
                    ItemMeta customDmeta = customD.getItemMeta();
                    customDmeta.setDisplayName("§cSet duration");
                    customDmeta.setLore(List.of("§7Click to set a custom duration", "", "§7Please make sure it ends in '§cseconds§7', '§cminutes§7', '§chours§7', etc", "", "§7Current duration: §c" + duration));
                    customD.setItemMeta(customDmeta);
                    customPunishment.setItem(14, customD);

                    ItemStack confirm = new ItemStack(Material.EMERALD);
                    ItemMeta confirmMeta = confirm.getItemMeta();
                    confirmMeta.setDisplayName("§aPunish");
                    confirmMeta.setLore(List.of("§7Click to punish the target player"));
                    confirm.setItemMeta(confirmMeta);
                    customPunishment.setItem(22, confirm);
                    player.openInventory(customPunishment);
                }
            }
            if (event.getView().getTitle().contains("Custom Punishment")) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                    event.setCancelled(true);
                    event.getView().close();
                } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.WRITABLE_BOOK) {

                    event.setCancelled(true);
                    InventoryView view = player.openAnvil(null, true);

                    if (view == null) {
                        return;
                    }

                    view.setTitle("Set custom reason");

                    if (view instanceof AnvilView anvilView) {
                        if (event.getRawSlot() == 0 || event.getRawSlot() == 1) {
                            event.setCancelled(true);
                            return;
                        }

                        ItemStack paper = new ItemStack(Material.PAPER);
                        ItemMeta paperMeta = paper.getItemMeta();
                        paperMeta.setDisplayName("Enter custom reason here");
                        paper.setItemMeta(paperMeta);

                        anvilView.getTopInventory().setItem(0, paper);
                        anvilView.setRepairCost(0);
                    }
                } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.CLOCK) {

                    event.setCancelled(true);
                    InventoryView view = player.openAnvil(null, true);

                    if (view == null) {
                        return;
                    }

                    view.setTitle("Set custom duration");

                    if (view instanceof AnvilView anvilView) {
                        ItemStack paper = new ItemStack(Material.PAPER);
                        ItemMeta paperMeta = paper.getItemMeta();
                        paperMeta.setDisplayName("Enter custom duration here");
                        paper.setItemMeta(paperMeta);

                        anvilView.getTopInventory().setItem(0, paper);
                        anvilView.setRepairCost(0);
                    }
                } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.EMERALD) {
                    event.setCancelled(true);
                    String reason = reasons.get(player);

                    if (reason == null) {
                        event.getView().close();
                        return;
                    }

                    String duration = durations.get(player);

                    if (duration == null) {
                        event.getView().close();
                        return;
                    }

                    String message = (
                            "§8§l[§c§lPUNISHMENTS§8§l]\n\n" +
                                    "§cYou are §4banned§c!\n\n" +
                                    "§cReason: §4" + reason +
                                    "\n§cTime left: §4" + punishments.formatDurations(
                                    punishments.parseDuration(duration)
                            )
                    );

                    if (punishmentType.get(player).equals("mute")) {
                        punishments.mute(targetPlyr, duration, reason);
                    } else if (punishmentType.get(player).equals("ban")) {
                        punishments.ban(targetPlyr, duration, reason, message);
                    }


                    reasons.remove(player);
                    durations.remove(player);
                    punishmentType.remove(player);
                    event.getView().close();
                    return;
                } else {
                    event.setCancelled(true);
                }
            }

            if (event.getView() instanceof AnvilView anvilView && event.getView().getTitle().equals("Set custom reason") && event.getRawSlot() == 2) {
                event.setCancelled(true);

                String reason = anvilView.getRenameText();

                if (reason == null || reason.isBlank()) {
                    player.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cPlease enter a valid string!");
                    return;
                }

                event.setCancelled(true);

                player.closeInventory();

                player.getInventory().remove(Material.PAPER);

                reasons.put(player, reason);

                Inventory customPunishment = Bukkit.createInventory(player, 27, "Custom Punishment");

                ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

                ItemMeta meta = borderPane.getItemMeta();
                meta.setDisplayName(" ");
                borderPane.setItemMeta(meta);

                for (int customSlot = 0; customSlot < 27; customSlot++) {
                    int row = customSlot / 9;
                    int column = customSlot % 9;

                    if (row == 0 || row == 2 || column == 0 || column == 8) {
                        customPunishment.setItem(customSlot, borderPane);
                    }
                }

                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta cancelMeta = back.getItemMeta();
                cancelMeta.setDisplayName("§cCancel");
                back.setItemMeta(cancelMeta);
                customPunishment.setItem(18, back);

                ItemStack customR = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta customRmeta = customR.getItemMeta();
                customRmeta.setDisplayName("§cSet reason");
                customRmeta.setLore(List.of("§7Click to set a custom reason", "", "§7Current reason: §c" + reason));
                customR.setItemMeta(customRmeta);
                customPunishment.setItem(12, customR);

                String duration = durations.get(player);

                if (durations.isEmpty()) {
                    duration = "";
                }

                ItemStack customD = new ItemStack(Material.CLOCK);
                ItemMeta customDmeta = customD.getItemMeta();
                customDmeta.setDisplayName("§cSet duration");
                customDmeta.setLore(List.of("§7Click to set a custom duration", "", "§7Please make sure it ends in '§cseconds§7', '§cminutes§7', '§chours§7', etc", "", "§7Current duration: §c" + duration));
                customD.setItemMeta(customDmeta);
                customPunishment.setItem(14, customD);

                ItemStack confirm = new ItemStack(Material.EMERALD);
                ItemMeta confirmMeta = confirm.getItemMeta();
                confirmMeta.setDisplayName("§aPunish");
                confirmMeta.setLore(List.of("§7Click to punish the target player"));
                confirm.setItemMeta(confirmMeta);
                customPunishment.setItem(22, confirm);
                player.openInventory(customPunishment);
            }

            if (event.getView() instanceof AnvilView anvilView && event.getView().getTitle().equals("Set custom duration") && event.getRawSlot() == 2) {

                if (event.getRawSlot() == 0 || event.getRawSlot() == 1) {
                    event.setCancelled(true);
                    return;
                }

                if (!(event.getRawSlot() == 2)) {
                    return;
                }

                String duration = anvilView.getRenameText();

                if (duration == null) {
                    player.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] §cPlease enter a valid string!");
                    return;
                }

                if (!(punishments.parseDuration(duration) >= 0)) {
                    player.sendMessage("§8§l[§c§lPUNISHMENTS§8§l] Enter a valid duration! Example: 1 seconds, 1 minutes, 1 hours, etc.");
                    return;
                }

                player.closeInventory();

                player.getInventory().remove(Material.PAPER);

                durations.put(player, duration);

                Inventory customPunishment = Bukkit.createInventory(player, 27, "Custom Punishment");

                ItemStack borderPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);

                ItemMeta meta = borderPane.getItemMeta();
                meta.setDisplayName(" ");
                borderPane.setItemMeta(meta);

                for (int customSlot = 0; customSlot < 27; customSlot++) {
                    int row = customSlot / 9;
                    int column = customSlot % 9;

                    if (row == 0 || row == 2 || column == 0 || column == 8) {
                        customPunishment.setItem(customSlot, borderPane);
                    }
                }

                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta cancelMeta = back.getItemMeta();
                cancelMeta.setDisplayName("§cCancel");
                back.setItemMeta(cancelMeta);
                customPunishment.setItem(18, back);

                ItemStack customR = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta customRmeta = customR.getItemMeta();
                customRmeta.setDisplayName("§cSet reason");
                customRmeta.setLore(List.of("§7Click to set a custom reason", "", "§7Current reason: §c" + reasons.get(player)));
                customR.setItemMeta(customRmeta);
                customPunishment.setItem(12, customR);

                ItemStack customD = new ItemStack(Material.CLOCK);
                ItemMeta customDmeta = customD.getItemMeta();
                customDmeta.setDisplayName("§cSet duration");
                customDmeta.setLore(List.of("§7Click to set a custom duration", "", "§7Please make sure it ends in '§cseconds§7', '§cminutes§7', '§chours§7', etc", "", "§7Current duration: §c" + duration));
                customD.setItemMeta(customDmeta);
                customPunishment.setItem(14, customD);

                ItemStack confirm = new ItemStack(Material.EMERALD);
                ItemMeta confirmMeta = confirm.getItemMeta();
                confirmMeta.setDisplayName("§aPunish");
                confirmMeta.setLore(List.of("§7Click to punish the target player"));
                confirm.setItemMeta(confirmMeta);
                customPunishment.setItem(22, confirm);
                player.openInventory(customPunishment);
            }

        }

    }
}
