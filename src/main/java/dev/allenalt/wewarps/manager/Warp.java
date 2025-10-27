package dev.allenalt.wewarps.manager;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Simple POJO representing a warp entry.
 */
public class Warp {

    private final String name;
    private final String owner;
    private final String description;
    private final Location location;

    public Warp(@NotNull String name, @NotNull Location location, @NotNull String owner, @NotNull String description) {
        this.name = name.toLowerCase();
        this.location = location.clone();
        this.owner = owner;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    // Equals/hash based on name (unique)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Warp warp = (Warp) o;
        return name.equals(warp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
