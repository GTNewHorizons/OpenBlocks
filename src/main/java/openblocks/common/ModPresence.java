package openblocks.common;

/** Cached mod-presence flags. Class.forName is called once per JVM startup. */
public final class ModPresence {

    private ModPresence() {}

    public static final boolean BAUBLES = classExists("baubles.api.IBauble");
    public static final boolean TCONSTRUCT = classExists("tconstruct.util.config.PHConstruct");
    public static final boolean ADVENTURE_BACKPACK = classExists(
            "com.darkona.adventurebackpack.item.IBackWearableItem");
    public static final boolean MC_BACKPACK = classExists("de.eydamos.backpack.item.ItemBackpackBase");

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
