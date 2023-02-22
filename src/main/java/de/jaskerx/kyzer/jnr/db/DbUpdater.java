package de.jaskerx.kyzer.jnr.db;

import de.jaskerx.kyzer.jnr.ActionBlock;
import de.jaskerx.kyzer.jnr.KyzerJnR;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.IntConsumer;

public class DbUpdater {

    private final KyzerJnR plugin;
    private final DbManager db;
    private final Cache cache;

    public DbUpdater(KyzerJnR plugin, DbManager db, Cache cache) {
        this.plugin = plugin;
        this.db = db;
        this.cache = cache;
    }

    public void updateBlock(ActionBlock block, Player player, IntConsumer consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            byte[] worldUUID = db.getConverter().uuidToByteArray(block.getWorld().getUID());
            String worldName = block.getWorld().getName();
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            byte[] playerUUID = db.getConverter().uuidToByteArray(player.getUniqueId());
            String playerName = player.getName();

            try (PreparedStatement statement = db.getConnection().prepareStatement("INSERT INTO coordinates (id, world_uuid, world_name, x, y, z, player_changed_uuid, player_changed_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE world_uuid = ?, world_name = ?, x_old = x, y_old = y, z_old = z, x = ?, y = ?, z = ?, player_changed_uuid = ?, player_changed_name = ?")) {
                statement.setString(1, block.getId());
                statement.setBytes(2, worldUUID);
                statement.setString(3, worldName);
                statement.setInt(4, x);
                statement.setInt(5, y);
                statement.setInt(6, z);
                statement.setBytes(7, playerUUID);
                statement.setString(8, playerName);
                statement.setBytes(9, worldUUID);
                statement.setString(10, worldName);
                statement.setInt(11, x);
                statement.setInt(12, y);
                statement.setInt(13, z);
                statement.setBytes(14, playerUUID);
                statement.setString(15, playerName);

                int rows = statement.executeUpdate();
                consumer.accept(rows);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateHighscore(Player player, long time, IntConsumer consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            byte[] playerUUID = db.getConverter().uuidToByteArray(player.getUniqueId());
            String playerName = player.getName();
            byte[] worldUUID = db.getConverter().uuidToByteArray(cache.getBlockStart().getWorld().getUID());
            int startX = cache.getBlockStart().getX();
            int startY = cache.getBlockStart().getY();
            int startZ = cache.getBlockStart().getZ();
            int endX = cache.getBlockEnd().getX();
            int endY = cache.getBlockEnd().getY();
            int endZ = cache.getBlockEnd().getZ();

            try (PreparedStatement statementStore = db.getConnection().prepareStatement("INSERT INTO highscores (player_uuid, player_name, highscore_time_millis, world_uuid, start_x, start_y, start_z, end_x, end_y, end_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE player_uuid = ?, player_name = ?, highscore_time_millis = ?, world_uuid = ?, start_x = ?, start_y = ?, start_z = ?, end_x = ?, end_y = ?, end_z = ?")) {
                statementStore.setBytes(1, playerUUID);
                statementStore.setString(2, playerName);
                statementStore.setLong(3, time);
                statementStore.setBytes(4, worldUUID);
                statementStore.setInt(5, startX);
                statementStore.setInt(6, startY);
                statementStore.setInt(7, startZ);
                statementStore.setInt(8, endX);
                statementStore.setInt(9, endY);
                statementStore.setInt(10, endZ);
                statementStore.setBytes(11, playerUUID);
                statementStore.setString(12, playerName);
                statementStore.setLong(13, time);
                statementStore.setBytes(14, worldUUID);
                statementStore.setInt(15, startX);
                statementStore.setInt(16, startY);
                statementStore.setInt(17, startZ);
                statementStore.setInt(18, endX);
                statementStore.setInt(19, endY);
                statementStore.setInt(20, endZ);

                int rows = statementStore.executeUpdate();
                consumer.accept(rows);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

}