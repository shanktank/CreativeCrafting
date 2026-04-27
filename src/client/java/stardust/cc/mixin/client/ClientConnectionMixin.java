package stardust.cc.mixin.client;

import stardust.cc.CreativeCrafting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof CloseHandledScreenC2SPacket p) || !CreativeCrafting.getConfig().isSticky()) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        ScreenHandler playerScreenHandler = player.playerScreenHandler;
        if (p.getSyncId() != playerScreenHandler.syncId || playerScreenHandler.slots.size() < 5) return;

        for (int i = 1; i < 5; i++) {
            if (!playerScreenHandler.slots.get(i).getStack().isEmpty()) {
                CreativeCrafting.LOGGER.info("[CreativeCrafting] Canceled CloseHandledScreenC2SPacket with syncId=" + p.getSyncId() + " (slot " + i + " not empty)");
                ci.cancel();
                return;
            }
        }
    }
}