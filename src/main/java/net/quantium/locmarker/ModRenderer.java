package net.quantium.locmarker;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.locmarker.MarkerState.Marker;

@SideOnly(Side.CLIENT)
public final class ModRenderer {

	private static final ResourceLocation MARKER_TEXTURE = new ResourceLocation(ModProvider.MODID, "textures/marker.png");
	
	private static final MatrixCapturer capturer = new MatrixCapturer();
	private static final Vector4f vec0 = new Vector4f();
	
	@SubscribeEvent
	public static void onWorldRender(RenderWorldLastEvent e) {
		capturer.capture();
	}
	
	@SubscribeEvent
	public static void onOverlayRender(RenderGameOverlayEvent.Pre e) {
		if(e.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP player = mc.player;
			RenderManager rnd = mc.getRenderManager();
			ScaledResolution res = e.getResolution();
			FontRenderer font = rnd.getFontRenderer();
			float ticks = mc.getRenderPartialTicks();
			Matrix4f camera = capturer.getMatrix();
			
			if(player == null) return;
			
			for(Marker marker : MarkerState.markers()) {
				if(marker.getDimension() != player.dimension) continue;
				
				double posx = marker.getInterpolatedX(ticks);
				double posy = marker.getInterpolatedY(ticks);
				double posz = marker.getInterpolatedZ(ticks);
				
				float relx = (float)(posx - rnd.viewerPosX); 
				float rely = (float)(posy - rnd.viewerPosY);
				float relz = (float)(posz - rnd.viewerPosZ);
				
				vec0.set(relx, rely, relz, 1f);
				Matrix4f.transform(camera, vec0, vec0);
				
				float w = Math.max(0.001f, vec0.w);
				float x = vec0.x / w;
				float y = vec0.y / w;
				float z = vec0.z / w;
				
				float cx = x;
				float cy = y;
				
				//clip marker position to screen
				final float SCREEN_SPACE = 0.85f;
				final float SPAWN_ANIM_TIME = 0.2f;
				
				float rsq = cx * cx * cx * cx + cy * cy * cy * cy;
				if (rsq > SCREEN_SPACE * SCREEN_SPACE * SCREEN_SPACE * SCREEN_SPACE) {
					float factor = SCREEN_SPACE / (float)Math.pow(rsq, 1 / 4f);
					cx *= factor;
					cy *= factor;
				} else if(z < 0) { //special case (exactly behind)
					cx = 0;
					cy = -0.9f;
				}
				
				//arrow direction & magnitude
				float dx = x - cx;
				float dy = y - cy;
				float dm = MathHelper.sqrt(dx * dx + dy * dy) / 0.2f;
				
				if(dm > 1) {
					dx /= dm;
					dy /= dm;
					dm = 1;
				}
				
				float angle = (float)(MathHelper.atan2(-dy, dx) / Math.PI) * 180f;
				
				//marker alpha
				float decay = Math.min(0.5f, (float)marker.getTimeLeft()) / 0.5f;
				decay *= decay;
				
				float alpha = decay * (1f - dm * 0.4f);
				float textAlpha = alpha * (1 - dm);

				//calculating marker scale
				double spawnScale = 1d;
				double timeSinceSpawn = marker.getTimeFromSpawn();
				
				//spawning animation
				if(timeSinceSpawn < SPAWN_ANIM_TIME) {
					double xf = timeSinceSpawn / SPAWN_ANIM_TIME;
					spawnScale = 1 - (1 - xf) * MathHelper.cos((float) (4.7 * xf));
				}
				
				double sizeFactor = lerp(w, 150 / 16, dm);
				double scale = Math.max(150 / sizeFactor, 4) * spawnScale;
				
				//marker position on screen
				//transforming [-1, 1] to [0, size]
				float px = (1 + cx) / 2f * res.getScaledWidth();
				float py = (1 - cy) / 2f * res.getScaledHeight();
				
				//rendering state
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				
				//rendering
				GlStateManager.pushMatrix();
				
				GlStateManager.translate(px, py, 0);

				//render text
				if(textAlpha > 0.05f) {
					float reldst = MathHelper.sqrt(relx * relx + rely * rely + relz * relz);
					
					GlStateManager.pushMatrix();
					float tsf = Math.max(0.5f, 2f * (float)spawnScale / MathHelper.sqrt(sizeFactor));
					
					String str1 = marker.getName();
					String str2 = String.format("%.2f m", reldst);
								
					int alph = (int)(textAlpha * 255) << 24;
					
					GlStateManager.scale(tsf, tsf, 1f);

					GlStateManager.pushMatrix();
					GlStateManager.translate(-font.getStringWidth(str1) / 2f, -0.6f * (scale / tsf), 0);
					font.drawString(str1, 0, -font.FONT_HEIGHT, 0x00ffffff | alph);
					GlStateManager.popMatrix();
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(-font.getStringWidth(str2) / 2f,  0.6f * (scale / tsf), 0);
					font.drawString(str2, 0, 0, 0x00ffffff | alph);
					GlStateManager.popMatrix();
					
					GlStateManager.popMatrix();
				}
			
				GlStateManager.scale(scale, scale, 1f);
				
				rnd.renderEngine.bindTexture(MARKER_TEXTURE);
					
				//first pass
				GlStateManager.enableDepth();
				
				GlStateManager.depthMask(true);
				GlStateManager.colorMask(false, false, false, false);
				
				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
				
				GlStateManager.pushMatrix();
			
				GlStateManager.rotate(angle - 45f, 0, 0, 1);
				GlStateManager.scale(dm, dm, 1);
				GlStateManager.translate(0.5, 0.5, 0);
			
				GlStateManager.color(1f, 1f, 1f, alpha);
				
				drawTexturedRect(0, 0.5, -20);
			
				GlStateManager.popMatrix();
			
				drawTexturedRect(marker.isOwned() ? 0.5 : 0, 0, -10);
				
				//second pass
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
				GlStateManager.depthMask(false);
				GlStateManager.colorMask(true, true, true, true);
				
				GlStateManager.pushMatrix();
				
				GlStateManager.rotate(angle - 45f, 0, 0, 1);
				GlStateManager.scale(dm, dm, 1);
				GlStateManager.translate(0.5, 0.5, 0);
			
				GlStateManager.color(1f, 1f, 1f, alpha);
				
				drawTexturedRect(0, 0.5, -20);
			
				GlStateManager.popMatrix();
			
				drawTexturedRect(marker.isOwned() ? 0.5 : 0, 0, -10);
				
				//end
				GlStateManager.depthMask(true);
				GlStateManager.popMatrix();
				GlStateManager.disableBlend();
				GlStateManager.disableAlpha();
			}
		}
	}
	
	private static void drawTexturedRect(double ox, double oy, double depth) {
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-0.5,  0.5, depth).tex(ox + 0.0, oy + 0.5).endVertex();
        bufferbuilder.pos( 0.5,  0.5, depth).tex(ox + 0.5, oy + 0.5).endVertex();
        bufferbuilder.pos( 0.5, -0.5, depth).tex(ox + 0.5, oy + 0.0).endVertex();
        bufferbuilder.pos(-0.5, -0.5, depth).tex(ox + 0.0, oy + 0.0).endVertex();
        tessellator.draw();
	}
	
	private static double lerp(double d0, double d1, double t) {
		return d0 + (d1 - d0) * t;
	}
	
	private static class MatrixCapturer {
		
		private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
		private final FloatBuffer modelView = GLAllocation.createDirectFloatBuffer(16);
		
		private final Matrix4f projectionMatrix = new Matrix4f();
		private final Matrix4f modelViewMatrix = new Matrix4f();
		private final Matrix4f modelViewProjectionMatrix = new Matrix4f();
		
		public void capture() {
			projection.clear();
			modelView.clear();
			
			GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projection);
			GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
			
			projectionMatrix.load(projection);
			modelViewMatrix.load(modelView);
			
			Matrix4f.mul(projectionMatrix, modelViewMatrix, modelViewProjectionMatrix);
		}
		
		public Matrix4f getMatrix() {
			return modelViewProjectionMatrix;
		}
	}
}
