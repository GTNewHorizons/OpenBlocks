package openblocks.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import openblocks.OpenBlocks;
import openblocks.client.gui.GuiInfoBook;

public class ItemInfoBook extends Item {

    public ItemInfoBook() {
        setCreativeTab(OpenBlocks.tabOpenBlocks);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) FMLCommonHandler.instance().showGuiScreen(new GuiInfoBook());
        return stack;
    }
}
