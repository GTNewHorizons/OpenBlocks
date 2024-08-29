package openblocks.common;

import static com.kuba6000.mobsinfo.api.VillagerTrade.create;
import static com.kuba6000.mobsinfo.api.VillagerTrade.createItem;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.oredict.OreDictionary;

import com.kuba6000.mobsinfo.api.IVillagerInfoProvider;
import com.kuba6000.mobsinfo.api.VillagerTrade;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
import openblocks.Config;

@Optional.Interface(iface = "com.kuba6000.mobsinfo.api.IVillagerInfoProvider", modid = "mobsinfo")
public class RadioVillagerTradeManager implements IVillageTradeHandler, IVillagerInfoProvider {

    private static ItemStack randomItemAmount(Random random, Item item, int min, int max) {
        int amount = random.nextInt(max - min) + min;
        return new ItemStack(item, amount);
    }

    private static ItemStack randomEmeralds(Random random, int min, int max) {
        return randomItemAmount(random, Items.emerald, min, max);
    }

    @Override
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
        if (Config.radioVillagerRecords) {
            for (ItemStack record : OreDictionary.getOres("record")) if (random.nextFloat() < 0.01)
                recipeList.addToListWithCheck(new MerchantRecipe(randomEmeralds(random, 7, 15), record));
        }

        if (random.nextFloat() > 0.5) recipeList
                .addToListWithCheck(new MerchantRecipe(randomEmeralds(random, 1, 2), new ItemStack(Blocks.noteblock)));

        if (random.nextFloat() > 0.25 || recipeList.isEmpty()) recipeList
                .addToListWithCheck(new MerchantRecipe(randomEmeralds(random, 3, 7), new ItemStack(Blocks.jukebox)));
    }

    @Optional.Method(modid = "mobsinfo")
    @Override
    public void provideTrades(@Nonnull EntityVillager villager, int profession,
            @Nonnull ArrayList<VillagerTrade> trades) {
        if (Config.radioVillagerRecords) {
            for (ItemStack record : OreDictionary.getOres("record")) {
                trades.add(
                        create(createItem(Items.emerald).withPossibleSizes(7, 15), createItem(record))
                                .withChance(0.01d));
            }
        }
        trades.add(
                create(
                        createItem(Items.emerald).withPossibleSizes(1, 2),
                        createItem(Item.getItemFromBlock(Blocks.noteblock))).withChance(0.5d));
        trades.add(
                create(
                        createItem(Items.emerald).withPossibleSizes(3, 7),
                        createItem(Item.getItemFromBlock(Blocks.jukebox))).withChance(0.25d));
    }
}
