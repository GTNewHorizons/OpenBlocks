package openblocks.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Objects;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import openblocks.Config;
import openblocks.common.tileentity.TileEntityImaginary;
import openblocks.common.tileentity.TileEntityImaginary.ICollisionData;
import openblocks.common.tileentity.TileEntityImaginary.PanelData;
import openblocks.common.tileentity.TileEntityImaginary.StairsData;
import openmods.item.ItemOpenBlock;
import openmods.utils.BlockUtils;
import openmods.utils.ColorUtils;
import openmods.utils.ColorUtils.ColorMeta;
import openmods.utils.ItemUtils;

public class ItemImaginary extends ItemOpenBlock {

    public static final float CRAFTING_COST = 1.0f;
    public static final String TAG_COLOR = "Color";
    public static final String TAG_USES = "Uses";
    public static final String TAG_MODE = "Mode";

    public static final int DAMAGE_PENCIL = 0;
    public static final int DAMAGE_CRAYON = 1;

    private enum PlacementMode {

        BLOCK(1.0f, "block", "overlay_block", false) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return TileEntityImaginary.DUMMY;
            }
        },
        PANEL(0.5f, "panel", "overlay_panel", false) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return new PanelData(1.0f);
            }
        },
        HALF_PANEL(0.5f, "half_panel", "overlay_half", false) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return new PanelData(0.5f);
            }
        },
        STAIRS(0.75f, "stairs", "overlay_stairs", false) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                ForgeDirection dir = BlockUtils.get2dOrientation(player);
                return new StairsData(0.5f, 1.0f, dir);
            }
        },

        INV_BLOCK(1.5f, "inverted_block", "overlay_inverted_block", true) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return TileEntityImaginary.DUMMY;
            }
        },
        INV_PANEL(1.0f, "inverted_panel", "overlay_inverted_panel", true) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return new PanelData(1.0f);
            }
        },
        INV_HALF_PANEL(1.0f, "inverted_half_panel", "overlay_inverted_half", true) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                return new PanelData(0.5f);
            }
        },
        INV_STAIRS(1.25f, "inverted_stairs", "overlay_inverted_stairs", true) {

            @Override
            public ICollisionData createCollisionData(ItemStack stack, EntityPlayer player) {
                ForgeDirection dir = BlockUtils.get2dOrientation(player);
                return new StairsData(0.5f, 1.0f, dir);
            }
        };

        public final float cost;
        public final String name;
        public final String overlayName;
        public final boolean isInverted;
        public IIcon overlay;

        private PlacementMode(float cost, String name, String overlayName, boolean isInverted) {
            this.cost = cost;
            this.name = "openblocks.misc.mode." + name;
            this.overlayName = "openblocks:" + overlayName;
            this.isInverted = isInverted;
        }

        public abstract ICollisionData createCollisionData(ItemStack stack, EntityPlayer player);

        public static final PlacementMode[] VALUES = values();
    }

    public static float getUses(NBTTagCompound tag) {
        NBTBase value = tag.getTag(TAG_USES);
        if (value == null) return 0;
        if (value instanceof NBTPrimitive) return ((NBTPrimitive) value).func_150288_h();

        throw new IllegalStateException("Invalid tag type: " + value);
    }

    public static float getUses(ItemStack stack) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        return getUses(tag);
    }

    public static PlacementMode getMode(NBTTagCompound tag) {
        int value = tag.getByte(TAG_MODE);
        return PlacementMode.VALUES[value];
    }

    public static PlacementMode getMode(ItemStack stack) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        return getMode(tag);
    }

    public static boolean isCrayon(ItemStack stack) {
        return stack.getItemDamage() == DAMAGE_CRAYON;
    }

    public ItemImaginary(Block block) {
        super(block);
        setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    public static ItemStack setupValues(Integer color, ItemStack result) {
        return setupValues(color, result, Config.imaginaryItemUseCount);
    }

    public static ItemStack setupValues(Integer color, ItemStack result, float uses) {
        NBTTagCompound tag = ItemUtils.getItemTag(result);

        if (color != null) {
            tag.setInteger(TAG_COLOR, color);
            result.setItemDamage(DAMAGE_CRAYON);
        }

        tag.setFloat(TAG_USES, uses);
        return result;
    }

    @Override
    protected void afterBlockPlaced(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);

        NBTTagInt color = (NBTTagInt) tag.getTag(TAG_COLOR);
        PlacementMode mode = getMode(tag);
        ICollisionData collisions = mode.createCollisionData(stack, player);
        world.setTileEntity(
                x,
                y,
                z,
                new TileEntityImaginary(color == null ? null : color.func_150287_d(), mode.isInverted, collisions));

        if (!player.capabilities.isCreativeMode) {
            float uses = Math.max(getUses(tag) - mode.cost, 0);
            tag.setFloat(TAG_USES, uses);

            if (uses <= 0) stack.stackSize = 0;
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
        if (stack == null) return false;

        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        float uses = getUses(tag);
        if (uses <= 0) {
            stack.stackSize = 0;
            return true;
        }

        if (uses < getMode(tag).cost) return false;

        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        return tag.hasKey(TAG_COLOR) ? "item.openblocks.crayon" : "item.openblocks.pencil";
    }

    @Override
    public String getUnlocalizedName() {
        return "item.openblocks.imaginary";
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List result, boolean extended) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);

        result.add(StatCollector.translateToLocalFormatted("openblocks.misc.uses", getUses(tag)));

        NBTTagInt color = (NBTTagInt) tag.getTag(TAG_COLOR);
        if (color != null)
            result.add(StatCollector.translateToLocalFormatted("openblocks.misc.color", color.func_150287_d()));

        PlacementMode mode = getMode(tag);
        String translatedMode = StatCollector.translateToLocal(mode.name);
        result.add(StatCollector.translateToLocalFormatted("openblocks.misc.mode", translatedMode));
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubItems(Item item, CreativeTabs tab, List result) {
        result.add(setupValues(null, new ItemStack(this, 1, DAMAGE_PENCIL)));
        for (ColorMeta color : ColorUtils.getAllColors())
            result.add(setupValues(color.rgb, new ItemStack(this, 1, DAMAGE_CRAYON)));
    }

    @Override
    public int getSpriteNumber() {
        return 1; // render as item
    }

    private IIcon iconCrayonBackground;
    private IIcon iconCrayonColor;
    private IIcon iconPencil;
    private IIcon iconBlank;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister registry) {
        iconCrayonBackground = registry.registerIcon("openblocks:crayon_1");
        iconCrayonColor = registry.registerIcon("openblocks:crayon_2");
        iconPencil = registry.registerIcon("openblocks:pencil");
        iconBlank = registry.registerIcon("openblocks:blank");

        for (PlacementMode mode : PlacementMode.VALUES) mode.overlay = registry.registerIcon(mode.overlayName);
    }

    private IIcon getOverlayIcon(ItemStack stack, boolean inGui) {
        return inGui ? getMode(stack).overlay : iconBlank;
    }

    private IIcon getIcon(ItemStack stack, int pass, boolean inGui) {
        if (!isCrayon(stack)) return pass == 1 ? getOverlayIcon(stack, inGui) : iconPencil;

        switch (pass) {
            case 0:
                return iconCrayonBackground;
            case 1:
                return iconCrayonColor;
            case 2:
                return getOverlayIcon(stack, inGui);
        }

        throw new IllegalArgumentException("Invalid pass: " + pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final IIcon getIcon(ItemStack stack, int pass) {
        return getIcon(stack, pass, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        return getIcon(stack, pass, false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        if (isCrayon(stack) && pass == 1) return Objects.firstNonNull(ItemUtils.getInt(stack, TAG_COLOR), 0x000000);

        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return metadata == DAMAGE_CRAYON ? 3 : 2;
    }

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        float uses = getUses(tag) - CRAFTING_COST;
        if (uses <= 0) return null;

        ItemStack copy = stack.copy();
        NBTTagCompound copyTag = ItemUtils.getItemTag(copy);
        copyTag.setFloat(TAG_USES, uses);
        return copy;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        if (getUses(tag) <= 0) {
            stack.stackSize = 0;
        } else if (player.isSneaking()) {
            byte modeId = tag.getByte(TAG_MODE);
            modeId = (byte) ((modeId + 1) % PlacementMode.VALUES.length);
            tag.setByte(TAG_MODE, modeId);

            if (world.isRemote) {
                PlacementMode mode = PlacementMode.VALUES[modeId];
                ChatComponentTranslation modeName = new ChatComponentTranslation(mode.name);
                player.addChatComponentMessage(new ChatComponentTranslation("openblocks.misc.mode", modeName));
            }
        }

        return stack;
    }
}
