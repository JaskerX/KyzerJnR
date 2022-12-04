package de.jaskerx.kyzer.jnr.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import de.jaskerx.kyzer.jnr.db.DbManager;

public class PlayerDeathListener implements Listener {

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		Player p = event.getEntity();
		int deaths = DbManager.increaseDeath(event.getEntity());
		int nameIndex = p.getPlayerListName().split(" ")[0].length() + 1;
		p.setPlayerListName("§8[§6" + deaths + "§8] §r" + p.getPlayerListName().substring(nameIndex));
	}
	
}
