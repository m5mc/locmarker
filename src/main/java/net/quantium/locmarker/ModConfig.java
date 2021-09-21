package net.quantium.locmarker;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;

@Config(modid = ModProvider.MODID)
public final class ModConfig {
	@RangeDouble(min = 0, max = 60)
	@Name("Show Time (in seconds)")
	public static double time = 10d;
}
