package openblocks.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import openblocks.common.HeightMapData;
import openblocks.common.MapDataManager;

public class ItemHeightMap extends Item {

    public ItemHeightMap() {
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack item, EntityPlayer player, List result, boolean extended) {
        int mapId = item.getItemDamage();
        HeightMapData data = MapDataManager.getMapData(player.worldObj, mapId);

        if (data.isValid()) {
            result.add(StatCollector.translateToLocalFormatted("openblocks.misc.map.scale.center_x", data.centerX));
            result.add(StatCollector.translateToLocalFormatted("openblocks.misc.map.scale.center_z", data.centerZ));
            result.add(StatCollector.translateToLocalFormatted("openblocks.misc.map.scale", 1 << data.scale));
        } else if (data.isEmpty()) {
            MapDataManager.requestMapData(player.worldObj, mapId);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("rawtypes")
    public void getSubItems(Item item, CreativeTabs tab, List items) {}

}
