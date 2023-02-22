package de.jaskerx.kyzer.jnr.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jaskerx.kyzer.jnr.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.checkerframework.checker.nullness.qual.NonNull;

public class JnRCommand implements CommandExecutor, TabCompleter {

	private final Utils utils;
	private final List<String> argsOne = new ArrayList<>(Arrays.asList("parkour", "display"));

	public JnRCommand(Utils utils) {
		this.utils = utils;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {

		if(!(sender instanceof Player player)) {
			utils.sendMessage(sender, "Diesen Command können nur Spieler ausführen!", true);
			return true;
		}
		if(!sender.hasPermission("jnr.manage")) {
			utils.sendMessage(sender, "Du bist nicht berechtigt, diesen Command auszuführen!", true);
			return true;
		}
		if(args.length < 1) {
			return false;
		}

		ItemStack item = new ItemStack(Material.STICK);
		
		if(argsOne.contains(args[0])) {
			
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("JnR tool " + args[0]);
			meta.addEnchant(Enchantment.KNOCKBACK, 1, false);
			item.setItemMeta(meta);
			
		} else return false;
		
		if(player.getInventory().addItem(item).size() > 0) {
			utils.sendMessage(player, "Es ist nicht genug Platz in deinem Inventar verfügbar!", true);
			return true;
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String name, @NonNull String[] args) {
		List<String> res = new ArrayList<>();
		
		if(args.length == 1) {
			
			for(String s : argsOne) {
				if(s.startsWith(args[0].toLowerCase())) {
					res.add(s);
				}
			}
		}
		
		return res;
	}

}
