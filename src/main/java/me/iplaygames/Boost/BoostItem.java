package me.iplaygames.Boost;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Material;
import java.util.UUID;
import org.bukkit.ChatColor;


public class BoostItem implements CommandExecutor {

    private BoostPlugin plugin;

    public BoostItem(BoostPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();

            // Check if the player has at least 1 killstreak
            int killStreak = plugin.getKillStreaks().getOrDefault(playerId, 0);
            if (killStreak > 0) {
                // Decrease the player's killstreak by 1
                killStreak--;
                plugin.getKillStreaks().put(playerId, killStreak);

                // Reset the player's killstreak effects
                plugin.applyKillStreakEffects(player, killStreak);

                // Save the updated killstreaks to file
                plugin.saveKillStreaks();

                // Give the player an item
                ItemStack item = new ItemStack(Material.AMETHYST_SHARD); // Changed to AMETHYST_SHARD
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "ʙᴏᴏѕᴛ ꜰʀᴀɢᴍᴇɴᴛ");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "Use this item to gain back 1 Boost!");
                meta.setLore(lore);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);

                player.sendMessage(ChatColor.GREEN + "You have converted 1 Boost into an item!");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have any Boosts to convert into items.");
            }

            return true;
        }

        return false;
    }
}

