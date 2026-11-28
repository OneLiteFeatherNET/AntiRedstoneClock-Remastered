package net.onelitefeather.antiredstoneclockremastered.service.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FoliaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoliaHelper.class);
    private static final String REGIONIZED_SERVER_CLASS = "io.papermc.paper.threadedregions.RegionizedServer";

    /**
     * Detects if the server is running on Folia.
     * Folia is identified by its regionized server class, independent of the Minecraft version.
     *
     * @return true if Folia is detected, false otherwise
     */
    static boolean isFolia() {
        return isFolia(Class::forName);
    }

    static boolean isFolia(ClassLookup classLookup) {
        try {
            classLookup.find(REGIONIZED_SERVER_CLASS);
            LOGGER.debug("Found {}, running on a Folia server", REGIONIZED_SERVER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.debug("No Folia classes found, running on a regular Paper server");
            return false;
        }
    }

    /**
     * Looks up a class by its fully qualified name.
     */
    @FunctionalInterface
    interface ClassLookup {
        Class<?> find(String className) throws ClassNotFoundException;
    }
}
