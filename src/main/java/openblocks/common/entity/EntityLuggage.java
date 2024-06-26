package openblocks.common.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemNameTag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.google.common.base.Strings;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import openblocks.OpenBlocks;
import openblocks.OpenBlocksGuiHandler;
import openblocks.common.entity.ai.EntityAICollectItem;
import openmods.api.VisibleForDocumentation;
import openmods.inventory.GenericInventory;
import openmods.inventory.IInventoryProvider;
import openmods.inventory.legacy.ItemDistribution;

@VisibleForDocumentation
public class EntityLuggage extends EntityTameable implements IInventoryProvider, IEntityAdditionalSpawnData {

    private static final int SIZE_SPECIAL = 54;

    private static final int SIZE_NORMAL = 27;

    private static final String TAG_ITEM_TAG = "ItemTag";

    private static final String TAG_SHINY = "shiny";

    protected GenericInventory inventory = createInventory(SIZE_NORMAL);

    private GenericInventory createInventory(int size) {
        return new GenericInventory("luggage", false, size) {

            @Override
            public boolean isUseableByPlayer(EntityPlayer player) {
                return !isDead && player.getDistanceSqToEntity(EntityLuggage.this) < 64;
            }
        };
    }

    public boolean special;

    public int lastSound = 0;

    private NBTTagCompound itemTag;

    public EntityLuggage(World world) {
        super(world);
        setSize(0.5F, 0.5F);
        setAIMoveSpeed(0.7F);
        setMoveForward(0);
        setTamed(true);
        func_110163_bv(); // set persistent
        getNavigator().setAvoidsWater(true);
        getNavigator().setCanSwim(true);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIFollowOwner(this, getAIMoveSpeed(), 10.0F, 2.0F));
        this.tasks.addTask(3, new EntityAICollectItem(this));
        this.dataWatcher.addObject(18, Integer.valueOf(inventory.getSizeInventory()));
    }

    public void setSpecial() {
        if (special) return;
        special = true;
        GenericInventory inventory = createInventory(SIZE_SPECIAL);
        inventory.copyFrom(this.inventory);
        if (this.dataWatcher != null) {
            this.dataWatcher.updateObject(18, Integer.valueOf(inventory.getSizeInventory()));
        }
        this.inventory = inventory;
    }

    public boolean isSpecial() {
        if (worldObj.isRemote) {
            return inventory.getSizeInventory() > SIZE_NORMAL;
        }
        return special;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote) {
            int inventorySize = dataWatcher.getWatchableObjectInt(18);
            if (inventory.getSizeInventory() != inventorySize) {
                inventory = createInventory(inventorySize);
            }
        }
        lastSound++;
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    public GenericInventory getInventory() {
        return inventory;
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return convertToItem();
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entityageable) {
        return null;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (!isDead) {
            final ItemStack heldItem = player.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemNameTag) return false;

            if (worldObj.isRemote) {
                if (player.isSneaking()) spawnPickupParticles();
            } else {
                if (player.isSneaking()) {
                    ItemStack luggageItem = convertToItem();
                    if (player.inventory.addItemStackToInventory(luggageItem)) setDead();
                    playSound("random.pop", 0.5f, worldObj.rand.nextFloat() * 0.1f + 0.9f);

                } else {
                    playSound("random.chestopen", 0.5f, worldObj.rand.nextFloat() * 0.1f + 0.9f);
                    player.openGui(
                            OpenBlocks.instance,
                            OpenBlocksGuiHandler.GuiId.luggage.ordinal(),
                            player.worldObj,
                            getEntityId(),
                            0,
                            0);
                }
            }
        }
        return true;
    }

    protected void spawnPickupParticles() {
        final double py = this.posY + this.height;
        for (int i = 0; i < 50; i++) {
            double vx = rand.nextGaussian() * 0.02D;
            double vz = rand.nextGaussian() * 0.02D;
            double px = this.posX + this.width * this.rand.nextFloat();
            double pz = this.posZ + this.width * this.rand.nextFloat();
            this.worldObj.spawnParticle("portal", px, py, pz, vx, -1, vz);
        }
    }

    protected ItemStack convertToItem() {
        ItemStack luggageItem = new ItemStack(OpenBlocks.Items.luggage);
        NBTTagCompound tag = itemTag != null ? (NBTTagCompound) itemTag.copy() : new NBTTagCompound();

        inventory.writeToNBT(tag);
        luggageItem.setTagCompound(tag);

        String nameTag = getCustomNameTag();
        if (!Strings.isNullOrEmpty(nameTag)) luggageItem.setStackDisplayName(nameTag);
        return luggageItem;
    }

    public void restoreFromStack(ItemStack stack) {
        final NBTTagCompound tag = stack.getTagCompound();

        if (tag != null) {
            getInventory().readFromNBT(tag);
            if (getInventory().getSizeInventory() > SIZE_NORMAL) setSpecial();

            NBTTagCompound tagCopy = (NBTTagCompound) tag.copy();
            tagCopy.removeTag(GenericInventory.TAG_SIZE);
            tagCopy.removeTag(GenericInventory.TAG_ITEMS);
            this.itemTag = tagCopy.hasNoTags() ? null : tagCopy;
        }

        if (stack.hasDisplayName()) setCustomNameTag(stack.getDisplayName());
    }

    public boolean canConsumeStackPartially(ItemStack stack) {
        return ItemDistribution.testInventoryInsertion(inventory, stack) > 0;
    }

    @Override
    protected void func_145780_a(int x, int y, int z, Block block) {
        playSound("openblocks:luggage.walk", 0.3F, 0.7F + (worldObj.rand.nextFloat() * 0.5f));
    }

    public void storeItemTag(NBTTagCompound itemTag) {
        this.itemTag = itemTag;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean(TAG_SHINY, special);
        inventory.writeToNBT(tag);
        if (itemTag != null) tag.setTag(TAG_ITEM_TAG, itemTag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.getBoolean(TAG_SHINY)) setSpecial();
        inventory.readFromNBT(tag);
        this.itemTag = tag.hasKey(TAG_ITEM_TAG, Constants.NBT.TAG_COMPOUND) ? tag.getCompoundTag(TAG_ITEM_TAG) : null;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightning) {
        setSpecial();
    }

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    @Override
    public void setHealth(float health) {
        // NO-OP
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeInt(inventory.getSizeInventory());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        inventory = createInventory(data.readInt());
    }

    @Override
    public double getMountedYOffset() {
        return 0.825;
    }
}
