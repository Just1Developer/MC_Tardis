package net.justonedev.mc.tardisplugin.schematics;

import net.justonedev.mc.tardisplugin.BlockUtils;
import net.justonedev.mc.tardisplugin.schematics.rotation.Rotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Objects;

public class Quader {

    final BlockData quaderData;
    QuaderDimensions quaderDimensions;

    public Quader(BlockData blockData, QuaderDimensions dimensions, byte quaderOrientation) {
        this.quaderData = blockData; // I think reference is fine since it's read-only everywhere
        this.quaderDimensions = dimensions.copy(quaderOrientation);
    }
    
    public Quader(BlockData blockData, Vector startPos, Vector endpos) {
        // This is just for expanding later
        final Vector minVect = minimalLocation(startPos, endpos);
        this.quaderData = blockData.copy(minVect);
        this.quaderDimensions = new QuaderDimensions(maximalLocation(startPos, endpos).subtract(minVect));
    }
    
    private static Vector minimalLocation(Vector v1, Vector v2) {
        return new Vector(
                Math.min(v1.getBlockX(), v2.getBlockX()),
                Math.min(v1.getBlockY(), v2.getBlockY()),
                Math.min(v1.getBlockZ(), v2.getBlockZ())
        );
    }
    
    private static Vector maximalLocation(Vector v1, Vector v2) {
        return new Vector(
                Math.max(v1.getBlockX(), v2.getBlockX()),
                Math.max(v1.getBlockY(), v2.getBlockY()),
                Math.max(v1.getBlockZ(), v2.getBlockZ())
        );
    }

    public void placeInWorld(Location anchorPosition) {
        placeInWorld(anchorPosition, Rotation.None, null);
    }
    public void placeInWorld(Location anchorPosition, Rotation rotation) {
        placeInWorld(anchorPosition, rotation, null);
    }
    public void placeInWorld(Location anchorPosition, Rotation rotation, Collection<BlockMetaDataInjection> injections) {
        Location anchor = anchorPosition.clone();
        Vector offset = quaderData.location.clone();
        if (anchor.getWorld() == null) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world was null. Location: " + anchorPosition);
            return;
        }
        if (!anchor.isWorldLoaded()) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world wasn't loaded. World: " + anchor.getWorld().getName());
            return;
        }
        
        Vector xDelta = new Vector(1, 0, 0);
        Vector yDelta = new Vector(0, 1, 0);
        Vector zDelta = new Vector(0, 0, 1);
        
        if (rotation != null && rotation != Rotation.None) {
            rotation.transformVector(offset);
            rotation.transformVector(xDelta);
            rotation.transformVector(zDelta);
        }
        anchor.add(offset);
        
        Location writeHead = anchor.clone();
        Vector bounds = quaderDimensions.toVectorDimension();
        var headBlock = writeHead.getBlock();
        headBlock.setType(quaderData.material);
        quaderData.cacheBlockDataSetters(headBlock);
        
        for (int x = 0; x <= bounds.getBlockX(); ++x, writeHead.add(xDelta)) {
            for (int y = 0; y <= bounds.getBlockY(); ++y, writeHead.add(yDelta)) {
                for (int z = 0; z <= bounds.getBlockZ(); ++z, writeHead.add(zDelta)) {
                    Block b = anchor.getWorld().getBlockAt(writeHead);
                    applyWholeBlockData(b, injections);
                }
                writeHead.setZ(anchor.getBlockZ());
            }
            writeHead.setY(anchor.getBlockY());
        }
    }
    
    public void placeBreakdown(Location anchorPosition, Material material) {
        Location anchor = anchorPosition.clone().add(quaderData.location);
        if (anchor.getWorld() == null) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world was null. Location: " + anchorPosition);
            return;
        }
        if (!anchor.isWorldLoaded()) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world wasn't loaded. World: " + anchor.getWorld().getName());
            return;
        }
        Location writeHead = anchor.clone();
        Vector bounds = quaderDimensions.toVectorDimension();
        var headBlock = writeHead.getBlock();
        headBlock.setType(material);
        
        for (int x = 0; x <= bounds.getBlockX(); ++x, writeHead.add(1, 0, 0)) {
            for (int y = 0; y <= bounds.getBlockY(); ++y, writeHead.add(0, 1, 0)) {
                for (int z = 0; z <= bounds.getBlockZ(); ++z, writeHead.add(0, 0, 1)) {
                    writeHead.getBlock().setType(material);
                }
                writeHead.setZ(anchor.getBlockZ());
            }
            writeHead.setY(anchor.getBlockY());
        }
    }
    
    public void placeCorners(Location anchorPosition, Material material) {
        Location anchor = anchorPosition.clone().add(quaderData.location);
        if (anchor.getWorld() == null) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world was null. Location: " + anchorPosition);
            return;
        }
        if (!anchor.isWorldLoaded()) {
            Bukkit.getLogger().severe("placeInWorld called on Quader, but world wasn't loaded. World: " + anchor.getWorld().getName());
            return;
        }
        anchor.getBlock().setType(material);
        anchor.add(quaderDimensions.toVectorDimension()).getBlock().setType(material);
    }
    
    private void applyWholeBlockData(Block block) {
        applyWholeBlockData(block, null);
    }

    private void applyWholeBlockData(Block block, Collection<BlockMetaDataInjection> injections) {
        block.setType(quaderData.material);
        quaderData.applyAllAttributesTo(block);
        if (injections != null) {
            for (BlockMetaDataInjection injection : injections) {
                for (var attr : injection.metadataTags.entrySet()) {
                    // apply them to the block here
                    BlockUtils.setNBTTag(block, attr.getKey(), attr.getValue());
                }
                for (var func : injection.runFunctions) {
                    func.run(block);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quader quader = (Quader) o;
        return Objects.equals(quaderData, quader.quaderData) && Objects.equals(quaderDimensions, quader.quaderDimensions);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quaderData, quaderDimensions);
    }
    
    @Override
    public String toString() {
        return String.format("{Quader: bounds: {Val1: %d, Val2: %d, Val3: %d, Orientation: %d}, location: {x: %d, y: %d, z: %d}, data: {material: %s, ...}}", quaderDimensions.VALUE1, quaderDimensions.VALUE2, quaderDimensions.VALUE3, quaderDimensions.ORIENTATION_KEY, quaderData.location.getBlockX(), quaderData.location.getBlockY(), quaderData.location.getBlockZ(), quaderData.material.name());
    }
}
