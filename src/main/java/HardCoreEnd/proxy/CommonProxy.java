package HardCoreEnd.proxy;

import HardCoreEnd.Main;
import HardCoreEnd.entity.dragon.managers.DragonChunkManager;
import HardCoreEnd.event.MyEventHandler;
import HardCoreEnd.init.EntityInit;
import HardCoreEnd.network.PacketPipeline;
import HardCoreEnd.save.SaveData;
import HardCoreEnd.sound.MusicManager;
import HardCoreEnd.util.handlers.RegistryHandler;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public static boolean opMobs = false;
    public void registerItemRenderer(Item item, int meta, String id){}
    public void registerBlockRenderer(Block block, int meta, String id){}

    public void preInitRegistries(FMLPreInitializationEvent event){
        EntityInit.registerEntity();
        DragonChunkManager.register();
        Main.sourceFile = event.getSourceFile();
    }

    public void initRegistries(FMLInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new MyEventHandler());
        PacketPipeline.initializePipeline();
    }

    public void postInitRegistries(FMLPostInitializationEvent event){}
}
