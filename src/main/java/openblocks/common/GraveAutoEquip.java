package openblocks.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.item.IBackWearableItem;
import com.darkona.adventurebackpack.playerProperties.BackpackProperty;

import baubles.api.BaublesApi;
import baubles.api.IBauble;
import de.eydamos.backpack.item.ItemBackpackBase;
import de.eydamos.backpack.saves.PlayerSave;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.inventory.InventoryCosArmor;
import micdoodle8.mods.galacticraft.api.item.IItemThermal;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.inventory.InventoryExtended;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.items.ItemOxygenMask;
import micdoodle8.mods.galacticraft.core.items.ItemOxygenTank;
import micdoodle8.mods.galacticraft.core.items.ItemParaChute;
import openmods.Log;
import tconstruct.armor.player.ArmorExtended;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.library.accessory.IAccessory;
import tconstruct.util.config.PHConstruct;

public class GraveAutoEquip {

    /**
     * Tries to restore the stack to the exact slot it came from at death. Returns true if restored.
     */
    public static boolean tryRestoreToOrigin(EntityPlayer player, ItemStack stack, GraveSlotOrigin origin) {
        if (stack == null || origin == null) return false;
        try {
            return switch (origin.inventoryType()) {
                case GraveSlotOrigin.INV_MAIN -> restoreToMain(player, stack, origin.slot());
                case GraveSlotOrigin.INV_ARMOR -> restoreToArmor(player, stack, origin.slot());
                case GraveSlotOrigin.INV_ADVENTURE_BACKPACK -> restoreToAdventureBackpack(player, stack);
                case GraveSlotOrigin.INV_BAUBLES -> restoreToBaubles(player, stack, origin.slot());
                case GraveSlotOrigin.INV_COSMETIC_ARMOR -> restoreToCosmeticArmor(player, stack, origin.slot());
                case GraveSlotOrigin.INV_GALACTICRAFT -> restoreToGalacticraft(player, stack, origin.slot());
                case GraveSlotOrigin.INV_MC_BACKPACK -> restoreToMcBackpack(player, stack);
                case GraveSlotOrigin.INV_TCONSTRUCT -> restoreToTConstruct(player, stack, origin.slot());
                default -> false;
            };
        } catch (Exception e) {
            Log.warn(
                    "GraveAutoEquip: error restoring %s to origin %s/%d: %s",
                    stack.getDisplayName(),
                    origin.inventoryType(),
                    origin.slot(),
                    e);
            return false;
        }
    }

    private static boolean restoreToMain(EntityPlayer player, ItemStack stack, int slot) {
        if (slot < 0 || slot >= player.inventory.mainInventory.length) return false;
        if (player.inventory.mainInventory[slot] != null) return false;
        player.inventory.mainInventory[slot] = stack.copy();
        return true;
    }

    private static boolean restoreToArmor(EntityPlayer player, ItemStack stack, int slot) {
        if (slot < 0 || slot >= player.inventory.armorInventory.length) return false;
        if (player.inventory.armorInventory[slot] != null) return false;
        player.inventory.armorInventory[slot] = stack.copy();
        return true;
    }

    private static boolean restoreToBaubles(EntityPlayer player, ItemStack stack, int slot) {
        if (!ModPresence.BAUBLES) return false;
        return BaublesRestoreHelper.restore(player, stack, slot);
    }

    private static final class BaublesRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            IInventory inv = BaublesApi.getBaubles(player);
            if (inv == null) return false;
            if (slot < 0 || slot >= inv.getSizeInventory()) return false;
            if (inv.getStackInSlot(slot) != null) return false;
            inv.setInventorySlotContents(slot, stack.copy());
            return true;
        }
    }

    private static boolean restoreToAdventureBackpack(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.ADVENTURE_BACKPACK) return false;
        return AdventureBackpackRestoreHelper.restore(player, stack);
    }

    private static final class AdventureBackpackRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof IBackWearableItem)) return false;
            BackpackProperty prop = BackpackProperty.get(player);
            if (prop == null || prop.getWearable() != null) return false;
            prop.setWearable(stack.copy());
            ((IBackWearableItem) stack.getItem()).onEquipped(player.worldObj, player, stack);
            BackpackProperty.sync(player);
            return true;
        }
    }

    private static boolean restoreToCosmeticArmor(EntityPlayer player, ItemStack stack, int slot) {
        if (!ModPresence.COSMETIC_ARMOR) return false;
        return CosmeticArmorRestoreHelper.restore(player, stack, slot);
    }

    private static final class CosmeticArmorRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            InventoryCosArmor inv = CosmeticArmorReworked.invMan.getCosArmorInventory(player.getUniqueID());
            if (inv.getStackInSlot(slot) != null) return false;
            inv.setInventorySlotContents(slot, stack.copy());
            inv.markDirty();
            return true;
        }
    }

    private static boolean restoreToMcBackpack(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.MC_BACKPACK) return false;
        return McBackpackRestoreHelper.restore(player, stack);
    }

    private static final class McBackpackRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof ItemBackpackBase)) return false;
            PlayerSave save = new PlayerSave(player);
            if (save.hasPersonalBackpack()) return false;
            save.setPersonalBackpack(stack.copy());
            return true;
        }
    }

    /**
     * Tries to equip the stack into the appropriate slot on the player. Returns null if equipped, or the original stack
     * if it could not be equipped. Order: vanilla armor → Baubles → Adventure Backpack → Minecraft Backpack.
     */
    public static ItemStack tryEquipOrDrop(EntityPlayer player, ItemStack stack) {
        if (stack == null) return null;

        try {
            if (tryEquipTConstructAccessory(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping tconstruct accessory %s: %s", stack.getDisplayName(), e);
        }

        try {
            if (tryEquipVanillaArmor(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping vanilla armor %s: %s", stack.getDisplayName(), e);
        }

        try {
            if (tryEquipBauble(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping bauble %s: %s", stack.getDisplayName(), e);
        }

        try {
            if (tryEquipAdventureBackpack(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping adventure backpack %s: %s", stack.getDisplayName(), e);
        }

        try {
            if (tryEquipMcBackpack(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping mc backpack %s: %s", stack.getDisplayName(), e);
        }

        try {
            if (tryEquipGalacticraft(player, stack)) return null;
        } catch (Exception e) {
            Log.warn("GraveAutoEquip: error equipping galacticraft gear %s: %s", stack.getDisplayName(), e);
        }

        return stack;
    }

    // -------------------------------------------------------------------------
    // Tinkers' Construct accessories (soft dependency) — skipped when tab is disabled
    // -------------------------------------------------------------------------

    private static boolean isTConstructTabEnabled() {
        if (!ModPresence.TCONSTRUCT) return false;
        return TConstructConfigHelper.isTabEnabled();
    }

    private static final class TConstructConfigHelper {

        static boolean isTabEnabled() {
            try {
                // enableTinkerInventoryTab is available only since TConstruct 1.14.72-GTNH
                return PHConstruct.enableTinkerInventoryTab;
            } catch (NoSuchFieldError e) {
                return true;
            }
        }
    }

    private static boolean tryEquipTConstructAccessory(EntityPlayer player, ItemStack stack) {
        if (!isTConstructTabEnabled()) return false;
        return TConstructAccessoryHelper.equip(player, stack);
    }

    private static final class TConstructAccessoryHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof IAccessory)) return false;
            IAccessory accessory = (IAccessory) stack.getItem();
            TPlayerStats stats = TPlayerStats.get(player);
            if (stats == null) return false;
            ArmorExtended armor = stats.armor;
            for (int i = 0; i < armor.getSizeInventory(); i++) {
                if (armor.getStackInSlot(i) == null && accessory.canEquipAccessory(stack, i)) {
                    armor.setInventorySlotContents(i, stack.copy());
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean restoreToTConstruct(EntityPlayer player, ItemStack stack, int slot) {
        if (!isTConstructTabEnabled()) return false;
        return TConstructRestoreHelper.restore(player, stack, slot);
    }

    private static final class TConstructRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            TPlayerStats stats = TPlayerStats.get(player);
            if (stats == null) return false;
            ArmorExtended armor = stats.armor;
            if (slot < 0 || slot >= armor.getSizeInventory()) return false;
            if (armor.getStackInSlot(slot) != null) return false;
            armor.setInventorySlotContents(slot, stack.copy());
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // Vanilla armor
    // -------------------------------------------------------------------------

    private static boolean tryEquipVanillaArmor(EntityPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemArmor)) return false;
        // armorType: 0=helmet,1=chest,2=legs,3=boots; armorInventory: 0=boots,1=legs,2=chest,3=helmet
        int slot = 3 - ((ItemArmor) stack.getItem()).armorType;
        if (player.inventory.armorInventory[slot] == null) {
            player.inventory.armorInventory[slot] = stack.copy();
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Baubles Expanded (soft dependency)
    // -------------------------------------------------------------------------

    private static boolean tryEquipBauble(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.BAUBLES) return false;
        return BaubleEquipHelper.equip(player, stack);
    }

    private static final class BaubleEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof IBauble)) return false;
            IInventory inv = BaublesApi.getBaubles(player);
            if (inv == null) return false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i) == null && inv.isItemValidForSlot(i, stack)) {
                    inv.setInventorySlotContents(i, stack.copy());
                    return true;
                }
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Adventure Backpack 2 (soft dependency)
    // -------------------------------------------------------------------------

    private static boolean tryEquipAdventureBackpack(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.ADVENTURE_BACKPACK) return false;
        return AdventureBackpackEquipHelper.equip(player, stack);
    }

    private static final class AdventureBackpackEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof IBackWearableItem)) return false;
            BackpackProperty prop = BackpackProperty.get(player);
            if (prop == null || prop.getWearable() != null) return false;
            prop.setWearable(stack.copy());
            ((IBackWearableItem) stack.getItem()).onEquipped(player.worldObj, player, stack);
            BackpackProperty.sync(player);
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // Minecraft Backpack Mod (soft dependency)
    // -------------------------------------------------------------------------

    private static boolean tryEquipMcBackpack(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.MC_BACKPACK) return false;
        return McBackpackEquipHelper.equip(player, stack);
    }

    private static final class McBackpackEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof ItemBackpackBase)) return false;
            PlayerSave save = new PlayerSave(player);
            if (save.hasPersonalBackpack()) return false;
            save.setPersonalBackpack(stack.copy());
            return true;
        }
    }

    private static boolean restoreToGalacticraft(EntityPlayer player, ItemStack stack, int slot) {
        if (!ModPresence.GALACTICRAFT) return false;
        return GalacticraftRestoreHelper.restore(player, stack, slot);
    }

    private static final class GalacticraftRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            if (!(player instanceof EntityPlayerMP)) return false;
            GCPlayerStats stats = GCPlayerStats.get((EntityPlayerMP) player);
            if (stats == null) return false;
            InventoryExtended inv = stats.extendedInventory;
            if (slot < 0 || slot >= inv.getSizeInventory()) return false;
            if (inv.getStackInSlot(slot) != null) return false;
            inv.setInventorySlotContents(slot, stack.copy());
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // GalactiCraft extended inventory (soft dependency)
    // -------------------------------------------------------------------------

    private static boolean tryEquipGalacticraft(EntityPlayer player, ItemStack stack) {
        if (!ModPresence.GALACTICRAFT) return false;
        return GalacticraftEquipHelper.equip(player, stack);
    }

    private static final class GalacticraftEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(player instanceof EntityPlayerMP)) return false;
            GCPlayerStats stats = GCPlayerStats.get((EntityPlayerMP) player);
            if (stats == null) return false;
            InventoryExtended inv = stats.extendedInventory;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i) == null && isValidForSlot(stack, i)) {
                    inv.setInventorySlotContents(i, stack.copy());
                    return true;
                }
            }
            return false;
        }

        /** Mirrors SlotExtendedInventory.isItemValid logic without depending on the slot class. */
        private static boolean isValidForSlot(ItemStack stack, int slot) {
            return switch (slot) {
                case 0 -> stack.getItem() instanceof ItemOxygenMask;
                case 1 -> stack.getItem() == GCItems.oxygenGear;
                case 2, 3 -> stack.getItem() instanceof ItemOxygenTank;
                case 4 -> stack.getItem() instanceof ItemParaChute;
                case 5 -> stack.getItem() == GCItems.basicItem && stack.getItemDamage() == 19; // damage=19 is Frequency
                                                                                               // Module
                case 6 -> isThermalValid(stack, 0);
                case 7 -> isThermalValid(stack, 1);
                case 8 -> isThermalValid(stack, 2);
                case 9 -> isThermalValid(stack, 3);
                default -> false;
            };
        }

        private static boolean isThermalValid(ItemStack stack, int slotIndex) {
            if (!(stack.getItem() instanceof IItemThermal thermal)) return false;
            return thermal.isValidForSlot(stack, slotIndex);
        }
    }
}
