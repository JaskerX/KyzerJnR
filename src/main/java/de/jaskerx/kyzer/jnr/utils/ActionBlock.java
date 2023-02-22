package de.jaskerx.kyzer.jnr.utils;

import de.jaskerx.kyzer.jnr.KyzerJnR;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Represents a block with MetaData "jnr"
 */
public class ActionBlock {
	
	private final World world;
	private final int x;
	private final int y;
	private final int z;
	private final Block block;
	private final String id;
	
	/**
	 * Represents a block with MetaData "jnr"
	 */
	public ActionBlock(String id, World world, int x, int y, int z) {
		this.id = id;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		block = world.getBlockAt(x, y, z);
		block.setMetadata("jnr", new FixedMetadataValue(KyzerJnR.getInstance(), id));
	}

	public ActionBlock(String id, Block block) {
		this.id = id;
		this.block = block;
		world = block.getWorld();
		x = block.getX();
		y = block.getY();
		z = block.getZ();
		block.setMetadata("jnr", new FixedMetadataValue(KyzerJnR.getInstance(), id));
	}

	public String getId() {
		return id;
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
