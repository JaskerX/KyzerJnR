package de.jaskerx.kyzer.jnr;

import de.jaskerx.kyzer.jnr.db.Cache;
import de.jaskerx.kyzer.jnr.db.data.HighscoreData;
import de.jaskerx.kyzer.jnr.time.TimesManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaskerx.kyzer.jnr.commands.JnRCommand;
import de.jaskerx.kyzer.jnr.db.DbManager;
import de.jaskerx.kyzer.jnr.listeners.PlayerInteractListener;

import java.util.List;

/**
 * @author JaskerX
 * @version Spigot 1.12.2 +
 */
public class KyzerJnR extends JavaPlugin {

	private static KyzerJnR instance;
	private DbManager db;
	private Cache cache;
	private TimesManager timesManager;
	
	@Override
	public void onEnable() {
		instance = this;
		db = new DbManager(this);
		db.init();
		cache = new Cache(this, db);
		cache.loadData();
		timesManager = new TimesManager(this, cache);

		getCommand("jnr").setExecutor(new JnRCommand(this));

		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new PlayerInteractListener(this, timesManager, cache), this);
	}
	
	@Override
	public void onDisable() {
		db.close();
	}
	
	
	
	/**
	 * Sends a formatted message
	 * @param sender The sender the message should be sent to
	 * @param message The message that should be sent
	 * @param error If the message should be displayed as an error message
	 */
	public void sendMessage(CommandSender sender, String message, boolean error) {
		String PREFIX = "§8[§9J&R§8] §r";
		if(error) {
			sender.sendMessage(PREFIX + "§4" + message);
		} else {
			sender.sendMessage(PREFIX + "§7" + message);
		}
	}
	
	
	
	/**
	 * refreshs the displayed Highscores by replacing the ArmorStands
	 */
	public void refreshHighscore() {
		if(cache.getBlockHighscoreDisplay() == null || cache.getBlockStart() == null || cache.getBlockEnd() == null) return;

		cache.getTopTen((List<HighscoreData> times) -> Bukkit.getScheduler().runTask(this, () -> {
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
			asTitle.setCustomName("§6Top 10 Highscores:");

			final int[] i = new int[]{1};
			times.forEach(data -> {
				loc.setY(loc.getY() - 0.25);

				ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
				armorStand.setGravity(false);
				armorStand.setCanPickupItems(false);
				armorStand.setCustomNameVisible(true);
				armorStand.setVisible(false);
				String timeFormatted = String.valueOf(data.getTime() * 0.001);
				armorStand.setCustomName("§a" + i[0] + ". §b" + data.getPlayerName() + ": " + timeFormatted.substring(0, Math.min(timeFormatted.length(), 5)) + " s");
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

	public static KyzerJnR getInstance() {
		return instance;
	}

}
