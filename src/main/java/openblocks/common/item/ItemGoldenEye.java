package openblocks.common.item;

import java.util.List;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.google.common.base.Strings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.entity.EntityGoldenEye;
import openmods.Log;
import openmods.utils.ItemUtils;
import openmods.world.StructureRegistry;

public class ItemGoldenEye extends Item {

    public static final int MAX_DAMAGE = 100;
    private static final String TAG_STRUCTURE = "Structure";

    public ItemGoldenEye() {
        setMaxDamage(MAX_DAMAGE);
        setCreativeTab(OpenBlocks.tabOpenBlocks);
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world instanceof WorldServer && player instanceof EntityPlayerMP) {
            EntityPlayerMP betterPlayer = (EntityPlayerMP) player;
            if (player.isSneaking()) tryLearnStructure(stack, (WorldServer) world, betterPlayer);
            else {
                if (trySpawnEntity(stack, (WorldServer) world, betterPlayer)) stack.stackSize = 0;
            }
        }

        return stack;
    }

    private static void tryLearnStructure(ItemStack stack, WorldServer world, EntityPlayerMP player) {
        Map<String, ChunkPosition> nearbyStructures = StructureRegistry.instance
                .getNearestStructures(world, (int) player.posX, (int) player.posY, (int) player.posZ);

        String structureName = "";
        double max = Double.MAX_VALUE;

        for (Map.Entry<String, ChunkPosition> e : nearbyStructures.entrySet()) {
            ChunkPosition pos = e.getValue();
            if (Config.eyeDebug) player.addChatMessage(
                    new ChatComponentTranslation(
                            "openblocks.misc.structure_pos",
                            e.getKey(),
                            pos.chunkPosX,
                            pos.chunkPosY,
                            pos.chunkPosZ));

            double dx = pos.chunkPosX - player.posX;
            double dy = pos.chunkPosY - player.posY;
            double dz = pos.chunkPosZ - player.posZ;

            double dist = (dx * dx) + (dy * dy) + (dz * dz);

            if (dist < max) {
                max = dist;
                structureName = e.getKey();
            }
        }

        if (!Strings.isNullOrEmpty(structureName)) {
            Log.info("Learned structure %s, d = %f", structureName, max);
            NBTTagCompound tag = ItemUtils.getItemTag(stack);
            tag.setString(TAG_STRUCTURE, structureName);
        }
    }

    private static boolean trySpawnEntity(ItemStack stack, WorldServer world, EntityPlayerMP player) {
        int damage = stack.getItemDamage();
        if (damage >= stack.getMaxDamage()) return false;

        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        String structureName = tag.getString(TAG_STRUCTURE);

        if (Strings.isNullOrEmpty(structureName)) return false;

        Map<String, ChunkPosition> nearbyStructures = StructureRegistry.instance
                .getNearestStructures(world, (int) player.posX, (int) player.posY, (int) player.posZ);

        ChunkPosition structurePos = nearbyStructures.get(structureName);
        if (structurePos != null) {
            if (Config.eyeDebug) player.addChatComponentMessage(
                    new ChatComponentTranslation(
                            "openblocks.misc.structure_pos",
                            structureName,
                            structurePos.chunkPosX,
                            structurePos.chunkPosY,
                            structurePos.chunkPosZ));

            stack.setItemDamage(damage + 1);
            EntityGoldenEye eye = new EntityGoldenEye(world, stack, player, structurePos);
            world.spawnEntityInWorld(eye);
            world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List result) {
        result.add(new ItemStack(item, 1, 0));
        result.add(new ItemStack(item, 1, getMaxDamage()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List result, boolean expanded) {
        NBTTagCompound tag = ItemUtils.getItemTag(stack);
        if (tag.hasKey(TAG_STRUCTURE)) result.add(StatCollector.translateToLocal("openblocks.misc.locked"));
    }

}
