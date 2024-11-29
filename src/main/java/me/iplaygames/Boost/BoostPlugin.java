package me.iplaygames.Boost;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BoostPlugin extends JavaPlugin implements Listener {
    private final Map<String, Date> lastKillTimes = new HashMap<>();
    private final HashMap<UUID, Integer> killStreaks = new HashMap<>();
    private final HashMap<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final File dataFile = new File("killstreaks.txt");


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BoostReroll(this), this);
        loadKillStreaks();

        // Register BoostItem as a command executor
        this.getCommand("withdrawBoost").setExecutor(new BoostItem(this));
        this.getCommand("Boost").setExecutor(new KillStreakCommand(this));
        this.getCommand("BoostChange").setExecutor(new BoostChangeCommand(this));

        // Create a new Bukkit Runnable to refresh the scoreboards every second
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerScoreboard(player);
                }
            }
        }.runTaskTimer(this, 0L, 20L);  // 0 initial delay, 20 ticks = 1 second delay between each run
    }


    public class KillStreakCommand implements CommandExecutor {

        private BoostPlugin plugin;

        public KillStreakCommand(BoostPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /Boost <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            int killStreak = plugin.getKillStreak(target);
            sender.sendMessage(ChatColor.GREEN + target.getName() + "'s Boost is " + killStreak);

            return true;
        }
    }

    public int getKillStreak(Player player) {
        UUID playerId = player.getUniqueId();
        return killStreaks.getOrDefault(playerId, 0);
    }

    public void setKillStreak(Player player, int killStreak) {
        UUID playerId = player.getUniqueId();
        killStreaks.put(playerId, killStreak);
    }

    public class BoostChangeCommand implements CommandExecutor {

        private BoostPlugin plugin;

        public BoostChangeCommand(BoostPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "Usage: /BoostChange <player> <killstreak>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            int killStreak;
            try {
                killStreak = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid Boost. Please enter a number.");
                return true;
            }

            setKillStreak(target, killStreak);
            player.sendMessage(ChatColor.GREEN + target.getName() + "'s Boost has been set to " + killStreak);

            saveKillStreaks();

            return true;
        }
    }



    public ItemStack createBoostFragment() {
        ItemStack boostFragment = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = boostFragment.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "ʙᴏᴏѕᴛ ꜰʀᴀɢᴍᴇɴᴛ");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "Use this item to gain back 1 Boost!");
        meta.setLore(lore);
        boostFragment.setItemMeta(meta);
        return boostFragment;
    }


    public void assignKillStreak(Player player, int additionalKillStreak) {
        UUID playerId = player.getUniqueId();

        // Get the player's current killstreak
        int currentKillStreak = killStreaks.getOrDefault(playerId, 0);

        // Add the additional killstreak to the current killstreak
        int newKillStreak = currentKillStreak + additionalKillStreak;

        // If the new killstreak is more than 5, cap it at 5
        if (newKillStreak > 5) {
            newKillStreak = 5;
        }

        // Set the player's killstreak to the new killstreak
        killStreaks.put(playerId, newKillStreak);

        // Apply the effects of the new killstreak
        applyKillStreakEffects(player, newKillStreak);
    }





    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is right-clicking with the specific item in their hand
        if (item != null && item.isSimilar(createBoostFragment()) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            UUID playerId = player.getUniqueId();

            // Get the player's current killstreak
            int killStreak = getKillStreaks().getOrDefault(playerId, 0);

            // Check if the player's killstreak is less than 5
            if (killStreak < 5) {
                // Increase the player's killstreak by 1
                getKillStreaks().put(playerId, killStreak + 1);

                // Apply the player's killstreak effects
                applyKillStreakEffects(player, killStreak + 1);

                // Save the updated killstreaks to file
                saveKillStreaks();

                // Remove the item from the player's inventory
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().removeItem(item);
                }

                player.sendMessage(ChatColor.GREEN + "You have used an item to gain 1 Boost! Your current Boost is: " + (killStreak + 1));
            } else {
                player.sendMessage(ChatColor.RED + "Your Boost is already at the maximum of 5. You cannot use this item.");
            }
        }
    }




    public Map<UUID, Integer> getKillStreaks() {
        return killStreaks;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Check if the killer and victim are the same player
        if (killer != null && killer.equals(victim)) {
            victim.sendMessage(ChatColor.RED + "You cannot earn boosts by killing yourself.");
            return;
        }

        // If the victim was killed by another player
        if (killer != null) {
            UUID victimId = victim.getUniqueId();
            UUID killerId = killer.getUniqueId();

            // Get the current time
            long now = System.currentTimeMillis();

            // Get the time of the last kill of the victim by the killer
            String killerVictimPair = killerId.toString() + ":" + victimId.toString();
            long lastKillTime = lastKillTimes.getOrDefault(killerVictimPair, new Date(0L)).getTime();

            // If the killer has killed the victim in the last 5 minutes, return
            if (now - lastKillTime < 5 * 60 * 1000) {
                killer.sendMessage(ChatColor.RED + "You are on a cooldown! You won't get a killstreak for killing the same player within 5 minutes.");
                return;
            }

            // Update the time of the last kill
            lastKillTimes.put(killerVictimPair, new Date(now));

            // Decrease the victim's killstreak by 1 if killed by another player
            int victimKillStreak = killStreaks.getOrDefault(victimId, 0);
            if (victimKillStreak > 0) {
                killStreaks.put(victimId, victimKillStreak - 1);
                victim.sendMessage(ChatColor.RED + "Your Boost has been decreased by 1.");
            }
            applyKillStreakEffects(victim, victimKillStreak - 1);
            int limitedVictimKillStreak = Math.min(victimKillStreak, 5);
            ChatColor victimKillStreakColor = getColorForKillStreak(limitedVictimKillStreak);
            String coloredVictimKillStreak = victimKillStreakColor + String.valueOf(limitedVictimKillStreak);
            victim.setPlayerListName(victim.getName() + " " + victimKillStreakColor + "[" + coloredVictimKillStreak + "]" + ChatColor.RESET);
            updatePlayerScoreboard(victim);

            // Increase the killer's killstreak by 1, but not more than 5
            int killerKillStreak = killStreaks.getOrDefault(killerId, 0);
            if (killerKillStreak < 5) {
                killerKillStreak++;
                killer.sendMessage(ChatColor.GREEN + "You have received +1 Boost! Your current Boost is: " + killerKillStreak);
            } else {
                killer.sendMessage(ChatColor.GREEN + "You have hit the max Boost of 5!");
            }
            killStreaks.put(killerId, killerKillStreak);
            applyKillStreakEffects(killer, killerKillStreak);
            updatePlayerScoreboard(killer);
        } else {
            // Handle non-player caused deaths (e.g., environment, mobs)
            UUID victimId = victim.getUniqueId();
            int victimKillStreak = killStreaks.getOrDefault(victimId, 0);
            if (victimKillStreak > 0) {
                killStreaks.put(victimId, victimKillStreak - 1);
                victim.sendMessage(ChatColor.RED + "Your Boost has been decreased due to environmental damage or mob attack.");
            }
            applyKillStreakEffects(victim, victimKillStreak - 1);
            updatePlayerScoreboard(victim);
        }

        // Save the updated killstreaks to file
        saveKillStreaks();
    }



    public void applyKillStreakEffects(Player player, int killStreak) {
        // Clear existing potion effects related to killstreaks
        player.removePotionEffect(PotionEffectType.LUCK);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

        // Apply new effects based on the current killstreak
        if (killStreak >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        }
        if (killStreak >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0));
        }
        if (killStreak >= 5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
        }

        // Update attributes if needed
        AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            attackSpeedAttribute.setBaseValue(killStreak >= 2 ? 4.5 : 4.0);
        }

        AttributeInstance knockbackResistanceAttribute = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (knockbackResistanceAttribute != null) {
            knockbackResistanceAttribute.setBaseValue(killStreak >= 3 ? 0.5 : 0.0);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (killStreaks.containsKey(playerId)) {
            int killStreak = killStreaks.get(playerId);
            int limitedKillStreak = Math.min(killStreak, 5);
            ChatColor killStreakColor = getColorForKillStreak(limitedKillStreak);
            String coloredKillStreak = killStreakColor + String.valueOf(limitedKillStreak);

            player.setPlayerListName(player.getName() + " " + killStreakColor + "[" + coloredKillStreak + "]" + ChatColor.RESET);
            updatePlayerScoreboard(player); // Make sure this method is correctly handling PlaceholderAPI replacements

            // Apply special effects based on existing killstreak
            applyKillStreakEffects(player, killStreak); // This method should handle the application of potion effects and attributes
        }
    }



    private void updatePlayerScoreboard(Player player) {

        if (player != null) {
            Scoreboard scoreboard = playerScoreboards.computeIfAbsent(player.getUniqueId(), p -> Bukkit.getScoreboardManager().getNewScoreboard());
            Objective objective = scoreboard.getObjective("killstreak");

            if (objective == null) {
                objective = scoreboard.registerNewObjective("killstreak", "dummy", ChatColor.GREEN + "Boost");
                objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            }

            // Update the score for all players
            for (Player p : Bukkit.getOnlinePlayers()) {
                UUID playerId = p.getUniqueId();
                int playerStreak = killStreaks.getOrDefault(playerId, 0);
                int limitedPlayerStreak = Math.min(playerStreak, 5);

                Score score = objective.getScore(p.getName());
                score.setScore(limitedPlayerStreak); // Limit the displayed killstreak to 5

                // Set the player list name with only the brackets and number colored
                ChatColor streakColor = getColorForKillStreak(limitedPlayerStreak);
                p.setPlayerListName(p.getName() + " " + ChatColor.WHITE + "[" + streakColor + limitedPlayerStreak + ChatColor.WHITE + "]");
            }

            player.setScoreboard(scoreboard);
        } else {
            getLogger().info("Player object is null.");
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (killStreaks.containsKey(playerId)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    int killStreak = killStreaks.get(playerId);

                    // Apply special effects based on existing killstreak
                    if (killStreak >= 1) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0)); // Luck 1 for infinite time
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, Integer.MAX_VALUE, 0)); // Hero of the Village 1 for infinite time
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)); // Speed 1 for infinite time
                    }

                    // Attack speed attribute
                    AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                    if (attackSpeedAttribute != null) {
                        if (killStreak >= 2) {
                            attackSpeedAttribute.setBaseValue(4.5);
                        } else {
                            attackSpeedAttribute.setBaseValue(4.0); // Default attack speed for players
                        }
                    }

                    // Knockback resistance attribute
                    AttributeInstance knockbackResistanceAttribute = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                    if (knockbackResistanceAttribute != null) {
                        if (killStreak >= 3) {
                            knockbackResistanceAttribute.setBaseValue(0.5);
                        } else {
                            knockbackResistanceAttribute.setBaseValue(0.0); // Default knockback resistance for players
                        }
                    }

                    if (killStreak >= 4) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 0)); // Health Boost 1 for infinite time
                    }
                    if (killStreak >= 5) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0)); // Strength 1 for infinite time
                    }
                }
            }.runTaskLater(this, 60);  // Delay of 60 ticks (3 seconds)
        }
    }



    private ChatColor getColorForKillStreak(int killStreak) {
        switch (killStreak) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.LIGHT_PURPLE;
            case 3:
                return ChatColor.BLUE;
            case 4:
                return ChatColor.GOLD;
            case 5:
                return ChatColor.DARK_RED;
            default:
                return ChatColor.WHITE; // Default to white if the killstreak is outside the defined range
        }
    }

    public void saveKillStreaks() {
        StringBuilder data = new StringBuilder();
        for (UUID playerId : killStreaks.keySet()) {
            data.append(playerId.toString()).append(":").append(killStreaks.get(playerId)).append("\n");
        }

        try {
            Files.write(Paths.get(dataFile.toURI()), data.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadKillStreaks() {
        if (!dataFile.exists()) return;

        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile.toURI()));
            for (String line : lines) {
                String[] parts = line.split(":");
                UUID playerId = UUID.fromString(parts[0]);
                int killStreak = Integer.parseInt(parts[1]);

                killStreaks.put(playerId, killStreak);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




