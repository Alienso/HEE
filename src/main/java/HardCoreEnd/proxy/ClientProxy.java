package HardCoreEnd.proxy;

import HardCoreEnd.Main;
import HardCoreEnd.entity.dragon.managers.DragonChunkManager;
import HardCoreEnd.event.MyEventHandler;
import HardCoreEnd.init.EntityInit;
import HardCoreEnd.network.PacketPipeline;
import HardCoreEnd.save.SaveData;
import HardCoreEnd.sound.CustomMusicTicker;
import HardCoreEnd.sound.MusicManager;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.block.Block;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
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
    public void postInitRegistries(FMLPostInitializationEvent event){ }

    @Override
    public void onLoadComplete(FMLLoadCompleteEvent e){

        if (MusicManager.isMusicAvailable(new ResourceLocation("hardcoreenderexpansion","music.game_end"))) {
            try {
                MusicManager.instance.onSoundLoad(null);
            } catch (NoSuchFieldException | IllegalAccessException noSuchFieldException) {
                noSuchFieldException.printStackTrace();
            }
        }
    }
}
