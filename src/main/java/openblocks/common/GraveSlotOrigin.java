package openblocks.common;

import net.minecraft.nbt.NBTTagCompound;

public record GraveSlotOrigin(String inventoryType, int slot) {

    public static final String INV_MAIN = "main";
    public static final String INV_ARMOR = "armor";
    public static final String INV_ADVENTURE_BACKPACK = "adventurebackpack";
    public static final String INV_BAUBLES = "baubles";
    public static final String INV_COSMETIC_ARMOR = "cosmeticarmor";
    public static final String INV_GALACTICRAFT = "galacticraft";
    public static final String INV_MC_BACKPACK = "mcbackpack";
    public static final String INV_TCONSTRUCT = "tconstruct";

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("inv", inventoryType);
        tag.setInteger("slot", slot);
        return tag;
    }

    public static GraveSlotOrigin fromNBT(NBTTagCompound tag) {
        return new GraveSlotOrigin(tag.getString("inv"), tag.getInteger("slot"));
    }
}
