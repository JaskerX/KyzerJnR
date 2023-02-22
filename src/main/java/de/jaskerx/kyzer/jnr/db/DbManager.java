package de.jaskerx.kyzer.jnr.db;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import de.jaskerx.kyzer.jnr.TypeConverter;
import de.jaskerx.kyzer.jnr.KyzerJnR;

public class DbManager {

	private final KyzerJnR plugin;
	private Connection connection;
	private final TypeConverter converter;

	public DbManager(KyzerJnR plugin) {
		this.plugin = plugin;
		converter = new TypeConverter();
	}

	/**
	 * Create connection
	 */
	public void init() {
		File dataFolder = plugin.getDataFolder();
		if(!dataFolder.exists()) new File(dataFolder.getAbsolutePath()).mkdir();

		try(FileReader reader = new FileReader(new File(dataFolder, "database.properties"))) {
			Properties properties = new Properties();
			properties.load(reader);

			Class.forName("org.mariadb.jdbc.Driver");
			connection = DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("user"), properties.getProperty("password"));
			
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes the database connection
	 */
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	public Connection getConnection() {
		return connection;
	}

	public TypeConverter getConverter() {
		return converter;
	}

}
