package zero.mods.tpmanager.fabric.client.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zero.mods.tpmanager.fabric.client.TpManagerFabricClient;
import zero.mods.tpmanager.fabric.payload.PlayerListPayload;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(
            method = "interactItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUseItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (isHoldingModItem()) {
            cir.setReturnValue(ActionResult.SUCCESS);
            cir.cancel();
            requestPlayerList();
        }
    }

    @Unique
    private void requestPlayerList() {
        ClientPlayNetworking.send(new PlayerListPayload(List.of()));
    }

    @Unique
    private Boolean isHoldingModItem() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.getMainHandStack().getItem() == TpManagerFabricClient.modItem;
    }
}