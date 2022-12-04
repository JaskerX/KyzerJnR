package de.jaskerx.kyzer.jnr.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import de.jaskerx.kyzer.jnr.db.DbManager;
import de.jaskerx.kyzer.jnr.main.Main;
import de.jaskerx.kyzer.jnr.time.TimesManager;


public class PlayerInteractListener implements Listener {

	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		
		Player p = event.getPlayer();
		ItemStack itemHand = p.getInventory().getItemInMainHand();
		
		// stick (JnR tool)
		if(event.hasBlock() && itemHand.hasItemMeta() && itemHand.getItemMeta().hasDisplayName() && itemHand.getItemMeta().getDisplayName().startsWith("JnR tool")) {
			
			event.setCancelled(true);
			
			if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
				
				if(!p.hasPermission("jnr.manage")) {
					Main.sendMessage(p, "Du bist nicht berechtigt, diese Aktion auszuführen!", true);
					return;
				}
				
				if(itemHand.getItemMeta().getDisplayName().equals("JnR tool parkour")) {
					// start block
					if(!event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
						Main.sendMessage(p, "Bitte wähle eine Steindruckplatte aus!", false);
						return;
					}
					DbManager.setCoords("start", p.getWorld(), event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), p);
					
				} else if(itemHand.getItemMeta().getDisplayName().equals("JnR tool display")) {
					// highscore display block
					DbManager.setCoords("highscore_display", p.getWorld(), event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), p);
					Main.refreshHighscore();
				}
				
			} else if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
				
				if(!p.hasPermission("jnr.manage")) {
					Main.sendMessage(p, "Du bist nicht berechtigt, diese Aktion auszuführen!", true);
					return;
				}
				
				if(itemHand.getItemMeta().getDisplayName().equals("JnR tool parkour")) {
					// end block
					if(!event.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
						Main.sendMessage(p, "Bitte wähle eine Steindruckplatte aus!", false);
						return;
					}
					DbManager.setCoords("end", p.getWorld(), event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), p);
				}
			}
		}
		
		// pressure plate
		if(event.getClickedBlock() != null && event.getClickedBlock().hasMetadata("jnr") && event.getAction().equals(Action.PHYSICAL)) {
			String tag = event.getClickedBlock().getMetadata("jnr").get(0).asString();

			if(tag.equals("start")) {
				TimesManager.start(p.getUniqueId());
			} else if(tag.equals("end")) {
				long time = TimesManager.stop(p);
				if(time != -1) {
					String timeInSeconds = String.valueOf((time / 100000) * 0.0001);
					Main.sendMessage(p, "Du hast " + timeInSeconds.substring(0, (timeInSeconds.split("\\.")[1].length() >= 3) ? (timeInSeconds.split("\\.")[0].length() + 1 + 3) : (timeInSeconds.split("\\.")[0].length() + timeInSeconds.split("\\.")[1].length())) + " Sekunden gebraucht.", false);
					Main.refreshHighscore();
				}
			}
		}
	}
	
}
