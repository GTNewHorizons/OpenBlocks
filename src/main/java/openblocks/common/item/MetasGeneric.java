package openblocks.common.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.Loader;
import openblocks.Config;
import openblocks.OpenBlocks;
import openmods.infobook.ICustomBookEntryProvider;
import openmods.item.IMetaItem;

public enum MetasGeneric {

    gliderWing {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "gliderwing",
                    new ShapedOreRecipe(result, " sl", "sll", "lll", 's', "stickWood", 'l', Items.leather),
                    new ShapedOreRecipe(result, "ls ", "lls", "lll", 's', "stickWood", 'l', Items.leather));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.hangGlider != null;
        }
    },
    beam {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack(2);
            return new MetaGeneric(
                    "beam",
                    new ShapedOreRecipe(
                            result,
                            "iii",
                            "b y",
                            "iii",
                            'i',
                            "ingotIron",
                            'b',
                            "dyeBlack",
                            'y',
                            "dyeYellow"),
                    new ShapedOreRecipe(
                            result,
                            "iii",
                            "y b",
                            "iii",
                            'i',
                            "ingotIron",
                            'b',
                            "dyeBlack",
                            'y',
                            "dyeYellow"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.craneBackpack != null;
        }
    },
    craneEngine {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "crane_engine",
                    new ShapedOreRecipe(
                            result,
                            "iii",
                            "isi",
                            "iri",
                            'i',
                            "ingotIron",
                            's',
                            "stickWood",
                            'r',
                            "dustRedstone"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.craneBackpack != null;
        }
    },
    craneMagnet {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "crane_magnet",
                    new ShapedOreRecipe(
                            result,
                            "biy",
                            "iri",
                            'i',
                            "ingotIron",
                            'r',
                            "dustRedstone",
                            'b',
                            "dyeBlack",
                            'y',
                            "dyeYellow"),
                    new ShapedOreRecipe(
                            result,
                            "yib",
                            "iri",
                            'i',
                            "ingotIron",
                            'r',
                            "dustRedstone",
                            'b',
                            "dyeBlack",
                            'y',
                            "dyeYellow"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.craneBackpack != null
                    || (Loader.isModLoaded(openmods.Mods.OPENPERIPHERALCORE) && Config.enableCraneTurtles);
        }
    },
    miracleMagnet {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            ItemStack magnet = craneMagnet.newItemStack();
            return new MetaMiracleMagnet(
                    "miracle_magnet",
                    new ShapedOreRecipe(
                            result,
                            "rer",
                            "eme",
                            "rer",
                            'r',
                            "dustRedstone",
                            'e',
                            Items.ender_pearl,
                            'm',
                            magnet),
                    new ShapedOreRecipe(
                            result,
                            "ere",
                            "rmr",
                            "ere",
                            'r',
                            "dustRedstone",
                            'e',
                            Items.ender_pearl,
                            'm',
                            magnet));
        }

        @Override
        public boolean isEnabled() {
            return Loader.isModLoaded(openmods.Mods.OPENPERIPHERALCORE) && Config.enableCraneTurtles;
        }
    },
    line {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack(2);
            return new MetaGeneric(
                    "line",
                    new ShapedOreRecipe(result, "sss", "bbb", "sss", 's', Items.string, 'b', "slimeball"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.craneBackpack != null;
        }
    },
    mapController {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack(1);
            return new MetaGeneric(
                    "map_controller",
                    new ShapedOreRecipe(result, " r ", "rgr", " r ", 'r', "dustRedstone", 'g', "ingotGold"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.emptyMap != null;
        }
    },
    mapMemory {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack(1);
            return new MetaGeneric(
                    "map_memory",
                    new ShapedOreRecipe(result, "rg", "rg", "rg", 'g', "nuggetGold", 'r', "dustRedstone"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.emptyMap != null;
        }
    },
    /**
     * Deprecated. Moved to ItemCursor Can't remove it from here because it'll re-index everyones items.. I guess the
     * next time we add a meta, replace this one.
     */
    cursor {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public IMetaItem createMetaItem() {
            return null;
        }
    },
    assistantBase {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "assistant_base",
                    new ShapedOreRecipe(
                            result,
                            "iei",
                            "iri",
                            'i',
                            "ingotIron",
                            'e',
                            Items.ender_pearl,
                            'r',
                            "dustRedstone"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Items.cartographer != null;
        }
    },
    unpreparedStencil {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "unprepared_stencil",
                    new ShapedOreRecipe(result, " p ", "pip", " p ", 'p', Items.paper, 'i', "ingotIron"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Blocks.drawingTable != null;
        }
    },
    sketchingPencil {

        @Override
        public IMetaItem createMetaItem() {
            ItemStack result = newItemStack();
            return new MetaGeneric(
                    "sketching_pencil",
                    new ShapedOreRecipe(
                            result,
                            "c  ",
                            " s ",
                            "  s",
                            'c',
                            new ItemStack(Items.coal, 1, OreDictionary.WILDCARD_VALUE),
                            's',
                            "stickWood"));
        }

        @Override
        public boolean isEnabled() {
            return OpenBlocks.Blocks.drawingTable != null;
        }
    };

    public ItemStack newItemStack(int size) {
        return new ItemStack(OpenBlocks.Items.generic, size, ordinal());
    }

    public ItemStack newItemStack() {
        return new ItemStack(OpenBlocks.Items.generic, 1, ordinal());
    }

    public boolean isA(ItemStack stack) {
        return (stack.getItem() instanceof ItemOBGeneric) && (stack.getItemDamage() == ordinal());
    }

    protected abstract IMetaItem createMetaItem();

    protected boolean isEnabled() {
        return true;
    }

    public static void registerItems() {
        for (MetasGeneric m : values())
            if (m.isEnabled()) OpenBlocks.Items.generic.registerItem(m.ordinal(), m.createMetaItem());
    }

    public static class DocProvider implements ICustomBookEntryProvider {

        @Override
        public Iterable<Entry> getBookEntries() {
            return ImmutableList.of(
                    new ICustomBookEntryProvider.Entry(
                            "unprepared_stencil",
                            MetasGeneric.unpreparedStencil.newItemStack()));
        }

    }

    public static boolean subItemEnabled() {
        for (MetasGeneric m : values()) if (m.isEnabled()) {
            return true;
        }
        return false;
    }
}
