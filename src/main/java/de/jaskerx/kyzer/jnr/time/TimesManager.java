package de.jaskerx.kyzer.jnr.time;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.jaskerx.kyzer.jnr.KyzerJnR;
import de.jaskerx.kyzer.jnr.db.Cache;
import org.bukkit.entity.Player;

public class TimesManager {

	private final KyzerJnR plugin;
	private final Map<UUID, Stopwatch> times = new HashMap<>();
	private final Cache cache;

	public TimesManager(KyzerJnR plugin, Cache cache) {
		this.plugin = plugin;
		this.cache = cache;
	}
	
	/**
	 * Puts a started Stopwatch into the HashMap
	 * @param uuid The players UUID
	 */
	public void start(UUID uuid) {
		times.put(uuid, new Stopwatch().start());
	}
	
	/**
	 * Stops the associated Stopwatch
	 * @param player The player
	 * @return The time measured by the associated Stopwatch or -1 if there is no Stopwatch associated with the given player
	 */
	public long stop(Player player) {
		UUID uuid = player.getUniqueId();
		if(times.containsKey(uuid)) {
			long time = times.get(uuid).stop();
			times.remove(uuid);
			cache.setHighscore(player, time, rows -> {
				if (rows == 1 || rows == 2) {
					plugin.refreshHighscore();
				} else {
					plugin.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
				}
			});
			return time;
		}
		return -1;
	}
	
}
