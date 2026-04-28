package openblocks.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import openmods.Log;
import openmods.inventory.GenericInventory;

public class GraveInventorySnapshot {

    public static final class OriginatedStack {

        public final GraveSlotOrigin origin;
        public final ItemStack stack;

        public OriginatedStack(GraveSlotOrigin origin, ItemStack stack) {
            this.origin = origin;
            this.stack = stack;
        }
    }

    private final List<OriginatedStack> entries = new ArrayList<OriginatedStack>();

    public GraveInventorySnapshot(EntityPlayer player) {
        captureMain(player);
        captureArmor(player);
        captureTConstruct(player);
        captureBaubles(player);
        captureAdventureBackpack(player);
        captureMcBackpack(player);
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
        try {
            Class.forName("tconstruct.armor.player.TPlayerStats");
        } catch (ClassNotFoundException ignored) {
            return;
        }
        try {
            TConstructCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture TConstruct slots: %s", e);
        }
    }

    private static final class TConstructCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            tconstruct.armor.player.TPlayerStats stats = tconstruct.armor.player.TPlayerStats.get(player);
            if (stats == null) return;
            tconstruct.armor.player.ArmorExtended armor = stats.armor;
            for (int i = 0; i < armor.getSizeInventory(); i++) {
                ItemStack stack = armor.getStackInSlot(i);
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_TCONSTRUCT, i), stack));
            }
        }
    }

    private void captureBaubles(EntityPlayer player) {
        try {
            Class.forName("baubles.api.IBauble");
        } catch (ClassNotFoundException ignored) {
            return;
        }
        try {
            BaublesCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture Baubles slots: %s", e);
        }
    }

    private static final class BaublesCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            IInventory inv = baubles.api.BaublesApi.getBaubles(player);
            if (inv == null) return;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack != null)
                    out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_BAUBLES, i), stack));
            }
        }
    }

    private void captureAdventureBackpack(EntityPlayer player) {
        try {
            Class.forName("com.darkona.adventurebackpack.playerProperties.BackpackProperty");
        } catch (ClassNotFoundException ignored) {
            return;
        }
        try {
            AdventureBackpackCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture AdventureBackpack slot: %s", e);
        }
    }

    private static final class AdventureBackpackCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            com.darkona.adventurebackpack.playerProperties.BackpackProperty prop = com.darkona.adventurebackpack.playerProperties.BackpackProperty
                    .get(player);
            if (prop == null) return;
            ItemStack stack = prop.getWearable();
            if (stack != null)
                out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_ADVENTURE_BACKPACK, 0), stack));
        }
    }

    private void captureMcBackpack(EntityPlayer player) {
        try {
            Class.forName("de.eydamos.backpack.saves.PlayerSave");
        } catch (ClassNotFoundException ignored) {
            return;
        }
        try {
            McBackpackCaptureHelper.capture(player, entries);
        } catch (Exception e) {
            Log.warn("GraveInventorySnapshot: failed to capture McBackpack slot: %s", e);
        }
    }

    private static final class McBackpackCaptureHelper {

        static void capture(EntityPlayer player, List<OriginatedStack> out) {
            de.eydamos.backpack.saves.PlayerSave save = new de.eydamos.backpack.saves.PlayerSave(player);
            if (!save.hasPersonalBackpack()) return;
            ItemStack stack = save.getPersonalBackpack();
            if (stack != null)
                out.add(new OriginatedStack(new GraveSlotOrigin(GraveSlotOrigin.INV_MC_BACKPACK, 0), stack));
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
        List<ItemStack> pool = new ArrayList<ItemStack>(graveLoot.size());
        for (EntityItem ei : graveLoot) {
            ItemStack s = ei.getEntityItem();
            if (s != null) pool.add(s);
        }

        List<OriginatedStack> matched = new ArrayList<OriginatedStack>();
        for (OriginatedStack entry : entries) {
            int idx = findAndConsume(pool, entry.stack);
            if (idx >= 0) matched.add(entry);
        }

        // Items in graveLoot that had no snapshot match (e.g. spawned by other mods)
        // are added without origin info.
        List<ItemStack> unmatched = new ArrayList<ItemStack>(pool);

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
