package de.jaskerx.kyzer.jnr.db;

import de.jaskerx.kyzer.jnr.ActionBlock;
import de.jaskerx.kyzer.jnr.KyzerJnR;
import de.jaskerx.kyzer.jnr.db.data.HighscoreData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class Cache {

    private final KyzerJnR plugin;
    private final DbManager db;
    private final DbUpdater dbUpdater;
    private ActionBlock blockStart;
    private ActionBlock blockEnd;
    private ActionBlock blockHighscoreDisplay;
    private List<HighscoreData> highscores;

    public Cache(KyzerJnR plugin, DbManager db) {
        this.plugin = plugin;
        this.db = db;
        dbUpdater = new DbUpdater(plugin, db, this);
    }

    public void loadData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                try (PreparedStatement statement = db.getConnection().prepareStatement("SELECT id, world_uuid, x, y, z FROM coordinates")) {
                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        String key = result.getString("id");
                        World world = Bukkit.getWorld(db.getConverter().byteArrayToUUID(result.getBytes("world_uuid")));
                        int x = result.getInt("x");
                        int y = result.getInt("y");
                        int z = result.getInt("z");
                        switch (key) {
                            case "start":
                                blockStart = new ActionBlock(key, world, x, y, z);
                                break;
                            case "end":
                                blockEnd = new ActionBlock(key, world, x, y, z);
                                break;
                            case "highscore_display":
                                blockHighscoreDisplay = new ActionBlock(key, world, x, y, z);
                                break;
                        }
                    }
                }

                highscores = new CopyOnWriteArrayList<>();
                try (PreparedStatement statement = db.getConnection().prepareStatement("SELECT * FROM highscores")) {
                    ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        highscores.add(new HighscoreData(
                                db.getConverter().byteArrayToUUID(result.getBytes("player_uuid")),
                                result.getString("player_name"),
                                result.getLong("highscore_time_millis"),
                                db.getConverter().byteArrayToUUID(result.getBytes("world_uuid")),
                                result.getInt("start_x"),
                                result.getInt("start_y"),
                                result.getInt("start_z"),
                                result.getInt("end_x"),
                                result.getInt("end_y"),
                                result.getInt("end_z")
                        ));
                    }
                }

                plugin.refreshHighscore();
                plugin.getLogger().info("Database has been loaded!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Gets the max top 10 players by highscore
     * @param consumer The consumer to execute when the task is done
     */
    public void getTopTen(Consumer<List<HighscoreData>> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<HighscoreData> topTen = new ArrayList<>();
            highscores.sort((data1, data2) -> ((Comparable<Long>) data1.getTime()).compareTo(data2.getTime()));
            int i = 0;
            while(i < highscores.size() && topTen.size() < 10) {
                if(highscores.get(i).isStart(blockStart.getBlock()) && highscores.get(i).isEnd(blockEnd.getBlock())) {
                    topTen.add(highscores.get(i));
                }
                i++;
            }
            consumer.accept(topTen);
        });
    }

    /**
     * Sets the new highscore only if the time is a new highscore
     * @param player The player
     * @param time The players time achieved in the J&R
     */
    public void setHighscore(Player player, long time, IntConsumer consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
                plugin.sendMessage(player, "Glückwunsch, du hast einen neuen persönlichen Highscore erreicht!", false);
                if (data != null) {
                    highscores.add(new HighscoreData(data)
                            .withNewTime(time)
                            .withNewStart(blockStart.getBlock())
                            .withNewEnd(blockEnd.getBlock())
                    );
                    highscores.remove(data);
                } else {
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
                }
                plugin.refreshHighscore();
                dbUpdater.updateHighscore(player, time, consumer);
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
        dbUpdater.updateBlock(blockStart, player, consumer);
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
        dbUpdater.updateBlock(blockEnd, player, consumer);
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
        dbUpdater.updateBlock(blockHighscoreDisplay, player, consumer);
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

