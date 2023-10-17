package openblocks.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Strings;

import openblocks.client.model.ModelLuggage;
import openblocks.common.entity.EntityLuggage;

public class EntityLuggageRenderer extends RenderLiving {

    private static ModelBase luggageModel = new ModelLuggage();

    private static final ResourceLocation texture = new ResourceLocation("openblocks", "textures/models/luggage.png");
    private static final ResourceLocation textureSpecial = new ResourceLocation(
            "openblocks",
            "textures/models/luggage_special.png");
    private static final ResourceLocation creeperEffect = new ResourceLocation(
            "textures/entity/creeper/creeper_armor.png");

    public EntityLuggageRenderer() {
        super(luggageModel, 0.5F);
    }

    private int renderSpecial(EntityLuggage luggage, int pass, float partialTickTime) {
        if (luggage.isSpecial() && !Strings.isNullOrEmpty(luggage.func_152113_b())) {
            if (pass == 1) {
                float f1 = luggage.ticksExisted + partialTickTime;
                bindTexture(creeperEffect);
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                float f2 = f1 * 0.01F;
                float f3 = f1 * 0.01F;
                GL11.glTranslatef(f2, f3, 0.0F);
                setRenderPassModel(EntityLuggageRenderer.luggageModel);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glEnable(GL11.GL_BLEND);
                float f4 = 0.5F;
                GL11.glColor4f(f4, f4, f4, 1.0F);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
                return 1;
            }
            if (pass == 2) {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
        return -1;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float par2) {
        EntityLuggage luggage = (EntityLuggage) entity;
        if (luggage.isSpecial()) {
            float oscMagnitude = (float) Math.abs(Math.sin((entity.ticksExisted + par2) * 0.05));
            GL11.glColor3f(oscMagnitude * 0.4f + 0.6f, oscMagnitude * 0.4f + 0.6f, 1f);
        }
    }

    @Override
    protected int inheritRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3) {
        return -1;
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int pass, float partialTickTime) {
        return renderSpecial((EntityLuggage) entity, pass, partialTickTime);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        if (entity instanceof EntityLuggage && ((EntityLuggage) entity).isSpecial()) {
            return textureSpecial;
        }
        return texture;
    }

}
