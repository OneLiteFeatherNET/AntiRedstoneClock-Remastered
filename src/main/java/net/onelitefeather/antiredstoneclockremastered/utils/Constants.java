package net.onelitefeather.antiredstoneclockremastered.utils;

import org.bukkit.Material;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Constants {

    public static final String PERMISSION_NOTIFY = "antiredstoneclockremastered.notify.admin";
    public static final String PERMISSION_NOTIFY_UPDATE = "antiredstoneclockremastered.notify.admin.update";
    public static final String DISABLE_DONATION_NOTIFY = "antiredstoneclockremastered.notify.disable.donation";
    public static final URI LATEST_RELEASE_VERSION_URI = URI.create("https://hangar.papermc.io/api/v1/projects/AntiRedstoneClock-Remastered/latestrelease");
    public static final HttpRequest LATEST_RELEASE_VERSION_REQUEST = HttpRequest.newBuilder().GET().uri(LATEST_RELEASE_VERSION_URI).build();
    public static final int MAX_SIGN_LINES = 4;
    public static final int MAX_SIGN_LINE_LENGTH = 16;
    public static final String META_KEY_ARCR_SIGN = "arcr-sign";
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
