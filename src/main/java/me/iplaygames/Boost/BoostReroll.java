package me.iplaygames.Boost;

import org.bukkit.*;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Random;
import org.bukkit.event.EventHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;

public class BoostReroll implements Listener {

    private BoostPlugin plugin;
    private NamespacedKey rerollBookKey;

    public BoostReroll(BoostPlugin plugin) {
        this.plugin = plugin;
        this.rerollBookKey = new NamespacedKey(plugin, "reroll_book");

        // Create the reroll book item
        ItemStack rerollBook = new ItemStack(Material.KNOWLEDGE_BOOK);
        KnowledgeBookMeta meta = (KnowledgeBookMeta) rerollBook.getItemMeta();

        // Set the book's name
        meta.setDisplayName("ʀᴇʀᴏʟʟ ʙᴏᴏᴋ");

        // Set the book's lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Re-roll your Boost to any random number between 1-5");
        meta.setLore(lore);

        // Apply the meta to the book
        rerollBook.setItemMeta(meta);

        // Create the crafting recipe
        ShapedRecipe rerollRecipe = new ShapedRecipe(rerollBookKey, rerollBook);
        rerollRecipe.shape("ESE", "SAS", "ESE");
        rerollRecipe.setIngredient('E', Material.NETHERITE_INGOT);
        rerollRecipe.setIngredient('S', Material.DIAMOND_BLOCK);
        rerollRecipe.setIngredient('A', Material.BOOK);

        // Add the recipe to the server
        Bukkit.addRecipe(rerollRecipe);
    }


    // Create a HashMap to store the cooldown times
    private HashMap<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is using a reroll book
        if (item != null && item.getType() == Material.KNOWLEDGE_BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName().equals("ʀᴇʀᴏʟʟ ʙᴏᴏᴋ")) {
                // Get the player's current killstreak
                int currentKillStreak = plugin.getKillStreaks().getOrDefault(player.getUniqueId(), 0);

                // If the player's killstreak is 4 or 5, send a message and return
                if (currentKillStreak >= 4) {
                    player.sendMessage("Your killstreak is already at " + currentKillStreak + ". You cannot use a reroll book.");
                    event.setCancelled(true); // Cancel the event to prevent the book from being consumed
                    return;
                }

                // Check if the player is in cooldown
                if (cooldowns.containsKey(player.getUniqueId())) {
                    // If the player is still in cooldown, send a message and return
                    player.sendMessage("You are currently in cooldown. Please wait before using another reroll book.");
                    event.setCancelled(true); // Cancel the event to prevent the book from being consumed
                    return;
                }

                // If the player is not in cooldown, add them to the cooldown list
                // The cooldown lasts for 5 seconds (100 ticks)
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 5000);

                // Remove the reroll book from the player's inventory
                item.setAmount(item.getAmount() - 1);

// Create a new Random object
                Random rand = new Random();

// Generate a random killstreak as soon as the reroll book is used
// Adjust the range to be from 1 to 3
                int killStreak = rand.nextInt(3) + 1;

// Show a rapidly changing title screen
                new BukkitRunnable() {
                    int count = 1;
                    int delay = 2;  // Initial delay in ticks
                    @Override
                    public void run() {
                        if (delay < 60) {  // Maximum delay in ticks
                            // Color code the titles
                            String colorCode;
                            switch (count) {
                                case 1:
                                    colorCode = "§a";  // Green
                                    break;
                                case 2:
                                    colorCode = "§d";  // Light purple
                                    break;
                                default:
                                    colorCode = "§9";  // Blue
                                    break;
                            }
                            player.sendTitle(colorCode + count, "", 0, 10, 0);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);  // Lower volume
                            count = count % 3 + 1;
                            delay++;
                        } else {
                            // Play a louder and more high-pitched pling sound
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);

                            // Display the final killstreak
                            player.sendTitle(String.valueOf(killStreak), "", 0, 10, 0);
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

                            // Assign the final killstreak to the player
                            plugin.assignKillStreak(player, killStreak);
                            player.sendMessage("Your boost has been added by " + killStreak + "!");

                            // Apply the effects of the new killstreak
                            plugin.applyKillStreakEffects(player, killStreak);

                            // Save the player's killstreaks
                            plugin.saveKillStreaks();

                            // Cancel the runnable after assigning the killstreak
                            cancel();

                            // Remove the player from the cooldown list
                            cooldowns.remove(player.getUniqueId());
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);  // 0 initial delay, 2 ticks = 0.1 second delay between each run
            }
        }
    }
}


























