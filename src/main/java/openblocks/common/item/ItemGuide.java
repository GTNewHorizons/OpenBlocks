package openblocks.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import openblocks.shapes.GuideShape;
import openmods.item.ItemOpenBlock;

public class ItemGuide extends ItemOpenBlock {

    public static final String TAG_POS_X = "PosX";
    public static final String TAG_NEG_X = "NegX";

    public static final String TAG_POS_Y = "PosY";
    public static final String TAG_NEG_Y = "NegY";

    public static final String TAG_POS_Z = "PosZ";
    public static final String TAG_NEG_Z = "NegZ";

    public static final String TAG_COLOR = "Color";
    public static final String TAG_SHAPE = "Mode";

    // legacy tags
    public static final String TAG_WIDTH = "Width";
    public static final String TAG_HEIGHT = "Height";
    public static final String TAG_DEPTH = "Depth";

    public ItemGuide(Block block) {
        super(block);
    }

    private static void addIntInfo(NBTTagCompound tag, String name, String format, List<String> result) {
        if (tag.hasKey(name)) result.add(StatCollector.translateToLocalFormatted(format, tag.getInteger(name)));
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List result, boolean extended) {
        NBTTagCompound tag = stack.stackTagCompound;
        if (tag != null) {
            if (tag.hasKey(TAG_NEG_X) && tag.hasKey(TAG_NEG_Y) || tag.hasKey(TAG_NEG_Z)
                    || tag.hasKey(TAG_POS_X) && tag.hasKey(TAG_POS_Y)
                    || tag.hasKey(TAG_POS_Z)) {
                final int posX = tag.getInteger(TAG_POS_X);
                final int posY = tag.getInteger(TAG_POS_Y);
                final int posZ = tag.getInteger(TAG_POS_Z);

                final int negX = -tag.getInteger(TAG_NEG_X);
                final int negY = -tag.getInteger(TAG_NEG_Y);
                final int negZ = -tag.getInteger(TAG_NEG_Z);

                result.add(
                        StatCollector
                                .translateToLocalFormatted("openblocks.misc.box", negX, negY, negZ, posX, posY, posZ));
            } else {
                addIntInfo(tag, TAG_WIDTH, "openblocks.misc.width", result);
                addIntInfo(tag, TAG_HEIGHT, "openblocks.misc.height", result);
            }

            addIntInfo(tag, TAG_DEPTH, "openblocks.misc.depth", result);
            addIntInfo(tag, TAG_COLOR, "openblocks.misc.color", result);

            if (tag.hasKey(TAG_SHAPE)) {
                int mode = tag.getInteger(TAG_SHAPE);
                try {
                    GuideShape shape = GuideShape.VALUES[mode];
                    result.add(
                            StatCollector.translateToLocalFormatted("openblocks.misc.shape", shape.getLocalizedName()));
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
    }

}
