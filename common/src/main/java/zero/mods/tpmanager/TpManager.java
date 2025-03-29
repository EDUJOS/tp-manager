package zero.mods.tpmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TpManager {
    public static final String MOD_ID = "tpmanager";
    private static final Logger log = LoggerFactory.getLogger(TpManager.class);

    public static void init() {
        // Write common init code here.
        log.info("Common init");
    }
}
