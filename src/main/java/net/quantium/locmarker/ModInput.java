package net.quantium.locmarker;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public final class ModInput {
	private static KeyBinding key;

	public static void init() {
		key = new KeyBinding("locmarker.putmarker.desc", Keyboard.KEY_Z, "key.locmarker.category");
		ClientRegistry.registerKeyBinding(key);
	}
	
	public static boolean putMarker() {
		return key.isKeyDown();
	}
}
