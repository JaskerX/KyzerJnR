package de.jaskerx.kyzer.jnr.utils;

import de.jaskerx.kyzer.jnr.KyzerJnR;
import de.jaskerx.kyzer.jnr.db.Cache;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class Utils {

    private final KyzerJnR plugin;
    private Cache cache;

    public Utils(KyzerJnR plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a formatted message
     * @param sender The sender the message should be sent to
     * @param message The message that should be sent
     * @param error If the message should be displayed as an error message
     */
    public void sendMessage(CommandSender sender, String message, boolean error) {
        String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "J&R" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
        if(error) {
            sender.sendMessage(PREFIX + ChatColor.DARK_RED + message);
        } else {
            sender.sendMessage(PREFIX + ChatColor.GRAY + message);
        }
    }

    /**
     * refreshs the displayed Highscores by replacing the ArmorStands
     */
    public void refreshHighscore() {
        if(cache.getBlockHighscoreDisplay() == null || cache.getBlockStart() == null || cache.getBlockEnd() == null) return;

        cache.getTopTen().thenAccept(times -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            removeHighscore();

            Location loc = cache.getBlockHighscoreDisplay().getBlock().getLocation();
            loc.setY(loc.getY() + 2.0);
            loc.setX(loc.getX() + 0.5);
            loc.setZ(loc.getZ() + 0.5);

            ArmorStand asTitle = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            asTitle.setGravity(false);
            asTitle.setCanPickupItems(false);
            asTitle.setCustomNameVisible(true);
            asTitle.setVisible(false);
            asTitle.setCustomName(ChatColor.GOLD + "Top 10 Highscores:");

            final int[] i = new int[]{1};
            times.forEach(data -> {
                loc.setY(loc.getY() - 0.25);

                ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                armorStand.setGravity(false);
                armorStand.setCanPickupItems(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setVisible(false);
                String timeFormatted = String.valueOf(data.getTime() * 0.001);
                armorStand.setCustomName(ChatColor.GREEN + "" + i[0] + ". " + ChatColor.AQUA + data.getPlayerName() + ": " + timeFormatted.substring(0, Math.min(timeFormatted.length(), 5)) + " s");
                i[0]++;
            });
        }));
    }

    /**
     * Removes the displayed highscores by killing the ArmorStands at the position of the display block
     */
    public void removeHighscore() {
        if(cache.getBlockHighscoreDisplay() == null) return;

        Location loc = cache.getBlockHighscoreDisplay().getBlock().getLocation();
        loc.setY(loc.getY() + 2.0);
        loc.setX(loc.getX() + 0.5);
        loc.setZ(loc.getZ() + 0.5);

        loc.getWorld().getNearbyEntities(loc, 0, 3, 0).forEach(entity -> {
            if(entity instanceof ArmorStand) {
                entity.remove();
            }
        });
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

}
