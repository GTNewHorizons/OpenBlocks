package openblocks.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.darkona.adventurebackpack.playerProperties.BackpackProperty;

import baubles.api.BaublesApi;
import de.eydamos.backpack.saves.PlayerSave;
import lain.mods.cos.CosmeticArmorReworked;
import lain.mods.cos.inventory.InventoryCosArmor;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.inventory.InventoryExtended;
import openmods.Log;
import openmods.inventory.GenericInventory;
import tconstruct.armor.player.ArmorExtended;
import tconstruct.armor.player.TPlayerStats;
import tconstruct.util.config.PHConstruct;

public class GraveInventorySnapshot {

    public record OriginatedStack(GraveSlotOrigin origin, ItemStack stack) {}

    private final List<OriginatedStack> entries = new ArrayList<>();

    public GraveInventorySnapshot(EntityPlayer player) {
        captureMain(player);
        captureArmor(player);
        captureTConstruct(player);
        captureBaubles(player);
        captureAdventureBackpack(player);
        captureMcBackpack(player);
        captureGalacticraft(player);
        captureCosmeticArmor(player);
    }

    private void captureMain(EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            if (stack != null)
                entries.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_MAIN, i), stack));
        }
    }

    private void captureArmor(EntityPlayer player) {
        for (int i = 0; i < player.inventory.armorInventory.length; i++) {
            ItemStack stack = player.inventory.armorInventory[i];
            if (stack != null)
                entries.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_ARMOR, i), stack));
        }
    }

    private void captureTConstruct(EntityPlayer player) {
        if (!ModPresence.TCONSTRUCT) return;
        try {
            if (!TConstructCaptureHelper.isTabEnabled()) return;
            TConstructCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture TConstruct slots: %s", e);
        }
    }

    private static final class TConstructCaptureHelper {

        static boolean isTabEnabled() {
            try {
                // enableTinkerInventoryTab is available only since TConstruct 1.14.72-GTNH
                return PHConstruct.enableTinkerInventoryTab;
            } catch (NoSuchFieldError e) {
                return true;
            }
        }

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            TPlayerStats stats = TPlayerStats.get(player);
            if (stats == null) return;
            ArmorExtended armor = stats.armor;
            for (int i = 0; i < armor.getSizeInventory(); i++) {
                ItemStack stack = armor.getStackInSlot(i);
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_TCONSTRUCT, i), stack));
            }
        }
    }

    private void captureBaubles(EntityPlayer player) {
        if (!ModPresence.BAUBLES) return;
        try {
            BaublesCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture Baubles slots: %s", e);
        }
    }

    private static final class BaublesCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            IInventory inv = BaublesApi.getBaubles(player);
            if (inv == null) return;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_BAUBLES, i), stack));
            }
        }
    }

    private void captureAdventureBackpack(EntityPlayer player) {
        if (!ModPresence.ADVENTURE_BACKPACK) return;
        try {
            AdventureBackpackCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture AdventureBackpack slot: %s", e);
        }
    }

    private static final class AdventureBackpackCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            BackpackProperty prop = BackpackProperty.get(player);
            if (prop == null) return;
            ItemStack stack = prop.getWearable();
            if (stack != null)
                out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_ADVENTURE_BACKPACK, 0), stack));
        }
    }

    private void captureMcBackpack(EntityPlayer player) {
        if (!ModPresence.MC_BACKPACK) return;
        try {
            McBackpackCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture McBackpack slot: %s", e);
        }
    }

    private static final class McBackpackCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            PlayerSave save = new PlayerSave(player);
            if (!save.hasPersonalBackpack()) return;
            ItemStack stack = save.getPersonalBackpack();
            if (stack != null)
                out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_MC_BACKPACK, 0), stack));
        }
    }

    private void captureGalacticraft(EntityPlayer player) {
        if (!ModPresence.GALACTICRAFT) return;
        try {
            GalacticraftCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture GalactiCraft slots: %s", e);
        }
    }

    private static final class GalacticraftCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            if (!(player instanceof EntityPlayerMP)) return;
            GCPlayerStats stats = GCPlayerStats.get((EntityPlayerMP) player);
            if (stats == null) return;
            InventoryExtended inv = stats.extendedInventory;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_GALACTICRAFT, i), stack));
            }
        }
    }

    private void captureCosmeticArmor(EntityPlayer player) {
        if (!ModPresence.COSMETIC_ARMOR) return;
        try {
            CosmeticArmorCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture Cosmetic Armor slots: %s", e);
        }
    }

    private static final class CosmeticArmorCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            InventoryCosArmor cosArmorInventory = CosmeticArmorReworked.invMan
                    .getCosArmorInventory(player.getUniqueID());
            ItemStack[] inv = cosArmorInventory.getInventory();
            for (int i = 0; i < inv.length; i++) {
                ItemStack stack = inv[i];
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_COSMETIC_ARMOR, i), stack));
            }
        }
    }

    /**
     * Builds grave inventory and slot origins from the snapshot. Only items that appear in graveLoot (matched by
     * item/damage/NBT) are included.
     *
     * @param graveLoot  items that will go into the grave (from PlayerDropsEvent, post-filtering)
     * @param originsOut populated with grave-slot-index → GraveSlotOrigin
     * @return the grave inventory
     */
    public IInventory buildLoot(List<EntityItem> graveLoot, Map<Integer, GraveSlotOrigin> originsOut) {
        // Build a consumable pool of drops for matching
        List<ItemStack> pool = new ArrayList<>(graveLoot.size());
        for (EntityItem ei : graveLoot) {
            ItemStack s = ei.getEntityItem();
            if (s != null) pool.add(s);
        }

        List<OriginatedStack> matched = new ArrayList<>();
        for (OriginatedStack entry : entries) {
            int idx = findAndConsume(pool, entry.stack);
            if (idx >= 0) matched.add(entry);
        }

        // Items in graveLoot that had no snapshot match (e.g. spawned by other mods)
        // are added without origin info.
        List<ItemStack> unmatched = new ArrayList<>(pool);

        GenericInventory inv = new GenericInventory("tmpplayer", false, matched.size() + unmatched.size());
        int graveSlot = 0;
        for (OriginatedStack os : matched) {
            inv.setInventorySlotContents(graveSlot, os.stack.copy());
            originsOut.put(graveSlot, os.origin);
            graveSlot++;
        }
        for (ItemStack s : unmatched) {
            inv.setInventorySlotContents(graveSlot++, s.copy());
        }
        return inv;
    }

    /** Finds first stack in pool matching item+damage+NBT, removes it and returns its index; or -1. */
    private static int findAndConsume(List<ItemStack> pool, ItemStack target) {
        for (int i = 0; i < pool.size(); i++) {
            ItemStack candidate = pool.get(i);
            if (ItemStack.areItemStackTagsEqual(candidate, target) && candidate.getItem() == target.getItem()
                    && candidate.getItemDamage() == target.getItemDamage()) {
                pool.remove(i);
                return i;
            }
        }
        return -1;
    }
}
