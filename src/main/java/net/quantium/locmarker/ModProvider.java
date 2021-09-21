package net.quantium.locmarker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.quantium.locmarker.net.ModNetworking;

@Mod(modid = ModProvider.MODID, name = ModProvider.NAME, version = ModProvider.VERSION)
public final class ModProvider
{
    public static final String MODID = "locmarker";
    public static final String NAME = "Location Marker Mod";
    public static final String VERSION = "0.2";
    
    public static final EventBus MOD_BUS = new EventBus();

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        ModNetworking.init();
    }
    
    @SideOnly(Side.CLIENT)
    @EventHandler
    public void initClientOnly(FMLInitializationEvent event)
    {
    	ModInput.init();
    	
    	MinecraftForge.EVENT_BUS.register(ModRenderer.class);
    	MinecraftForge.EVENT_BUS.register(ModEventHandler.class);
    	MOD_BUS.register(ModEventHandler.class);
    }
}
