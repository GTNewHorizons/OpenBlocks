package openblocks.common;

import cpw.mods.fml.common.Loader;

public final class ModPresence {

    private ModPresence() {}

    public static final boolean BAUBLES = Loader.isModLoaded("Baubles");
    public static final boolean TCONSTRUCT = Loader.isModLoaded("TConstruct");
    public static final boolean ADVENTURE_BACKPACK = Loader.isModLoaded("adventurebackpack");
    public static final boolean MC_BACKPACK = Loader.isModLoaded("Backpack");
    public static final boolean GALACTICRAFT = Loader.isModLoaded("GalacticraftCore");
}
