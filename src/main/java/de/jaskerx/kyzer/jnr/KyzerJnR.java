package de.jaskerx.kyzer.jnr;

import de.jaskerx.kyzer.jnr.db.Cache;
import de.jaskerx.kyzer.jnr.time.StopwatchRegistry;
import de.jaskerx.kyzer.jnr.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.jaskerx.kyzer.jnr.commands.JnRCommand;
import de.jaskerx.kyzer.jnr.listeners.PlayerInteractListener;
import org.mariadb.jdbc.Connection;
import org.mariadb.jdbc.MariaDbDataSource;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author JaskerX
 * @version Spigot 1.12.2 +
 */
public class KyzerJnR extends JavaPlugin {

	private static KyzerJnR instance;
	private MariaDbDataSource dataSource;
	private Utils utils;
	private Cache cache;

	@Override
	public void onEnable() {
		instance = this;
		initDataSource();

		utils = new Utils(this);
		cache = new Cache(this, utils, dataSource);
		cache.loadData();
		StopwatchRegistry.init(utils, cache);

		getCommand("jnr").setExecutor(new JnRCommand(utils));

		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(new PlayerInteractListener(utils, cache), this);
	}



	private void initDataSource() {
		dataSource = new MariaDbDataSource();

		File dataFolder = getDataFolder();
		if(!dataFolder.exists()) new File(dataFolder.getAbsolutePath()).mkdir();

		try(FileReader reader = new FileReader(new File(dataFolder, "database.properties"))) {
			Properties properties = new Properties();
			properties.load(reader);

			Class.forName("org.mariadb.jdbc.Driver");
			dataSource.setUrl(properties.getProperty("url"));
			dataSource.setUser(properties.getProperty("user"));
			dataSource.setPassword(properties.getProperty("password"));

			try (Connection conn = (Connection) dataSource.getConnection()) {
				if (!conn.isValid(1000)) {
					throw new SQLException("Could not establish database connection.");
				}
			}

		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}



	public static KyzerJnR getInstance() {
		return instance;
	}

}