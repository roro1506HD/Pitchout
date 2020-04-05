package ovh.roro.pitchout.net;

import net.minecraft.server.v1_8_R3.Packet;

/**
 * @author roro1506_HD
 */
@FunctionalInterface
public interface IPacketHandler<T extends Packet> {

    void handle(PacketEvent<T> event);

}
