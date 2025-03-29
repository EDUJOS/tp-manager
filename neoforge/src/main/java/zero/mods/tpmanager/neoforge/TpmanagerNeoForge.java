package zero.mods.tpmanager.neoforge;

import zero.mods.tpmanager.TpManager;
import net.neoforged.fml.common.Mod;

@Mod(TpManager.MOD_ID)
public final class TpmanagerNeoForge {
    public TpmanagerNeoForge() {
        // Run our common setup.
        TpManager.init();
    }
}
