package net.quantium.locmarker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.locmarker.net.ModSync;

@SideOnly(Side.CLIENT)
public final class ModEventHandler {
	private static final float RANGE_HARD_LIMIT = 512f;
	private static final float RAYTRACE_STEP = 150f;
	
	private static RayTraceResult extendedRayTrace(World world, Vec3d start, Vec3d dir, float limit) {
		while(limit > 0.5f) {
			Vec3d end = start.addVector(dir.x * limit, dir.y * limit, dir.z * limit);
			RayTraceResult r = world.rayTraceBlocks(start, end, false, true, true);
		
			if(r != null && r.typeOfHit != Type.MISS) return r;
			
			Vec3d hpos = r == null ? start.addVector(dir.x * RAYTRACE_STEP, dir.y * RAYTRACE_STEP, dir.z * RAYTRACE_STEP) : r.hitVec;
			limit -= start.distanceTo(hpos);
			start = hpos;
		}
		
		return null;
	}
	
	private static void sendMarkerIfPressed() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		if(ModInput.putMarker() && player != null) {
			float ticks = Minecraft.getMinecraft().getRenderPartialTicks();
			Vec3d start = player.getPositionEyes(ticks);
	        Vec3d dir = player.getLook(ticks);
	        
			RayTraceResult res = extendedRayTrace(player.world, start, dir, RANGE_HARD_LIMIT);
			
			if (res != null && res.typeOfHit != Type.MISS) {
				ModSync.sendMarkerToServer(res.hitVec.x, res.hitVec.y, res.hitVec.z, player.dimension);
			}
		}
	}
	
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent e) {
		if(e.phase == TickEvent.Phase.START) {
			MarkerState.update();
			sendMarkerIfPressed();
		}
	}
}
