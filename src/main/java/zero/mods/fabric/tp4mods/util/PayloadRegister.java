package zero.mods.fabric.tp4mods.util;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.slf4j.Logger;

public class PayloadRegister {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static <T extends CustomPayload> void typesRegister(CustomPayload.Id<T> payloadId, PacketCodec<? super RegistryByteBuf, T> payloadCodec, Boolean isS2C) {
        PayloadTypeRegistry<RegistryByteBuf> payloadType;
        if (isS2C) {
            payloadType = PayloadTypeRegistry.playS2C();
            LOGGER.info("[{}]: Server to Client", isS2C);
        } else {
            payloadType = PayloadTypeRegistry.playC2S();
            LOGGER.info("[{}]: Client to Server", isS2C);
        }
        try {
            payloadType.register(payloadId, payloadCodec);
            LOGGER.info("Payload Type {} registrado con exito", payloadId.id());
        } catch (Exception e) {
            LOGGER.info("No se ha podido registrar el Payload Type {} \nError: {}", payloadId.id(), e.getMessage());
        }
    }

    public static void registerNetworkReceivers() {
        LOGGER.info("Comming soon");
    }
}
