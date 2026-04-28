package openblocks.common.tileentity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.FakePlayer;

import com.google.common.base.Strings;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import openblocks.Config;
import openblocks.common.GraveAutoEquip;
import openblocks.common.GraveSlotOrigin;
import openmods.api.IActivateAwareTile;
import openmods.api.IAddAwareTile;
import openmods.api.INeighbourAwareTile;
import openmods.api.IPlacerAwareTile;
import openmods.inventory.GenericInventory;
import openmods.inventory.IInventoryProvider;
import openmods.sync.SyncableBoolean;
import openmods.sync.SyncableString;
import openmods.tileentity.SyncedTileEntity;
import openmods.utils.BlockUtils;

public class TileEntityGrave extends SyncedTileEntity
        implements IPlacerAwareTile, IInventoryProvider, INeighbourAwareTile, IActivateAwareTile, IAddAwareTile {

    private static final String TAG_MESSAGE = "Message";
    private static final String TAG_ORIGINS = "SlotOrigins";
    private SyncableString perishedUsername;
    public SyncableBoolean onSoil;
    private SyncableBoolean inventoryEmpty;

    private IChatComponent deathMessage;

    /** grave slot index → origin slot at death; may be empty if grave predates this feature */
    private final Map<Integer, GraveSlotOrigin> slotOrigins = new HashMap<Integer, GraveSlotOrigin>();

    private final GenericInventory inventory = registerInventoryCallback(new GenericInventory("grave", false, 1));

    public TileEntityGrave() {}

    @Override
    protected void createSyncedFields() {
        perishedUsername = new SyncableString();
        onSoil = new SyncableBoolean(true);
        inventoryEmpty = new SyncableBoolean(false);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!worldObj.isRemote) {
            if (Config.spawnSkeletons && worldObj.difficultySetting != EnumDifficulty.PEACEFUL
                    && worldObj.rand.nextDouble() < Config.skeletonSpawnRate) {
                List<IMob> mobs = worldObj.getEntitiesWithinAABB(IMob.class, getBB().expand(7, 7, 7));
                if (mobs.size() < 5) {
                    double chance = worldObj.rand.nextDouble();
                    EntityLiving living = chance < 0.5 ? new EntitySkeleton(worldObj) : new EntityBat(worldObj);
                    living.setPositionAndRotation(
                            xCoord + 0.5,
                            yCoord + 0.5,
                            zCoord + 0.5,
                            worldObj.rand.nextFloat() * 360,
                            0);
                    if (living.getCanSpawnHere()) {
                        worldObj.spawnEntityInWorld(living);
                    }
                }
            }
        }
    }

    public String getUsername() {
        return perishedUsername.getValue();
    }

    public void setDeathMessage(IChatComponent msg) {
        deathMessage = msg.createCopy();
    }

    public void setUsername(String username) {
        this.perishedUsername.setValue(username);
    }

    public void setSlotOrigins(Map<Integer, GraveSlotOrigin> origins) {
        slotOrigins.clear();
        slotOrigins.putAll(origins);
    }

    public void setLoot(IInventory invent) {
        inventory.clearAndSetSlotCount(invent.getSizeInventory());
        inventory.copyFrom(invent);
        refreshEmptyFlag();
    }

    public boolean isOnSoil() {
        return onSoil.get();
    }

    @Override
    public void onBlockPlacedBy(EntityLivingBase placer, ItemStack stack) {
        if (!worldObj.isRemote) {
            if ((placer instanceof EntityPlayer player) && !(placer instanceof FakePlayer)) {

                if (stack.hasDisplayName()) setUsername(stack.getDisplayName());
                else setUsername(player.getGameProfile().getName());
                if (player.capabilities.isCreativeMode) setLoot(player.inventory);
                updateBlockBelow();
                sync();
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        inventory.writeToNBT(tag);

        if (deathMessage != null) {
            String serialized = IChatComponent.Serializer.func_150696_a(deathMessage);
            tag.setString(TAG_MESSAGE, serialized);
        }

        if (!slotOrigins.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<Integer, GraveSlotOrigin> entry : slotOrigins.entrySet()) {
                NBTTagCompound entry_tag = entry.getValue().toNBT();
                entry_tag.setInteger("graveSlot", entry.getKey());
                list.appendTag(entry_tag);
            }
            tag.setTag(TAG_ORIGINS, list);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.readFromNBT(tag);

        slotOrigins.clear();
        if (tag.hasKey(TAG_ORIGINS)) {
            NBTTagList list = tag.getTagList(TAG_ORIGINS, 10); // 10 = TAG_Compound
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry_tag = list.getCompoundTagAt(i);
                int graveSlot = entry_tag.getInteger("graveSlot");
                slotOrigins.put(graveSlot, GraveSlotOrigin.fromNBT(entry_tag));
            }
        }

        String serializedMsg = tag.getString(TAG_MESSAGE);

        if (!Strings.isNullOrEmpty(serializedMsg)) {
            deathMessage = IChatComponent.Serializer.func_150699_a(serializedMsg);
        }
    }

    @Override
    public IInventory getInventory() {
        return inventory;
    }

    protected void updateBlockBelow() {
        Block block = worldObj.getBlock(xCoord, yCoord - 1, zCoord);
        onSoil.set(block == Blocks.dirt || block == Blocks.grass);
    }

    @Override
    public void onAdded() {
        updateBlockBelow();
    }

    @Override
    public void onNeighbourChanged(Block block) {
        updateBlockBelow();
        sync();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    public boolean isOwner(EntityPlayer player) {
        return player.getGameProfile().getName().equals(perishedUsername.getValue());
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player.worldObj.isRemote) return false;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem().getToolClasses(held).contains("shovel")) {
            robGrave(player, held);
            return true;
        }

        if (held == null && player.isSneaking()) {
            if (isOwner(player)) {
                autoEquipAll(player);
            } else {
                player.addChatMessage(new ChatComponentTranslation("openblocks.misc.grave_not_owner"));
            }
            return true;
        }

        if (deathMessage != null) {
            player.addChatMessage(deathMessage.createCopy());
        }
        return true;
    }

    public boolean isInventoryEmpty() {
        return inventoryEmpty.get();
    }

    private void refreshEmptyFlag() {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i) != null) {
                inventoryEmpty.set(false);
                return;
            }
        }
        inventoryEmpty.set(true);
    }

    public void autoEquipAll(EntityPlayer player) {
        int equipped = 0;
        int toInventory = 0;
        int leftover = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;
            ItemStack copy = stack.copy();
            GraveSlotOrigin origin = slotOrigins.get(i);
            boolean restoredToOrigin = origin != null && GraveAutoEquip.tryRestoreToOrigin(player, copy, origin);
            ItemStack remainder = restoredToOrigin ? null : GraveAutoEquip.tryEquipOrDrop(player, copy);
            if (remainder == null) {
                inventory.setInventorySlotContents(i, null);
                equipped++;
            } else if (player.inventory.addItemStackToInventory(copy)) {
                inventory.setInventorySlotContents(i, null);
                toInventory++;
            } else {
                // copy may have been partially consumed — write back the actual remainder
                inventory.setInventorySlotContents(i, copy.stackSize > 0 ? copy : null);
                leftover++;
            }
        }
        refreshEmptyFlag();
        if (equipped > 0 || toInventory > 0) {
            markDirty();
            sync();
            player.inventory.markDirty();
            if (player instanceof EntityPlayerMP) {
                player.inventoryContainer.detectAndSendChanges();
            }
        }
        if (equipped > 0) {
            player.addChatMessage(new ChatComponentTranslation("openblocks.misc.grave_equipped", equipped));
        }
        if (toInventory > 0) {
            player.addChatMessage(new ChatComponentTranslation("openblocks.misc.grave_to_inventory", toInventory));
        }
        if (leftover > 0) {
            player.addChatMessage(new ChatComponentTranslation("openblocks.misc.grave_skipped", leftover));
        }
        if (leftover == 0 && (equipped > 0 || toInventory > 0)) {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
    }

    protected void robGrave(EntityPlayer player, ItemStack held) {
        boolean dropped = false;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                dropped = true;
                ItemStack remainder = GraveAutoEquip.tryEquipOrDrop(player, stack);
                if (remainder != null) {
                    BlockUtils.dropItemStackInWorld(worldObj, xCoord, yCoord, zCoord, remainder);
                }
            }
        }

        inventory.clearAndSetSlotCount(0);

        if (dropped) {
            worldObj.playAuxSFXAtEntity(null, 2001, xCoord, yCoord, zCoord, Block.getIdFromBlock(Blocks.dirt));
            if (worldObj.rand.nextDouble() < Config.graveSpecialAction) ohNoes(player);
            held.damageItem(2, player);
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
    }

    private void ohNoes(EntityPlayer player) {
        worldObj.playSoundAtEntity(player, "openblocks:grave.rob", 1, 1);

        final WorldInfo worldInfo = worldObj.getWorldInfo();
        worldInfo.setThunderTime(35 * 20);
        worldInfo.setRainTime(35 * 20);
        worldInfo.setThundering(true);
        worldInfo.setRaining(true);
    }

}
