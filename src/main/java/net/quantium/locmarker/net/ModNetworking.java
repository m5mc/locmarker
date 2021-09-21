package net.quantium.locmarker.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.quantium.locmarker.ModProvider;

public final class ModNetworking {
	private static SimpleNetworkWrapper net;
	
	public static void init() {
		net = NetworkRegistry.INSTANCE.newSimpleChannel(ModProvider.MODID);
		net.registerMessage(NetPacketSendMarkerToClient.Handler.class, NetPacketSendMarkerToClient.class, 1, Side.CLIENT);
		net.registerMessage(NetPacketSendMarkerToServer.Handler.class, NetPacketSendMarkerToServer.class, 2, Side.SERVER);
	}
	
	public static void sendToServer(IMessage msg) {
		net.sendToServer(msg);
	}
	
	public static void sendTo(IMessage msg, EntityPlayerMP ply) {
		net.sendTo(msg, ply);
	}
}
