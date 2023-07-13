package net.onelitefeather.antiredstoneclockremastered.utils;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Constants {

    public static final String PERMISSION_NOTIFY = "antiredstoneclockremasterd.notify.admin";

    public static final String PREFIX = "<gray></</gray><gradient:#c20300:#ff7f7a>Anti</gradient><gradient:#c20300:#ff7f7a>Redstone</gradient><gray>></gray>";

    public static final Collection<Material> REDSTONE_ITEMS = new ArrayList<>(initRedstoneItems());

    private static Collection<Material> initRedstoneItems() {
        List<Material> materials = new ArrayList<>();
        materials.add(Material.getMaterial("REDSTONE_WIRE"));
        if (Material.getMaterial("DIODE_BLOCK_OFF") != null) {
            // under 1.13
            materials.add(Material.getMaterial("DIODE_BLOCK_OFF"));
            materials.add(Material.getMaterial("DIODE_BLOCK_ON"));
            materials.add(Material.getMaterial("PISTON_BASE"));
            materials.add(Material.getMaterial("PISTON_EXTENSION"));
            materials.add(Material.getMaterial("PISTON_STICKY_BASE"));
            materials.add(Material.getMaterial("REDSTONE_COMPARATOR_OFF"));
            materials.add(Material.getMaterial("REDSTONE_COMPARATOR_ON"));
        } else {
            materials.add(Material.getMaterial("REPEATER"));
            materials.add(Material.getMaterial("PISTON"));
            materials.add(Material.getMaterial("STICKY_PISTON"));
            materials.add(Material.getMaterial("COMPARATOR"));
            materials.add(Material.getMaterial("OBSERVER"));
        }
        return materials;
    }

}
