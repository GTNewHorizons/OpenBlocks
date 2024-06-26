package openblocks.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.tileentity.TileEntity;

public class ModelXPShower extends ModelBase {

    ModelRenderer end;
    ModelRenderer main;

    public ModelXPShower() {
        textureWidth = 32;
        textureHeight = 32;

        end = new ModelRenderer(this, 0, 10);
        end.addBox(-1F, 1F, -1F, 2, 1, 2);
        end.setRotationPoint(0F, 7F, 0F);
        end.setTextureSize(32, 32);
        end.mirror = true;
        setRotation(end, 0F, 0F, 0F);
        main = new ModelRenderer(this, 0, 0);
        main.addBox(-1F, 0F, -1F, 2, 1, 9);
        main.setRotationPoint(0F, 7F, 0F);
        main.setTextureSize(32, 32);
        main.mirror = true;
        setRotation(main, 0F, 0F, 0F);
    }

    public void render(TileEntity te, float f) {

        float f5 = 0.0625F;
        setRotationAngles(te, f);
        main.render(f5);
        end.render(f5);
    }

    private static void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    /**
     * @param te
     * @param f
     */
    public void setRotationAngles(TileEntity te, float f) {}

}
