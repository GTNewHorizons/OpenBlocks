package openblocks.client.renderer.entity;

import openblocks.OpenBlocks;
import openblocks.common.entity.EntityHangGlider;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class EntityPlayerRenderer extends RenderPlayer {

	@Override
	protected void rotatePlayer(EntityPlayer player, float par2, float par3, float par4) {
		super.rotatePlayer(player, par2, par3, par4);
		if (OpenBlocks.proxy.gliderMap.containsKey(player) && !player.onGround) {
			player.limbSwing = 0f;
			player.prevLimbYaw = 0f;
			player.limbYaw = 0f;
			GL11.glRotatef(75, -1, 0, 0);
		}
	}
}
