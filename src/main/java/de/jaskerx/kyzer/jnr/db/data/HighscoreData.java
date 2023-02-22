package de.jaskerx.kyzer.jnr.db.data;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

public class HighscoreData {

    private final UUID playerUUID;
    private final String playerName;
    private long time;
    private World world;
    private int startX;
    private int startY;
    private int startZ;
    private int endX;
    private int endY;
    private int endZ;

    public HighscoreData(UUID playerUUID, String playerName, long time, UUID worldUUID, int startX, int startY, int startZ, int endX, int endY, int endZ) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.time = time;
        world = Bukkit.getWorld(worldUUID);
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public HighscoreData(HighscoreData data) {
        playerUUID = data.getPlayerUUID();
        playerName = data.getPlayerName();
        time = data.getTime();
        world = data.getWorld();
        startX = data.getStartX();
        startY = data.getStartY();
        startZ = data.getStartZ();
        endX = data.getEndX();
        endY = data.getEndY();
        endZ = data.getEndZ();
    }

    public HighscoreData withNewTime(long time) {
        this.time = time;
        return this;
    }

    public HighscoreData withNewStart(Block start) {
        world = start.getWorld();
        startX = start.getX();
        startY = start.getY();
        startZ = start.getZ();
        return this;
    }

    public HighscoreData withNewEnd(Block end) {
        world = end.getWorld();
        endX = end.getX();
        endY = end.getY();
        endZ = end.getZ();
        return this;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTime() {
        return time;
    }

    public World getWorld() {
        return world;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getEndZ() {
        return endZ;
    }

    public boolean isStart(Block block) {
        return block.getX() == startX && block.getY() == startY && block.getZ() == startZ && block.getWorld().equals(world);
    }

    public boolean isEnd(Block block) {
        return block.getX() == endX && block.getY() == endY && block.getZ() == endZ && block.getWorld().equals(world);
    }

}
