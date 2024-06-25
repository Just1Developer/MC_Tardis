package net.justonedev.mc.tardisplugin.schematics;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InjectionSet implements Iterable<BlockMetaDataInjection> {
    private final Set<BlockMetaDataInjection> injections;

    public InjectionSet() {
        injections = new HashSet<>();
    }

    public boolean add(BlockMetaDataInjection injection) {
        return injections.add(injection);
    }
    public boolean addAll(Collection<BlockMetaDataInjection> injections) {
        return this.injections.addAll(injections);
    }

    public boolean remove(BlockMetaDataInjection injection) {
        return injections.remove(injection);
    }
    public boolean removeAll(Collection<BlockMetaDataInjection> injections) {
        return this.injections.addAll(injections);
    }

    public boolean contains(BlockMetaDataInjection injection) {
        return injections.contains(injection);
    }

    Set<BlockMetaDataInjection> getInjections() {
        return injections;
    }

    public Set<BlockMetaDataInjection> where(Material material, boolean airAsAny) {
        return injections.stream()
                .filter(injection -> injection.material == material || (airAsAny && injection.material == Material.AIR))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Stream<BlockMetaDataInjection> stream() {
        return injections.stream();
    }

    /**
     * Returns an iterator over elements of type {@code BlockMetaDataInjection}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<BlockMetaDataInjection> iterator() {
        return injections.iterator();
    }
}
