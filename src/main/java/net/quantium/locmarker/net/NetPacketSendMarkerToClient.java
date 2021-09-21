package net.quantium.locmarker.net;

import java.util.Random;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.quantium.locmarker.MarkerState;

public final class NetPacketSendMarkerToClient implements IMessage {
	
	private String name;
	private double x;
	private double y;
	private double z;
	private int dim;
	private boolean self;
	
	public NetPacketSendMarkerToClient() {}
	
	public NetPacketSendMarkerToClient(String name, double x, double y, double z, int dim, boolean self) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dim = dim;
		this.self = self;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		name = ByteBufUtils.readUTF8String(buf);
		x = buf.readDouble();
		y = buf.readDouble();
		z = buf.readDouble();
		dim = buf.readInt();
		self = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		buf.writeDouble(x)
		   .writeDouble(y)
		   .writeDouble(z)
		   .writeInt(dim)
		   .writeBoolean(self);
	}

	public static class Handler implements IMessageHandler<NetPacketSendMarkerToClient, IMessage> {
        @Override
        public IMessage onMessage(NetPacketSendMarkerToClient message, MessageContext ctx) {
        	Minecraft.getMinecraft().addScheduledTask(() -> {
        		MarkerState.put(message.x, message.y, message.z, message.dim, message.name, message.self);
        	});
        	
            return null;
        }
    }
}
