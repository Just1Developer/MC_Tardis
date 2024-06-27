package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockMetaDataInjection {
    final Set<Material> include;
    final Set<Material> exclude;
    HashMap<String, Object> metadataTags;
    List<BlockRunnable> runFunctions;

    public BlockMetaDataInjection(Material material) {
        this();
        addMaterial(material);
    }
    
    public BlockMetaDataInjection(Collection<Material> materials) {
        this();
        addMaterials(materials);
    }
    
    public BlockMetaDataInjection() {
        this.include = new HashSet<>();
        this.exclude = new HashSet<>();
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
    
    public BlockMetaDataInjection addMaterial(Material material) {
        include.add(material);
        return this;
    }
    
    public BlockMetaDataInjection addMaterials(Collection<Material> materials) {
        include.addAll(materials);
        return this;
    }
    
    public BlockMetaDataInjection removeMaterial(Material material) {
        include.remove(material);
        return this;
    }
    
    public BlockMetaDataInjection removeMaterials(Collection<Material> materials) {
        include.removeAll(materials);
        return this;
    }
    
    public BlockMetaDataInjection excludeMaterial(Material material) {
        exclude.add(material);
        return this;
    }
    
    public BlockMetaDataInjection excludeMaterials(Collection<Material> materials) {
        exclude.addAll(materials);
        return this;
    }
}
