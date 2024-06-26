package openblocks;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.base.Throwables;

import codechicken.nei.api.IConfigureNEI;
import openblocks.common.item.MetasGeneric;
import openblocks.common.item.MetasGenericUnstackable;
import openmods.Log;

public class NEIOpenBlocksConfig implements IConfigureNEI {

    private static final String API = "codechicken.nei.api.API";
    private static final String HIDE_ITEM = "hideItem";

    private Method hideItem;

    @Override
    public void loadConfig() {
        if (OpenBlocks.Blocks.canvasGlass != null) {
            API$hideItem(new ItemStack(OpenBlocks.Blocks.canvasGlass));
        }

        if (OpenBlocks.Items.heightMap != null) {
            API$hideItem(new ItemStack(OpenBlocks.Items.heightMap, 1, OreDictionary.WILDCARD_VALUE));
        }

        if (!MetasGeneric.subItemEnabled()) {
            API$hideItem(new ItemStack(OpenBlocks.Items.generic, 1, OreDictionary.WILDCARD_VALUE));
        }

        if (!MetasGenericUnstackable.subItemEnabled()) {
            API$hideItem(new ItemStack(OpenBlocks.Items.genericUnstackable, 1, OreDictionary.WILDCARD_VALUE));
        }

        Log.info("OpenBlocks NEI Integration loaded successfully");
    }

    @Override
    public String getName() {
        return "OpenBlocks-ItemHider";
    }

    @Override
    public String getVersion() {
        return "0.0";
    }

    private void API$hideItem(final ItemStack stack) {
        try {
            if (this.hideItem == null) this.hideItem = Class.forName(API).getMethod(HIDE_ITEM, ItemStack.class);
            this.hideItem.invoke(null, stack);
        } catch (final Throwable thr) {
            Throwables.propagate(thr);
        }
    }
}
