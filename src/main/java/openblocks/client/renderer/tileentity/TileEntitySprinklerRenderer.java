package openblocks.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import openblocks.client.model.ModelSprinkler;
import openblocks.common.tileentity.TileEntitySprinkler;
import openmods.utils.BlockUtils;

public class TileEntitySprinklerRenderer extends TileEntitySpecialRenderer {

    private ModelSprinkler model = new ModelSprinkler();
    private static final ResourceLocation texture = new ResourceLocation("openblocks", "textures/models/sprinkler.png");

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

        TileEntitySprinkler sprinkler = (TileEntitySprinkler) tileentity;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.0f, (float) z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glPushMatrix();
        GL11.glRotatef(-BlockUtils.getRotationFromOrientation(sprinkler.getOrientation()), 0, 1, 0);
        bindTexture(texture);
        model.render(sprinkler, f);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

}
