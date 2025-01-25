package openblocks.common.item;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import openblocks.OpenBlocks;
import openblocks.common.entity.EntityHangGlider;
import openmods.infobook.BookDocumentation;

@BookDocumentation(hasVideo = true)
public class ItemHangGlider extends Item {

    private static HashSet<Integer> blacklistedDimensions = new HashSet<>();

    public ItemHangGlider() {
        setCreativeTab(OpenBlocks.tabOpenBlocks);
    }

    // Configuration loading
    // Outer Lands - 173
    // The Last Millenium - 112
    // Underdark - 100
    // Mehen Belt - 95
    // Seth - 94
    // Horus - 93
    // Anubis - 92
    // Maahes - 91
    // Neper - 90
    // Miranda - 86
    // T Ceti E - 85
    // Vega B - 84
    // Haumea - 83
    // Barnarda F - 82
    // Barnarda E - 81
    // Saturn - 77
    // Neptune - 74
    // Jupiter - 71
    // Mirror - 70
    // Pocket Plane - 69
    // Ross128b - 64
    // Ross128ba - 63
    // Bedrock - 60
    // Torment - 56
    // Spirit World - 55
    // Space Station 54 - 54
    // Storage Cell - 52
    // Uranus - 51
    // The Outer Lands - 50
    // Pluto - 49
    // Triton - 48
    // Proteus - 47
    // Oberon - 46
    // Callisto - 45
    // Titan - 44
    // Ganymede - 43
    // Ceres - 42
    // Enceladus - 41
    // Deimos - 40
    // Venus - 39
    // Phobos - 38
    // Mercury - 37
    // Io - 36
    // Europa - 35
    // Kuiper Belt - 33
    // Barnarda C - 32
    // α Centauri Bb - 31
    // Asteroids - 30
    // Mars - 29
    // Moon - 28
    // Makemake - 25
    // dimensionDarkWorld - 227

    public static void loadConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        int[] dimensionIDs = config.get(
                "general",
                "BlacklistedDimensions",
                new int[] { 95, 94, 93, 92, 86, 85, 84, 83, 82, 81, 77, 74, 71, 69, 63, 54, 51, 49, 48, 47, 46, 45, 44,
                        43, 42, 41, 40, 39, 38, 37, 36, 35, 33, 32, 31, 30, 29, 28, 25 },
                "List of dimension IDs where the glider doesn't work").getIntList();
        for (int id : dimensionIDs) {
            blacklistedDimensions.add(id);
        }

        config.save();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote && player != null) {
            EntityHangGlider glider = EntityHangGlider.getEntityHangGlider(player);
            if (glider != null) despawnGlider(player, glider);
            else spawnGlider(player);
        }
        return itemStack;
    }

    private static void despawnGlider(EntityPlayer player, EntityHangGlider glider) {
        glider.setDead();
    }

    private static void spawnGlider(EntityPlayer player) {
        if (isInvalidDimension(player)) {
            player.addChatMessage(new ChatComponentTranslation("item.openblocks.hangglider.invalid_dimension"));
            return;
        }

        EntityHangGlider glider = new EntityHangGlider(player.worldObj, player);
        glider.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationPitch, player.rotationYaw);
        EntityHangGlider.setEntityHangGlider(player, glider);
        player.worldObj.spawnEntityInWorld(glider);
    }

    private static boolean isInvalidDimension(EntityPlayer player) {
        return blacklistedDimensions.contains(player.dimension);
    }
}
