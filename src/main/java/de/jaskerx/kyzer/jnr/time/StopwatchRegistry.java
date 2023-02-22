package de.jaskerx.kyzer.jnr.time;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.jaskerx.kyzer.jnr.db.Cache;
import de.jaskerx.kyzer.jnr.utils.Utils;
import org.bukkit.entity.Player;

public class StopwatchRegistry {

	private static Utils utils;
	private static final Map<UUID, Stopwatch> times = new HashMap<>();
	private static Cache cache;

	public static void init(Utils utils, Cache cache) {
		StopwatchRegistry.utils = utils;
		StopwatchRegistry.cache = cache;
	}
	
	/**
	 * Puts a started Stopwatch into the HashMap
	 * @param uuid The players UUID
	 */
	public static void start(UUID uuid) {
		times.put(uuid, new Stopwatch().start());
	}
	
	/**
	 * Stops the associated Stopwatch
	 * @param player The player
	 * @return The time measured by the associated Stopwatch or -1 if there is no Stopwatch associated with the given player
	 */
	public static long stop(Player player) {
		UUID uuid = player.getUniqueId();
		if(times.containsKey(uuid)) {
			long time = times.get(uuid).stop();
			times.remove(uuid);
			cache.setHighscore(player, time, rows -> {
				if (rows == 1 || rows == 2) {
					utils.refreshHighscore();
				} else {
					utils.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
				}
			});
			return time;
		}
		return -1;
	}
	
}
