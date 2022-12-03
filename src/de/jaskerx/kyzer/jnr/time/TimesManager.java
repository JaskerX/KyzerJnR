package de.jaskerx.kyzer.jnr.time;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import de.jaskerx.kyzer.jnr.db.DbManager;

public class TimesManager {

	private static HashMap<UUID, Stopwatch> times = new HashMap<>();
	
	/**
	 * Puts a started Stopwatch into the HashMap
	 * @param uuid - The players UUID
	 */
	public static void start(UUID uuid) {
		times.put(uuid, new Stopwatch().start());
	}
	
	/**
	 * Stops the associated Stopwatch
	 * @param Player - The player
	 * @return long - The time measured by the associated Stopwatch or -1 if there is no Stopwatch associated with the given player
	 */
	public static long stop(Player player) {
		UUID uuid = player.getUniqueId();
		if(times.containsKey(uuid)) {
			long time = times.get(uuid).stop();
			times.remove(uuid);
			DbManager.setHighscore(player, time);
			return time;
		}
		return -1;
	}
	
}
