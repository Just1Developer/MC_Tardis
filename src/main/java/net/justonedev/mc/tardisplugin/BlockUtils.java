package net.justonedev.mc.tardisplugin;

import net.justonedev.mc.tardisplugin.tardis.Tardis;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public final class BlockUtils {
    public static void setNBTTag(Block block, String key, Object value) {
        block.setMetadata(key, new FixedMetadataValue(TardisPlugin.singleton, value));
    }

    public static MetadataValue getNBTTag(Block block, String key) {
        List<MetadataValue> metadata = block.getMetadata(key);
        if (!metadata.isEmpty()) {
            return metadata.get(0);
        }
        return null;
    }

    public static void setTardisBlockOwnership(Block block, int ownerValue) {
        block.setMetadata(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG,
                new FixedMetadataValue(TardisPlugin.singleton, ownerValue));
    }

    public static int getTardisBlockOwnership(Block block) {
        List<MetadataValue> metadata = block.getMetadata(Tardis.SHELL_GENERATED_BY_WHO_METADATA_TAG);
        if (!metadata.isEmpty()) {
            return metadata.get(0).asInt();
        }
        return -1;
    }
}
