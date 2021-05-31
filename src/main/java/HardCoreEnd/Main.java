package HardCoreEnd;

import HardCoreEnd.entity.dragon.managers.DragonChunkManager;
import HardCoreEnd.event.MyEventHandler;
import HardCoreEnd.network.PacketPipeline;
import HardCoreEnd.proxy.FXClientProxy;
import HardCoreEnd.util.Reference;
import HardCoreEnd.util.handlers.RegistryHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import HardCoreEnd.proxy.CommonProxy;

import java.io.File;

@Mod(modid = Reference.MODID,name=Reference.NAME,version = Reference.VERSION)
public class Main {

    @Mod.Instance
    public static Main instance;

    @SidedProxy(clientSide = Reference.CLIENT,serverSide = Reference.COMMON)
    public static CommonProxy proxy;

    public static File sourceFile;
    public static FXClientProxy fx;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        DragonChunkManager.register();
        sourceFile = event.getSourceFile();
        RegistryHandler.preInitRegistries();
    };

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new MyEventHandler());
        PacketPipeline.initializePipeline();
    };

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {};

}
