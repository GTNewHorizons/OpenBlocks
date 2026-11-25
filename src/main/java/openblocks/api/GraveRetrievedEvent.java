package openblocks.api;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired on the server after a player has retrieved a grave and it has been removed from the world, either by the owner
 * clicking it or by digging it up with a shovel (see {@link Type}). Not fired when the owner only retrieves part of the
 * items because their inventory is full; it fires once one of those actions finally empties the grave. Not fired when
 * the grave is removed any other way, such as being broken in creative mode or replaced with a command.
 */
public class GraveRetrievedEvent extends PlayerEvent {

    public static final long UNKNOWN_DEATH_TIME = -1;

    public enum Type {
        /** The owner retrieved the items by clicking the grave. */
        OWNER_CLICKED,
        /** The grave was dug up with a shovel, by the owner or anyone else. */
        SHOVEL_CLICK
    }

    @NotNull
    public final Type type;

    /**
     * UUID of the player the grave belonged to, may differ from {@link #entityPlayer} when dug up. {@code null} if
     * unknown: graves placed by hand with a custom name, or created before this field was added.
     */
    @Nullable
    public final UUID ownerId;

    /**
     * When the owner died, as {@link System#currentTimeMillis()}. {@link #UNKNOWN_DEATH_TIME} if unknown: graves placed
     * by hand, or created before this field was added.
     */
    public final long deathTime;

    /** X coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int x;

    /** Y coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int y;

    /** Z coordinate of the removed grave, in the world of {@link #entityPlayer}. */
    public final int z;

    public GraveRetrievedEvent(EntityPlayer player, @NotNull Type type, @Nullable UUID ownerId, long deathTime, int x,
            int y, int z) {
        super(player);
        this.type = type;
        this.ownerId = ownerId;
        this.deathTime = deathTime;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
