package openblocks.client;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import openblocks.common.item.ItemSleepingBag;

public class SleepingBagRenderHandler {

    @SubscribeEvent
    public void onPrePlayerRender(RenderPlayerEvent.Pre event) {
        final EntityPlayer entityPlayer = event.entityPlayer;
        if (entityPlayer instanceof EntityOtherPlayerMP) {
            if (entityPlayer.isPlayerSleeping() && ItemSleepingBag.isWearingSleepingBag(entityPlayer)) {
                event.entityPlayer.yOffset = 1.7f;
            }
        }

    }
}
