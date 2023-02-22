package de.jaskerx.kyzer.jnr.db;

import de.jaskerx.kyzer.jnr.utils.ActionBlock;
import de.jaskerx.kyzer.jnr.KyzerJnR;
import de.jaskerx.kyzer.jnr.db.data.HighscoreData;
import de.jaskerx.kyzer.jnr.utils.TypeConverter;
import de.jaskerx.kyzer.jnr.utils.Utils;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

public class Cache {

    private final KyzerJnR plugin;
    private final Utils utils;
    private final MariaDbDataSource dataSource;
    private final DbUpdater dbUpdater;
    private ActionBlock blockStart;
    private ActionBlock blockEnd;
    private ActionBlock blockHighscoreDisplay;
    private List<HighscoreData> highscores;

    public Cache(KyzerJnR plugin, Utils utils, MariaDbDataSource dataSource) {
        this.plugin = plugin;
        this.utils = utils;
        this.dataSource = dataSource;
        dbUpdater = new DbUpdater(dataSource, this);
    }

    public void loadData() {
        CompletableFuture.runAsync(() -> {
            TypeConverter converter = new TypeConverter();
            try {
                try (PreparedStatement statement = dataSource.getConnection().prepareStatement("SELECT id, world_uuid, x, y, z FROM coordinates")) {
                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        String key = result.getString("id");
                        World world = plugin.getServer().getWorld(converter.byteArrayToUUID(result.getBytes("world_uuid")));
                        int x = result.getInt("x");
                        int y = result.getInt("y");
                        int z = result.getInt("z");
                        switch (key) {
                            case "start" -> blockStart = new ActionBlock(key, world, x, y, z);
                            case "end" -> blockEnd = new ActionBlock(key, world, x, y, z);
                            case "highscore_display" -> blockHighscoreDisplay = new ActionBlock(key, world, x, y, z);
                        }
                    }
                }

                highscores = new CopyOnWriteArrayList<>();
                try (PreparedStatement statement = dataSource.getConnection().prepareStatement("SELECT player_uuid, player_name, highscore_time_millis, world_uuid, start_x, start_y, start_z, end_x, end_y, end_z FROM highscores")) {
                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        highscores.add(new HighscoreData(
                                converter.byteArrayToUUID(result.getBytes("player_uuid")),
                                result.getString("player_name"),
                                result.getLong("highscore_time_millis"),
                                converter.byteArrayToUUID(result.getBytes("world_uuid")),
                                result.getInt("start_x"),
                                result.getInt("start_y"),
                                result.getInt("start_z"),
                                result.getInt("end_x"),
                                result.getInt("end_y"),
                                result.getInt("end_z")
                        ));
                    }
                }

                utils.setCache(this);
                utils.refreshHighscore();
                plugin.getLogger().info("Database has been loaded!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gets the max top 10 players by highscore
     */
    public CompletableFuture<List<HighscoreData>> getTopTen() {
        return CompletableFuture.supplyAsync(() -> {
            List<HighscoreData> topTen = new ArrayList<>();
            highscores.sort((data1, data2) -> ((Comparable<Long>) data1.getTime()).compareTo(data2.getTime()));
            int i = 0;
            while(i < highscores.size() && topTen.size() < 10) {
                if(highscores.get(i).isStart(blockStart.getBlock()) && highscores.get(i).isEnd(blockEnd.getBlock())) {
                    topTen.add(highscores.get(i));
                }
                i++;
            }
            return topTen;
        });
    }

    /**
     * Sets the new highscore only if the time is a new highscore
     * @param player The player
     * @param time The players time achieved in the J&R
     */
    public void setHighscore(Player player, long time, IntConsumer consumer) {
        CompletableFuture.runAsync(() -> {
            long highscore = 0;
            Optional<HighscoreData> dataOptional = highscores.stream().filter(data -> data.getPlayerUUID().equals(player.getUniqueId())).findFirst();
            HighscoreData data = null;
            if (dataOptional.isPresent()) {
                data = dataOptional.get();
                if(data.isStart(blockStart.getBlock()) && data.isEnd(blockEnd.getBlock())) {
                    highscore = data.getTime();
                }
            }
            if (time < highscore || highscore == 0) {
                utils.sendMessage(player, "Glückwunsch, du hast einen neuen persönlichen Highscore erreicht!", false);
                if (data == null) {
                    highscores.add(new HighscoreData(
                            player.getUniqueId(),
                            player.getName(),
                            time,
                            blockStart.getWorld().getUID(),
                            blockStart.getX(),
                            blockStart.getY(),
                            blockStart.getZ(),
                            blockEnd.getX(),
                            blockEnd.getY(),
                            blockEnd.getZ()
                    ));
                } else {
                    highscores.add(new HighscoreData(data)
                            .withNewTime(time)
                            .withNewStart(blockStart.getBlock())
                            .withNewEnd(blockEnd.getBlock())
                    );
                    highscores.remove(data);
                }
                utils.refreshHighscore();
                dbUpdater.updateHighscore(player, time).thenAcceptAsync(consumer::accept);
            }
        });
    }

    public void setBlockStart(Block block, Player player, IntConsumer consumer) {
        if(blockStart != null) {
            blockStart.getBlock().removeMetadata("jnr", plugin);
        }
        blockStart = new ActionBlock("start", block);
        if(!blockStart.getBlock().hasMetadata("jnr")) {
            consumer.accept(-1);
            return;
        }
        dbUpdater.updateBlock(blockStart, player).thenAcceptAsync(consumer::accept);
    }

    public void setBlockEnd(Block block, Player player, IntConsumer consumer) {
        if(blockEnd != null) {
            blockEnd.getBlock().removeMetadata("jnr", plugin);
        }
        blockEnd = new ActionBlock("end", block);
        if(!blockEnd.getBlock().hasMetadata("jnr")) {
            consumer.accept(-1);
            return;
        }
        dbUpdater.updateBlock(blockEnd, player).thenAcceptAsync(consumer::accept);
    }

    public void setBlockHighscoreDisplay(Block block, Player player, IntConsumer consumer) {
        if(blockHighscoreDisplay != null) {
            blockHighscoreDisplay.getBlock().removeMetadata("jnr", plugin);
        }
        blockHighscoreDisplay = new ActionBlock("highscore_display", block);
        if(!blockHighscoreDisplay.getBlock().hasMetadata("jnr")) {
            consumer.accept(-1);
            return;
        }
        dbUpdater.updateBlock(blockHighscoreDisplay, player).thenAcceptAsync(consumer::accept);
    }

    public ActionBlock getBlockStart() {
        return blockStart;
    }

    public ActionBlock getBlockEnd() {
        return blockEnd;
    }

    public ActionBlock getBlockHighscoreDisplay() {
        return blockHighscoreDisplay;
    }

}

