package de.jaskerx.kyzer.jnr.main;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaskerx.kyzer.jnr.commands.JnRCommand;
import de.jaskerx.kyzer.jnr.db.DbManager;
import de.jaskerx.kyzer.jnr.listeners.PlayerDeathListener;
import de.jaskerx.kyzer.jnr.listeners.PlayerInteractListener;
import de.jaskerx.kyzer.jnr.listeners.PlayerJoinListener;

/**
 * @author JaskerX
 * @version Spigot 1.12.2
 */
public class Main extends JavaPlugin {

	//TODO: final test + reset
	
	public static Main instance;
	public static ActionBlock blockStart;
	public static ActionBlock blockEnd;
	public static ActionBlock blockHigscoreDisplay;
	
	@Override
	public void onEnable() {
		instance = this;
		
		getCommand("jnr").setExecutor(new JnRCommand());
		
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new PlayerInteractListener(), this);
		//pluginManager.registerEvents(new PlayerDeathListener(), this);
		//pluginManager.registerEvents(new PlayerJoinListener(), this);
		
		DbManager.initDb();
	}
	
	@Override
	public void onDisable() {
		DbManager.close();
	}
	
	
	
	/**
	 * Sends a formatted message
	 * @param sender - The sender the message should be sent to
	 * @param message - The message that should be sent
	 * @param error - If the message should be displayed as an error message
	 */
	public static void sendMessage(CommandSender sender, String message, boolean error) {
		String PREFIX = "§8[§9J&R§8] §r";
		if(error) {
			sender.sendMessage(PREFIX + "§4" + message);
		} else {
			sender.sendMessage(PREFIX + "§7" + message);
		}
	}
	
	
	
	public static void refreshHighscore() {
		if(blockHigscoreDisplay == null || blockStart == null || blockEnd == null) return;
		
		HashMap<String, Long> times = DbManager.getTopTen();
		removeHighscore();
		
		Location loc = blockHigscoreDisplay.getBlock().getLocation();
		loc.setY(loc.getY() + 2.0);
		loc.setX(loc.getX() + 0.5);
		loc.setZ(loc.getZ() + 0.5);
		ArmorStand asTitle = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		asTitle.setGravity(false);
		asTitle.setCanPickupItems(false);
		asTitle.setCustomNameVisible(true);
		asTitle.setVisible(false);
		asTitle.setCustomName("Top 10 Highscores:");

		final int[] i = new int[] {1};
		times.forEach((p, t) -> {
			loc.setY(loc.getY() - 0.25);
			
			ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
			as.setGravity(false);
			as.setCanPickupItems(false);
			as.setCustomNameVisible(true);
			as.setVisible(false);
			as.setCustomName(i[0] + ". " + p + ": " + t * 0.000000001 + " s");
			i[0]++;
		});
	}
	
	/**
	 * Removes the displayed highscores by killing the ArmorStands at the position of highscore_display
	 */
	public static void removeHighscore() {
		if(blockHigscoreDisplay == null) return;
		
		Location loc = blockHigscoreDisplay.getBlock().getLocation();
		loc.setY(loc.getY() + 2.0);
		loc.setX(loc.getX() + 0.5);
		loc.setZ(loc.getZ() + 0.5);
		
		loc.getWorld().getNearbyEntities(loc, 0, 3, 0).forEach(e -> {
			if(e instanceof ArmorStand) {
				e.remove();
			}
		});
	}
	
}
