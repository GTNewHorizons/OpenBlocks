package openblocks.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired on the server after a grave has been fully retrieved by a player and removed from the world. Not fired when the
 * owner only retrieves part of the items because their inventory is full; it fires once the grave is finally emptied.
 */
public class GraveRetrievedEvent extends PlayerEvent {

    public enum Type {
        /** The owner retrieved the items by clicking the grave. */
        OWNER_CLICKED,
        /** The grave was dug up with a shovel, by the owner or anyone else. */
        SHOVEL_CLICK
    }

    public final Type type;

    /** Name of the player the grave belonged to, may differ from {@link #entityPlayer} when dug up. */
    public final String ownerName;

    /**
     * When the owner died, as {@link System#currentTimeMillis()}. {@code -1} if unknown: graves placed by hand, or
     * created before this field was added.
     */
    public final long deathTime;

    /** X coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int x;

    /** Y coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int y;

    /** Z coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int z;

    public GraveRetrievedEvent(EntityPlayer player, Type type, String ownerName, long deathTime, int x, int y, int z) {
        super(player);
        this.type = type;
        this.ownerName = ownerName;
        this.deathTime = deathTime;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
