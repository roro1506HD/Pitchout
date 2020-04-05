package ovh.roro.pitchout.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.v1_8_R3.Packet;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.net.channel.ChannelInterceptor;

/**
 * @author roro1506_HD
 */
@SuppressWarnings("ALL")
public class PacketManager {

    private final Map<Class, Set<IPacketHandler>> handlers = new HashMap<>();

    public <T extends Packet> void addHandler(Class<T> packetClass, IPacketHandler<T> handler) {
        this.handlers.computeIfAbsent(packetClass, unused -> new HashSet<>()).add(handler);
    }

    public void addChannel(GamePlayer gamePlayer) {
        gamePlayer.getCraftPlayer().getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "ekalia_packet_handler", new ChannelInterceptor(this, gamePlayer));
    }

    public <T extends Packet> boolean handlePacket(T packet, GamePlayer player) {
        Set<IPacketHandler> handlers = this.handlers.get(packet.getClass());

        if (handlers == null)
            return false;

        PacketEvent<T> event = new PacketEvent<>(packet, player);

        for (IPacketHandler handler : handlers)
            // noinspection unchecked
            handler.handle(event);

        return event.isCancelled();
    }
}
