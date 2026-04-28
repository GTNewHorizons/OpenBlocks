package openblocks.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import openmods.Log;

public class GraveAutoEquip {

    /**
     * Tries to restore the stack to the exact slot it came from at death. Returns true if restored.
     */
    public static boolean tryRestoreToOrigin(EntityPlayer player, ItemStack stack, GraveSlotOrigin origin) {
        if (stack == null || origin == null) return false;
        try {
            switch (origin.inventoryType) {
                case GraveSlotOrigin.INV_MAIN:
                    return restoreToMain(player, stack, origin.slot);
                case GraveSlotOrigin.INV_ARMOR:
                    return restoreToArmor(player, stack, origin.slot);
                case GraveSlotOrigin.INV_TCONSTRUCT:
                    return restoreToTConstruct(player, stack, origin.slot);
                case GraveSlotOrigin.INV_BAUBLES:
                    return restoreToBaubles(player, stack, origin.slot);
                case GraveSlotOrigin.INV_ADVENTURE_BACKPACK:
                    return restoreToAdventureBackpack(player, stack);
                case GraveSlotOrigin.INV_MC_BACKPACK:
                    return restoreToMcBackpack(player, stack);
                default:
                    return false;
            }
        } catch (Exception e) {
            Log.warn(
                    "GraveAutoEquip: error restoring %s to origin %s/%d: %s",
                    stack.getDisplayName(),
                    origin.inventoryType,
                    origin.slot,
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

    private static boolean restoreToTConstruct(EntityPlayer player, ItemStack stack, int slot) {
        try {
            Class.forName("tconstruct.armor.player.TPlayerStats");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return TConstructRestoreHelper.restore(player, stack, slot);
    }

    private static final class TConstructRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            tconstruct.armor.player.TPlayerStats stats = tconstruct.armor.player.TPlayerStats.get(player);
            if (stats == null) return false;
            tconstruct.armor.player.ArmorExtended armor = stats.armor;
            if (slot < 0 || slot >= armor.getSizeInventory()) return false;
            if (armor.getStackInSlot(slot) != null) return false;
            armor.setInventorySlotContents(slot, stack.copy());
            return true;
        }
    }

    private static boolean restoreToBaubles(EntityPlayer player, ItemStack stack, int slot) {
        try {
            Class.forName("baubles.api.IBauble");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return BaublesRestoreHelper.restore(player, stack, slot);
    }

    private static final class BaublesRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack, int slot) {
            IInventory inv = baubles.api.BaublesApi.getBaubles(player);
            if (inv == null) return false;
            if (slot < 0 || slot >= inv.getSizeInventory()) return false;
            if (inv.getStackInSlot(slot) != null) return false;
            inv.setInventorySlotContents(slot, stack.copy());
            return true;
        }
    }

    private static boolean restoreToAdventureBackpack(EntityPlayer player, ItemStack stack) {
        try {
            Class.forName("com.darkona.adventurebackpack.item.IBackWearableItem");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return AdventureBackpackRestoreHelper.restore(player, stack);
    }

    private static final class AdventureBackpackRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof com.darkona.adventurebackpack.item.IBackWearableItem)) return false;
            com.darkona.adventurebackpack.playerProperties.BackpackProperty prop = com.darkona.adventurebackpack.playerProperties.BackpackProperty
                    .get(player);
            if (prop == null || prop.getWearable() != null) return false;
            prop.setWearable(stack.copy());
            ((com.darkona.adventurebackpack.item.IBackWearableItem) stack.getItem())
                    .onEquipped(player.worldObj, player, stack);
            com.darkona.adventurebackpack.playerProperties.BackpackProperty.sync(player);
            return true;
        }
    }

    private static boolean restoreToMcBackpack(EntityPlayer player, ItemStack stack) {
        try {
            Class.forName("de.eydamos.backpack.item.ItemBackpackBase");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return McBackpackRestoreHelper.restore(player, stack);
    }

    private static final class McBackpackRestoreHelper {

        static boolean restore(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof de.eydamos.backpack.item.ItemBackpackBase)) return false;
            de.eydamos.backpack.saves.PlayerSave save = new de.eydamos.backpack.saves.PlayerSave(player);
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

        return stack;
    }

    // -------------------------------------------------------------------------
    // Tinkers' Construct accessories (soft dependency) — must run BEFORE vanilla armor
    // -------------------------------------------------------------------------

    private static boolean tryEquipTConstructAccessory(EntityPlayer player, ItemStack stack) {
        try {
            Class.forName("tconstruct.library.accessory.IAccessory");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return TConstructAccessoryHelper.equip(player, stack);
    }

    private static final class TConstructAccessoryHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof tconstruct.library.accessory.IAccessory)) return false;
            tconstruct.library.accessory.IAccessory accessory = (tconstruct.library.accessory.IAccessory) stack
                    .getItem();
            tconstruct.armor.player.TPlayerStats stats = tconstruct.armor.player.TPlayerStats.get(player);
            if (stats == null) return false;
            tconstruct.armor.player.ArmorExtended armor = stats.armor;
            for (int i = 0; i < armor.getSizeInventory(); i++) {
                if (armor.getStackInSlot(i) == null && accessory.canEquipAccessory(stack, i)) {
                    armor.setInventorySlotContents(i, stack.copy());
                    return true;
                }
            }
            return false;
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
        try {
            Class.forName("baubles.api.IBauble");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return BaubleEquipHelper.equip(player, stack);
    }

    private static final class BaubleEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof baubles.api.IBauble)) return false;
            IInventory inv = baubles.api.BaublesApi.getBaubles(player);
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
        try {
            Class.forName("com.darkona.adventurebackpack.item.IBackWearableItem");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return AdventureBackpackEquipHelper.equip(player, stack);
    }

    private static final class AdventureBackpackEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof com.darkona.adventurebackpack.item.IBackWearableItem)) return false;
            com.darkona.adventurebackpack.playerProperties.BackpackProperty prop = com.darkona.adventurebackpack.playerProperties.BackpackProperty
                    .get(player);
            if (prop == null || prop.getWearable() != null) return false;
            prop.setWearable(stack.copy());
            ((com.darkona.adventurebackpack.item.IBackWearableItem) stack.getItem())
                    .onEquipped(player.worldObj, player, stack);
            com.darkona.adventurebackpack.playerProperties.BackpackProperty.sync(player);
            return true;
        }
    }

    // -------------------------------------------------------------------------
    // Minecraft Backpack Mod (soft dependency)
    // -------------------------------------------------------------------------

    private static boolean tryEquipMcBackpack(EntityPlayer player, ItemStack stack) {
        try {
            Class.forName("de.eydamos.backpack.item.ItemBackpackBase");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return McBackpackEquipHelper.equip(player, stack);
    }

    private static final class McBackpackEquipHelper {

        static boolean equip(EntityPlayer player, ItemStack stack) {
            if (!(stack.getItem() instanceof de.eydamos.backpack.item.ItemBackpackBase)) return false;
            de.eydamos.backpack.saves.PlayerSave save = new de.eydamos.backpack.saves.PlayerSave(player);
            if (save.hasPersonalBackpack()) return false;
            save.setPersonalBackpack(stack.copy());
            return true;
        }
    }
}
