package de.jaskerx.kyzer.jnr.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import de.jaskerx.kyzer.jnr.main.ActionBlock;
import de.jaskerx.kyzer.jnr.main.Main;

public class DbManager {

	private static Connection con;
	
	public static void initDb() {		
		try {
			Main plugin = Main.instance;
			if(!plugin.getDataFolder().exists()) new File(plugin.getDataFolder().getAbsolutePath()).mkdir();
			con = DriverManager.getConnection("jdbc:sqlite:plugins/" + plugin.getDataFolder().getName() + "/jnr.db");
			
			Statement stat = con.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS highscores ('player_uuid' TEXT NOT NULL UNIQUE PRIMARY KEY, 'player_name' TEXT NOT NULL, 'highscore_time_nanos' INTEGER NOT NULL)");
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS coords ('key' TEXT NOT NULL UNIQUE PRIMARY KEY, 'world_uuid' TEXT NOT NULL, 'world_name' TEXT NOT NULL, 'x' INTEGER NOT NULL, 'y' INTEGER NOT NULL, 'z' INTEGER NOT NULL, 'x_old' INTEGER, 'y_old' INTEGER, 'z_old' INTEGER, 'player_uuid' TEXT NOT NULL, 'player_name' TEXT NOT NULL, 'last_changed' DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
			// keys: start, end
			
			ResultSet rs = stat.executeQuery("SELECT * FROM coords WHERE key = 'start'");
			if(rs.next()) {
				Main.blockStart = new ActionBlock(Bukkit.getWorld(UUID.fromString(rs.getString("world_uuid"))), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				Main.blockStart.getBlock().setMetadata("jnr", new FixedMetadataValue(plugin, "start"));
			}
			rs = stat.executeQuery("SELECT * FROM coords WHERE key = 'end'");
			if(rs.next()) {
				Main.blockEnd = new ActionBlock(Bukkit.getWorld(UUID.fromString(rs.getString("world_uuid"))), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				Main.blockEnd.getBlock().setMetadata("jnr", new FixedMetadataValue(plugin, "end"));
			}
			rs = stat.executeQuery("SELECT * FROM coords WHERE key = 'highscore_display'");
			if(rs.next()) {
				Main.blockHigscoreDisplay = new ActionBlock(Bukkit.getWorld(UUID.fromString(rs.getString("world_uuid"))), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
				Main.blockHigscoreDisplay.getBlock().setMetadata("jnr", new FixedMetadataValue(plugin, "highscore_display"));
			}
			Main.refreshHighscore();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the database Connection
	 */
	public static void close() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void setHighscore(Player player, long time) {
		
		try {
			//TODO: Only change if time less than in db -> sendmessage neuer highscore
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT highscore_time_nanos FROM highscores WHERE player_uuid = '" + player.getUniqueId() + "'");
			boolean next = rs.next();
			if(!next || (next && rs.getLong(1) > time)) {
				
				player.sendMessage("Glückwunsch, du hast einen neuen persönlichen Highscore erreicht!");
				int rows = stat.executeUpdate("INSERT OR REPLACE INTO highscores(player_uuid, player_name, highscore_time_nanos) VALUES ('" + player.getUniqueId() + "', '" + player.getName() + "', " + time + ")");
				if(rows == 1) {
					Main.refreshHighscore();
				} else {
					player.sendMessage("Ein Fehler ist aufgetreten!");
				}
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void setCoords(String key, World world, int x, int y, int z, Player player) {
		
		try {
			ActionBlock block;
			switch(key) {
				case "start": block = Main.blockStart;
					break;
				case "end": block = Main.blockEnd;
					break;
				case "highscore_display": block = Main.blockHigscoreDisplay;
					break;
				default: block = null;
					break;
			}
			if(block != null) {
				block.getBlock().removeMetadata("jnr", Main.instance);
				if(key.equals("highscore_display")) {
					Main.removeHighscore();
				}
			}
			
			Statement stat = con.createStatement();
			int rows = stat.executeUpdate("INSERT OR REPLACE INTO coords(key, world_uuid, world_name, x, y, z, x_old, y_old, z_old, player_uuid, player_name) VALUES ('" + key + "', '" + world.getUID() + "', '" + world.getName() + "', " + x + ", " + y + ", " + z + ", " + (block != null ? block.getX() : null) + ", " + (block != null ? block.getY() : null) + ", " + (block != null ? block.getZ() : null) + ", '" + player.getUniqueId() + "', '" + player.getName() + "')");
			if(rows == 1) {
				switch(key) {
					case "start":
						Main.blockStart = new ActionBlock(world, x, y, z);
						player.sendMessage("Der Start wurde erfolgreich festgelegt.");
						break;
					case "end":
						Main.blockEnd = new ActionBlock(world, x, y, z);
						player.sendMessage("Das Ende wurde erfolgreich festgelegt.");
						break;
					case "highscore_display":
						Main.blockHigscoreDisplay = new ActionBlock(world, x, y, z);
						Main.refreshHighscore();
						player.sendMessage("Das Highscore-Display wurde erfolgreich festgelegt.");
						break;
					default: player.sendMessage("Irgendwas unbekanntes wurde zumindest in der Datenbank festgelegt lol.");
						break;
				}
			} else {
				player.sendMessage("Ein Fehler ist aufgetreten!");
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, Long> getTopTen() {
		
		try {
			HashMap<String, Long> times = new HashMap<>();
			
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT player_name, highscore_time_nanos FROM highscores ORDER BY highscore_time_nanos DESC LIMIT 10");
			while(rs.next()) {
				times.put(rs.getString("player_name"), rs.getLong("highscore_time_nanos"));
			}
			
			return times;
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
