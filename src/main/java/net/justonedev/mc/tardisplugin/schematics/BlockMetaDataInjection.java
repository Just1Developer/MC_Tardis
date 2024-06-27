package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockMetaDataInjection {
    final Material material;
    HashMap<String, Object> metadataTags;
    List<BlockRunnable> runFunctions;

    public BlockMetaDataInjection(Material material) {
        this.material = material;
        metadataTags = new HashMap<>();
        runFunctions = new ArrayList<>();
    }

    public BlockMetaDataInjection addMetadataTag(String tag, Object value) {
        metadataTags.put(tag, value);
        return this;
    }

    public BlockMetaDataInjection removeMetadataTag(String tag) {
        metadataTags.remove(tag);
        return this;
    }

    public Object getMetadataTag(String tag) {
        return metadataTags.get(tag);
    }

    public BlockMetaDataInjection addRunFunction(BlockRunnable runnable) {
        runFunctions.add(runnable);
        return this;
    }

    public BlockMetaDataInjection removeRunFunction(BlockRunnable runnable) {
        runFunctions.remove(runnable);
        return this;
    }
}
