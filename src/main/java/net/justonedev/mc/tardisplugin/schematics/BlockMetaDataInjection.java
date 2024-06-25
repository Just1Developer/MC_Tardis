package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Material;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;

public class BlockMetaDataInjection {
    final Material material;
    HashMap<String, Object> metadataTags;

    public BlockMetaDataInjection(Material material) {
        this.material = material;
        metadataTags = new HashMap<>();
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
}
