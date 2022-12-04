package de.jaskerx.kyzer.jnr.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.jaskerx.kyzer.jnr.db.DbManager;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		Player p = event.getPlayer();
		int deaths = DbManager.getDeaths(p);
		p.setPlayerListName("§8[§6" + deaths + "§8] §r" + p.getPlayerListName());
	}
	
}
