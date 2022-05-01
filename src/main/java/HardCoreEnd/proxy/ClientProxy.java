package HardCoreEnd.proxy;

import HardCoreEnd.Main;
import HardCoreEnd.entity.dragon.managers.DragonChunkManager;
import HardCoreEnd.event.MyEventHandler;
import HardCoreEnd.init.EntityInit;
import HardCoreEnd.network.PacketPipeline;
import HardCoreEnd.save.SaveData;
import HardCoreEnd.sound.MusicManager;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override public void registerItemRenderer(Item item, int meta, String id){
        ModelLoader.setCustomModelResourceLocation(item,meta,new ModelResourceLocation(item.getRegistryName(),id));
    }

    @Override
    public void preInitRegistries(FMLPreInitializationEvent event){
        EntityInit.registerEntity();
        DragonChunkManager.register();
        SaveData.register();
        SoundsHandler.registerSounds();
        MusicManager.register();
        Main.sourceFile = event.getSourceFile();
    }

    @Override
    public void initRegistries(FMLInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new MyEventHandler());
        PacketPipeline.initializePipeline();
    }

    @Override
    public void postInitRegistries(FMLPostInitializationEvent event){
        /*try {
            MusicManager.instance.onSoundLoad(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
    }



}
