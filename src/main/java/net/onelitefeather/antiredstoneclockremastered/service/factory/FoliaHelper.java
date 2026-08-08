package net.onelitefeather.antiredstoneclockremastered.service.factory;

import io.papermc.paper.ServerBuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FoliaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoliaHelper.class);

    /**
     * Detects if the server is running on Folia.
     * Folia support is available starting from Paper version 1.20 and above.
     *
     * @return true if Folia is detected, false otherwise
     */
    static boolean isFolia() {
        try {
            ServerBuildInfo buildInfo = ServerBuildInfo.buildInfo();
            String version = buildInfo.minecraftVersionId();

            // Check if running Paper 1.20+ which supports Folia
            if (version.startsWith("1.20") || version.startsWith("1.21") || version.startsWith("1.22")) {
                // Additional check for Folia-specific classes
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                return true;
            }
            LOGGER.debug("Minecraft version {} is not checked for Folia support", version);
            return false;
        } catch (ClassNotFoundException e) {
            LOGGER.debug("No Folia classes found, running on a regular Paper server");
            return false;
        }
    }
}
