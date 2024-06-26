package openblocks.client.renderer.entity;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import openblocks.OpenBlocks.Items;
import openmods.utils.render.RenderUtils;

public class EntityGoldenEyeRenderer extends Render {

    private Tessellator tes = new Tessellator();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float f1, float partialTickTime) {
        bindEntityTexture(entity);
        GL11.glPushMatrix();
        float yaw = RenderUtils.interpolateYaw(entity, partialTickTime);
        float pitch = RenderUtils.interpolatePitch(entity, partialTickTime);

        GL11.glTranslated(x, y, z);
        GL11.glRotatef(yaw, 0, 1, 0);
        GL11.glRotatef(pitch, 1, 0, 0);
        final IIcon icon = Items.goldenEye.getIconFromDamage(0);

        GL11.glScaled(1.0 / 6.0, 1.0 / 6.0, 1.0 / 6.0);
        tes.setTranslation(-0.5, -0.5, 0);
        ItemRenderer.renderItemIn2D(
                tes,
                icon.getInterpolatedU(15),
                icon.getInterpolatedV(2),
                icon.getInterpolatedU(2),
                icon.getInterpolatedV(15),
                13,
                13,
                1.0f / 16.0f);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationItemsTexture;
    }
}
