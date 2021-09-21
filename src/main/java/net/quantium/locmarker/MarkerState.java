package net.quantium.locmarker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MarkerState {
	
	private static final Map<String, Marker> markers = new HashMap<>();
	
	public static void put(double x, double y, double z, int dimension, String name, boolean self) {
		Marker m = markers.get(name);
		
		if(m != null) {
			m.set(x, y, z, dimension, self);
		} else {
			Marker marker = new Marker(x, y, z, dimension, name, self);
			if(!ModProvider.MOD_BUS.post(new MarkerPutEvent(marker))) 
			{
				markers.put(name, marker);
			}
		}
	}
	
	public static void update() {
		markers.values().removeIf(marker -> !marker.update());
	}
	
	public static Collection<Marker> markers() {
		return markers.values();
	}
	
	public static class MarkerPutEvent extends Event 
	{
		private final Marker marker;

		public MarkerPutEvent(Marker marker) {
			this.marker = marker;
		}

		public Marker getMarker() {
			return marker;
		}
	}
	
	public static class Marker {
		private final String name;
		private boolean self;
		
		private double prevX;
		private double prevY;
		private double prevZ;
		
		private double x;
		private double y;
		private double z;
		
		private double targetX;
		private double targetY;
		private double targetZ;

		private int dimension;
		
		private long timestamp;
		private long spawnstamp;

		private Marker(double x, double y, double z, int dimension, String name, boolean self) {
			this.x = this.prevX = this.targetX = x;
			this.y = this.prevY = this.targetY = y;
			this.z = this.prevZ = this.targetZ = z;
			this.dimension = dimension;
			
			this.name = name;
			this.self = self;
			this.timestamp = this.spawnstamp = Minecraft.getSystemTime();
		}

		public boolean isOwned() {
			return self;
		}
		
		public String getName() {
			return name;
		}

		public long getTimestamp() {
			return timestamp;
		}
		
		public long getSpawnstamp() {
			return spawnstamp;
		}
		
		public double getLifetime() {
			return (Minecraft.getSystemTime() - timestamp) / 1000d;
		}
		
		public double getTimeFromSpawn() {
			return (Minecraft.getSystemTime() - spawnstamp) / 1000d;
		}
		
		public double getTimeLeft() {
			return ModConfig.time - getLifetime();
		}
		
		public double getInterpolatedX(double t) {
			return prevX + (x - prevX) * t;
		}
		
		public double getInterpolatedY(double t) {
			return prevY + (y - prevY) * t;
		}
		
		public double getInterpolatedZ(double t) {
			return prevZ + (z - prevZ) * t;
		}
		
		public int getDimension() {
			return dimension;
		}
		
		public void set(double x, double y, double z, int dimension, boolean self) {
			this.targetX = x;
			this.targetY = y;
			this.targetZ = z;
			
			if(dimension != this.dimension) {
				this.dimension = dimension;
				this.spawnstamp = Minecraft.getSystemTime();
			}
			
			this.timestamp = Minecraft.getSystemTime();
			this.self = self;
		}
		
		public boolean update() {
			if(getTimeLeft() <= 0) return false;
			
			prevX = x;
			prevY = y;
			prevZ = z;
			
			x += (targetX - x) * 0.5f;
			y += (targetY - y) * 0.5f;
			z += (targetZ - z) * 0.5f;
			return true;
		}
	}
}
