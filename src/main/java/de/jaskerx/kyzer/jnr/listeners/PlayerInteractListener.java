package de.jaskerx.kyzer.jnr.listeners;

import de.jaskerx.kyzer.jnr.ActionBlock;
import de.jaskerx.kyzer.jnr.db.Cache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import de.jaskerx.kyzer.jnr.KyzerJnR;
import de.jaskerx.kyzer.jnr.time.TimesManager;
import org.checkerframework.checker.nullness.qual.NonNull;


public class PlayerInteractListener implements Listener {

	private final KyzerJnR plugin;
	private final TimesManager timesManager;
	private final Cache cache;

	public PlayerInteractListener(KyzerJnR plugin, TimesManager timesManager, Cache cache) {
		this.plugin = plugin;
		this.timesManager = timesManager;
		this.cache = cache;
	}

	@EventHandler
	public void onPlayerClick(@NonNull PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		ItemStack itemHand = player.getInventory().getItemInMainHand();
		
		// stick (JnR tool)
		if(event.hasBlock() && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName() && itemHand.getItemMeta().getDisplayName().startsWith("JnR tool")) {
			
			event.setCancelled(true);
			
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				
				if(!player.hasPermission("jnr.manage")) {
					plugin.sendMessage(player, "Du bist nicht berechtigt, diese Aktion auszuführen!", true);
					return;
				}
				
				if(itemHand.getItemMeta().getDisplayName().equals("JnR tool parkour")) {
					// start block
					if(!event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
						plugin.sendMessage(player, "Bitte wähle eine Steindruckplatte aus!", false);
						return;
					}
					cache.setBlockStart(event.getClickedBlock(), player, rows -> {
						if(rows == 1 || rows == 2) {
							plugin.sendMessage(player, "Der Start wurde erfolgreich festgelegt.", false);
							plugin.refreshHighscore();
						} else {
							plugin.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
						}
					});

				} else if(itemHand.getItemMeta().getDisplayName().equals("JnR tool display")) {
					// highscore display block
					plugin.removeHighscore();
					cache.setBlockHighscoreDisplay(event.getClickedBlock(), player, rows -> {
						if(rows == 1 || rows == 2) {
							plugin.sendMessage(player, "Das Highscore-Display wurde erfolgreich festgelegt.", false);
							plugin.refreshHighscore();
						} else {
							plugin.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
						}
					});
					plugin.refreshHighscore();
				}
				
			} else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
				
				if(!player.hasPermission("jnr.manage")) {
					plugin.sendMessage(player, "Du bist nicht berechtigt, diese Aktion auszuführen!", true);
					return;
				}
				
				if(itemHand.getItemMeta().getDisplayName().equals("JnR tool parkour")) {
					// end block
					if(!event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
						plugin.sendMessage(player, "Bitte wähle eine Steindruckplatte aus!", false);
						return;
					}
					cache.setBlockEnd(event.getClickedBlock(), player, rows -> {
						if(rows == 1 || rows == 2) {
							plugin.sendMessage(player, "Das Ende wurde erfolgreich festgelegt.", false);
							plugin.refreshHighscore();
						} else {
							plugin.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
						}
					});
				}
			}
		}
		
		// pressure plate
		if(event.getClickedBlock() != null && event.getClickedBlock().hasMetadata("jnr") && event.getAction().equals(Action.PHYSICAL)) {
			String tag = event.getClickedBlock().getMetadata("jnr").get(0).asString();

			if(tag.equals("start")) {
				timesManager.start(player.getUniqueId());
			} else if(tag.equals("end")) {
				long time = timesManager.stop(player);
				if(time != -1) {
					String timeInSeconds = String.valueOf(time * 0.001);
					plugin.sendMessage(player, "Du hast " + timeInSeconds.substring(0, (timeInSeconds.split("\\.")[1].length() >= 3) ? (timeInSeconds.split("\\.")[0].length() + 1 + 3) : (timeInSeconds.split("\\.")[0].length() + timeInSeconds.split("\\.")[1].length())) + " Sekunden gebraucht.", false);
					plugin.refreshHighscore();
				}
			}
		}
	}
	
}
