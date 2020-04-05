package ovh.roro.pitchout.net;

import net.minecraft.server.v1_8_R3.Packet;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.util.reflect.FieldAccessor;

/**
 * @author roro1506_HD
 */
public class PacketEvent<T extends Packet> {

    private final T packet;
    private final GamePlayer player;

    private boolean cancelled;

    PacketEvent(T packet, GamePlayer player) {
        this.packet = packet;
        this.player = player;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public T getPacket() {
        return this.packet;
    }

    public GamePlayer getPlayer() {
        return this.player;
    }

    public <U> U getField(FieldAccessor<U> accessor) {
        return accessor.get(this.packet);
    }

    public <U> void setField(FieldAccessor<U> accessor, U value) {
        accessor.set(this.packet, value);
    }
}
