package net.quantium.locmarker.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class NetPacketSendMarkerToServer implements IMessage {
	
	private double x;
	private double y;
	private double z;
	private int dim;
	
	public NetPacketSendMarkerToServer() {}
	
	public NetPacketSendMarkerToServer(double x, double y, double z, int dim) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dim = dim;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		dim = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(x)
		   .writeDouble(y)
		   .writeDouble(z)
		   .writeInt(dim);
	}

	public static class Handler implements IMessageHandler<NetPacketSendMarkerToServer, IMessage> {
        @Override
        public IMessage onMessage(NetPacketSendMarkerToServer message, MessageContext ctx) {
        	ModSync.sendMarkerToClients(ctx.getServerHandler().player, message.x, message.y, message.z, message.dim);
            return null;
        }
    }
}
