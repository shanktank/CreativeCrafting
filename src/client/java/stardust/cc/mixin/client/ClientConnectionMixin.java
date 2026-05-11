package stardust.cc.mixin.client;

import stardust.cc.CreativeCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundContainerClosePacket p) || !CreativeCrafting.getConfig().isSticky()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        AbstractContainerMenu inventoryMenu = player.inventoryMenu;
        if ((p.getContainerId() != inventoryMenu.containerId && p.getContainerId() != -2718) || inventoryMenu.slots.size() < 5) return; // NBTE uses -2718

        for (int i = 1; i < 5; i++) {
            if (inventoryMenu.slots.get(i).hasItem()) {
                CreativeCrafting.LOGGER.debug("[CreativeCrafting] Canceled container close packet with id={} (slot {} not empty)", p.getContainerId(), i);
                ci.cancel();
                return;
            }
        }
    }
}