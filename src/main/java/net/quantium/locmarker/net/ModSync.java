package net.quantium.locmarker.net;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModSync {

	@SideOnly(Side.CLIENT)
	public static void sendMarkerToServer(double x, double y, double z, int dim) {
		ModNetworking.sendToServer(new NetPacketSendMarkerToServer(x, y, z, dim));
	}
	
	public static void sendMarkerToClients(EntityPlayerMP owner, double x, double y, double z, int dim) {
		Team team = owner.getTeam();
		String name = owner.getDisplayNameString() == null ? "" : owner.getDisplayNameString();

		getTeamMembers(team)
			.filter(p -> p.dimension == dim)
			.forEach(p -> ModNetworking.sendTo(new NetPacketSendMarkerToClient(name, x, y, z, dim, p == owner), p));
	}
	
	private static Stream<EntityPlayerMP> getTeamMembers(Team team) {
		PlayerList list = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
		
		if(team == null) return list.getPlayers().stream();
		return team.getMembershipCollection().stream().map(list::getPlayerByUsername).filter(Objects::nonNull);
	}
}
