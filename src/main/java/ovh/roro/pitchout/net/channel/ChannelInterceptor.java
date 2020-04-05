package ovh.roro.pitchout.net.channel;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.Packet;
import ovh.roro.pitchout.game.player.GamePlayer;
import ovh.roro.pitchout.net.PacketManager;

/**
 * @author roro1506_HD
 */
public class ChannelInterceptor extends ChannelDuplexHandler {

    private final PacketManager packetManager;
    private final GamePlayer player;

    public ChannelInterceptor(PacketManager packetManager, GamePlayer player) {
        this.packetManager = packetManager;
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!this.packetManager.handlePacket((Packet) msg, this.player))
            super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!this.packetManager.handlePacket((Packet) msg, this.player))
            super.write(ctx, msg, promise);
    }
}
