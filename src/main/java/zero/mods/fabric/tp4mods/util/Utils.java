package zero.mods.fabric.tp4mods.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

public class Utils {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean isServerSide(ServerPlayerEntity player) {
        boolean isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

        try {
            if (player != null) {
                if (isServer) {
                    LOGGER.info("Running on server side");
                    return true;
                }
                LOGGER.info("Running on client side");
                return false;
            }
            return false;
        } catch (Exception e) {
            LOGGER.info("isServerSide check failed, assuming client side: {}", e.getMessage());
            return false;
        }
    }
}
