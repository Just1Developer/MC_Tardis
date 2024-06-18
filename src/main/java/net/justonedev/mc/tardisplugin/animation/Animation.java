package net.justonedev.mc.tardisplugin.animation;

import net.justonedev.mc.tardisplugin.TardisPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Animation {
	public static void startAnimation(Player player, Block block, AnimationType animationType, int startFrame, int frames) {
		new BukkitRunnable() {
			int frame = 0; // Starting from frame 2
			
			@Override
			public void run() {
				if (frame > frames) {
					cancel();
					return;
				}
				// Change block state based on frame
				BlockData blockData = getBlockDataForFrame(animationType, frame + startFrame);
				player.sendBlockChange(block.getLocation(), blockData);
				
				frame++;
			}
		}.runTaskTimer(TardisPlugin.singleton, 0L, 20L); // Adjust timing as needed
	}
	
	private static BlockData getBlockDataForFrame(AnimationType animationType, int frame) {
		// Create and return appropriate BlockData based on the frame
		// This could involve custom model data or different materials
		// Placeholder example:
		return Bukkit.createBlockData(Material.GLASS); // Change as needed
	}
	
	public static void switchToAnimatedTexture(Player player, Block block) {
		// Create the BlockData with the animated texture
		BlockData animatedBlockData = Bukkit.createBlockData(Material.BEDROCK);
		
		// Simulate changing the texture by using custom model data or metadata (depending on your setup)
		// Here we are assuming the animated texture has different metadata
		
		player.sendBlockChange(block.getLocation(), animatedBlockData);
	}
	
	public static void switchToStaticTexture(Player player, Block block) {
		// Create the BlockData with the static texture
		BlockData staticBlockData = Bukkit.createBlockData(Material.BEDROCK);
		
		// Simulate changing the texture by using custom model data or metadata (depending on your setup)
		// Here we are assuming the static texture has different metadata
		
		player.sendBlockChange(block.getLocation(), staticBlockData);
	}
	
}
