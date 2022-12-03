package de.jaskerx.kyzer.jnr.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JnRCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {

		if(!(sender instanceof Player)) {
			sender.sendMessage("Diesen Command können nur Spieler ausführen!");
			return true;
		}
		if(!sender.hasPermission("jnr.manage")) {
			sender.sendMessage("Du bist nicht berechtigt, diesen Command auszuführen!");
			return true;
		}
		if(args.length < 1) {
			return false;
		}
		
		Player p = (Player) sender;
		
		ItemStack item = new ItemStack(Material.STICK);
		
		if(args[0].equals("parkour")) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("JnR tool parkour");
			meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
			item.setItemMeta(meta);
		} else if(args[0].equals("display")) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("JnR tool display");
			meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
			item.setItemMeta(meta);
		} else return false;
		
		if(p.getInventory().addItem(item).size() > 0) {
			p.sendMessage("Es ist nicht genug Platz in deinem Inventar verfügbar!");
			return true;
		}
		
		return true;
	}

}
