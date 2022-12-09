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
	
	/**
	 * Creates not existing tables and reads values
	 */
	public static void initDb() {		
		try {
			Main plugin = Main.instance;
			if(!plugin.getDataFolder().exists()) new File(plugin.getDataFolder().getAbsolutePath()).mkdir();
			con = DriverManager.getConnection("jdbc:sqlite:plugins/" + plugin.getDataFolder().getName() + "/jnr.db");
			
			Statement stat = con.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS highscores ('player_uuid' TEXT NOT NULL UNIQUE PRIMARY KEY, 'player_name' TEXT NOT NULL, 'highscore_time_nanos' INTEGER NOT NULL, 'world_uuid_start' TEXT NOT NULL, 'world_name_start', 'x_start' INTEGER NOT NULL, 'y_start' INTEGER NOT NULL, 'z_start' INTEGER NOT NULL, 'world_uuid_end' TEXT NOT NULL, 'world_name_end' TEXT NOT NULL, 'x_end' INTEGER NOT NULL, 'y_end' INTEGER NOT NULL, 'z_end' INTEGER NOT NULL)");
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS coords ('key' TEXT NOT NULL UNIQUE PRIMARY KEY, 'world_uuid' TEXT NOT NULL, 'world_name' TEXT NOT NULL, 'x' INTEGER NOT NULL, 'y' INTEGER NOT NULL, 'z' INTEGER NOT NULL, 'x_old' INTEGER, 'y_old' INTEGER, 'z_old' INTEGER, 'player_uuid' TEXT NOT NULL, 'player_name' TEXT NOT NULL, 'last_changed' DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
			// keys: start, end, highscore_display
			//stat.executeUpdate("CREATE TABLE IF NOT EXISTS deaths ('player_uuid' TEXT NOT NULL UNIQUE PRIMARY KEY, 'player_name' TEXT NOT NULL, 'deaths' INTEGER NOT NULL DEFAULT 0)");
			
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
	
	/**
	 * Sets the new highscore only if the time is a new highscore
	 * @param player - The player
	 * @param time - The players time achieved in the J&R
	 */
	public static void setHighscore(Player player, long time) {
		
		try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT highscore_time_nanos FROM highscores WHERE player_uuid = '" + player.getUniqueId() + "' AND world_uuid_start = '" + Main.blockStart.getWorld().getUID() + "' AND world_name_start = '" + Main.blockStart.getWorld().getName() + "' AND x_start = " + Main.blockStart.getX() + " AND y_start = " + Main.blockStart.getY() + " AND z_start = " + Main.blockStart.getZ() + " AND world_uuid_end = '" + Main.blockEnd.getWorld().getUID() + "' AND world_name_end = '" + Main.blockEnd.getWorld().getName() + "' AND x_end = " + Main.blockEnd.getX() + " AND y_end = " + Main.blockEnd.getY() + " AND z_end = " + Main.blockEnd.getZ());
			boolean next = rs.next();
			if(!next || (next && rs.getLong(1) > time)) {
				
				Main.sendMessage(player, "Glückwunsch, du hast einen neuen persönlichen Highscore erreicht!", false);
				int rows = stat.executeUpdate("INSERT OR REPLACE INTO highscores(player_uuid, player_name, highscore_time_nanos, world_uuid_start, world_name_start, x_start, y_start, z_start, world_uuid_end, world_name_end, x_end, y_end, z_end) VALUES ('" + player.getUniqueId() + "', '" + player.getName() + "', " + time + ", '" + Main.blockStart.getWorld().getUID() + "', '" + Main.blockStart.getWorld().getName() + "', " + Main.blockStart.getX() + ", " + Main.blockStart.getY() + ", " + Main.blockStart.getZ() + ", '" + Main.blockEnd.getWorld().getUID() + "', '" + Main.blockEnd.getWorld().getName() + "', " + Main.blockEnd.getX() + ", " + Main.blockEnd.getY() + ", " + Main.blockEnd.getZ() + ")");
				if(rows == 1) {
					Main.refreshHighscore();
				} else {
					Main.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
				}
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the coordinates for the ActionBlock associated with the given key
	 * @param key - The ActionBlock key
	 * @param world - The new world
	 * @param x - The new x coordinate
	 * @param y - The new y coordinate
	 * @param z - The new z coordinate
	 * @param player - The player who changed the block
	 */
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
			switch(key) {
				case "start":
					Main.blockStart = new ActionBlock(world, x, y, z);
					Main.blockStart.getBlock().setMetadata("jnr", new FixedMetadataValue(Main.instance, "start"));
					Main.sendMessage(player, "Der Start wurde erfolgreich festgelegt.", false);
					break;
				case "end":
					Main.blockEnd = new ActionBlock(world, x, y, z);
					Main.blockEnd.getBlock().setMetadata("jnr", new FixedMetadataValue(Main.instance, "end"));
					Main.sendMessage(player, "Das Ende wurde erfolgreich festgelegt.", false);
					break;
				case "highscore_display":
					Main.blockHigscoreDisplay = new ActionBlock(world, x, y, z);
					Main.blockHigscoreDisplay.getBlock().setMetadata("jnr", new FixedMetadataValue(Main.instance, "highscore_display"));
					Main.refreshHighscore();
					Main.sendMessage(player, "Das Highscore-Display wurde erfolgreich festgelegt.", false);
					break;
				default: Main.sendMessage(player, "Irgendwas unbekanntes wurde zumindest in der Datenbank festgelegt lol.", false);
					break;
			}
			
			Statement stat = con.createStatement();
			int rows = stat.executeUpdate("INSERT OR REPLACE INTO coords(key, world_uuid, world_name, x, y, z, x_old, y_old, z_old, player_uuid, player_name) VALUES ('" + key + "', '" + world.getUID() + "', '" + world.getName() + "', " + x + ", " + y + ", " + z + ", " + (block != null ? block.getX() : null) + ", " + (block != null ? block.getY() : null) + ", " + (block != null ? block.getZ() : null) + ", '" + player.getUniqueId() + "', '" + player.getName() + "')");
			if(rows != 1) {
				Main.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrives the max top 10 players with highscores from the database
	 * @return HashMap<String, Long> - The players with their highscore
	 */
	public static HashMap<String, Long> getTopTen() {
		
		try {
			HashMap<String, Long> times = new HashMap<>();
			
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT player_name, highscore_time_nanos FROM highscores WHERE world_uuid_start = '" + Main.blockStart.getWorld().getUID() + "' AND world_name_start = '" + Main.blockStart.getWorld().getName() + "' AND x_start = " + Main.blockStart.getX() + " AND y_start = " + Main.blockStart.getY() + " AND z_start = " + Main.blockStart.getZ() + " AND world_uuid_end = '" + Main.blockEnd.getWorld().getUID() + "' AND world_name_end = '" + Main.blockEnd.getWorld().getName() + "' AND x_end = " + Main.blockEnd.getX() + " AND y_end = " + Main.blockEnd.getY() + " AND z_end = " + Main.blockEnd.getZ() + " ORDER BY highscore_time_nanos ASC LIMIT 10");
			while(rs.next()) {
				times.put(rs.getString("player_name"), rs.getLong("highscore_time_nanos"));
			}
			
			return times;
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Retrieves the number of deaths associated with the given player
	 * @param player - The player
	 * @return int - The number of the players deaths
	 */
	public static int getDeaths(Player player) {
		
		/*try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT deaths FROM deaths WHERE player_uuid = '" + player.getUniqueId() + "'");
			if(rs.next()) {
				return rs.getInt(1);
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}*/
		
		return 0;
	}
	
	/**
	 * Increases the deaths value associated with the given player in the database
	 * @param player - The player
	 * @return int - The resulting, increased number of the players deaths
	 */
	public static int increaseDeath(Player player) {
		
		/*try {
			Statement stat = con.createStatement();
			ResultSet rs = stat.executeQuery("SELECT deaths FROM deaths WHERE player_uuid = '" + player.getUniqueId() + "'");
			int rows;
			int res;
			if(rs.next()) {
				res = rs.getInt(1) + 1;
				rows = stat.executeUpdate("UPDATE deaths SET deaths = deaths + 1 WHERE player_uuid = '" + player.getUniqueId() + "'");
			} else {
				rows = stat.executeUpdate("INSERT INTO deaths (player_uuid, player_name, deaths) VALUES ('" + player.getUniqueId() + "' , '" + player.getName() + "', 1)");
				res = 1;
			}
			
			if(rows == 1) {
				return res;
			} else {
				Main.sendMessage(player, "Ein Fehler ist aufgetreten!", true);
			}
			
		} catch(SQLException e) {
			e.printStackTrace();
		}*/
		
		return 0;
	}
	
}
