package de.jaskerx.kyzer.jnr.main;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Represents a block with MetaData "jnr"
 */
public class ActionBlock {
	
	private World world;
	private int x;
	private int y;
	private int z;
	private Block block;
	
	/**
	 * Represents a block with MetaData "jnr"
	 */
	public ActionBlock(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		block = world.getBlockAt(x, y, z);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public World getWorld() {
		return world;
	}

}
