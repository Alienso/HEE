package HardCoreEnd.save;

import HardCoreEnd.entity.EntityBlockEnderCrystal;
import HardCoreEnd.random.NBTCompound;
import HardCoreEnd.util.GameRegistryUtil;
import HardCoreEnd.random.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jline.utils.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public final class SaveData{
    public static File enderCrystalFile;
    private static final SaveData instance = new SaveData();

    public static void register(){
        GameRegistryUtil.registerEventHandler(instance);
        for(ISaveDataHandler handler:instance.handlers)handler.register();
    }

    public static <T extends SaveFile> T global(Class<T> cls){
        checkServerSide();
        return instance.global.get(cls);
    }

    /*public static <T extends PlayerFile> T player(EntityPlayer player, Class<T> cls){
        checkServerSide();
        return instance.player.get(player, cls);
    }

    public static <T extends PlayerFile> T player(String playerID, Class<T> cls){
        checkServerSide();
        return instance.player.get(playerID, cls);
    }*/

    private static void checkServerSide(){
        if (Log.isDebugEnabled() && FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER){
            throw new RuntimeException("SaveData cannot be accessed on client side: "+Thread.currentThread().getName());
        }
    }

    private final GlobalDataHandler global;
    //private final PlayerDataHandler player;
    private final ISaveDataHandler[] handlers;

    private File worldSaveDir;
    private String worldIdentifier = "";

    private SaveData(){
        this.global = new GlobalDataHandler();
        //this.player = new PlayerDataHandler();

        this.handlers = new ISaveDataHandler[]{
                global//, player
        };
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e){
        if (e.getWorld().isRemote)return;

        if (e.getWorld().provider.getDimension() == 1){
            loadEndCrystals(e);
        }
        String id = e.getWorld().getSaveHandler().getWorldDirectory()+e.getWorld().getWorldInfo().getWorldName()+e.getWorld().getWorldInfo().getSeed();

        if (!worldIdentifier.equals(id)){
            worldIdentifier = id;

            File root = DimensionManager.getCurrentSaveRootDirectory();
            if (root == null)throw new IllegalStateException("Could not determine world save directory!");

            worldSaveDir = new File(root, "hee2");
            if (!worldSaveDir.exists())worldSaveDir.mkdirs();
            enderCrystalFile = new File(worldSaveDir,"mojFajl.txt");

            for(ISaveDataHandler handler:handlers)handler.clear(worldSaveDir);
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save e){
        if (worldSaveDir == null)return;

        for(ISaveDataHandler handler:handlers)handler.save();
    }

    public static NBTCompound readFile(File file){
        if (file.exists()){
            try(FileInputStream fileStream = new FileInputStream(file)){
                return NBT.wrap(CompressedStreamTools.readCompressed(fileStream));
            }catch(IOException ioe){
                //Log.throwable(ioe, "Error reading NBT file - $0", file);
            }
        }

        return new NBTCompound();
    }

    public static boolean saveFile(File file, NBTCompound nbt){
        try(FileOutputStream fileStream = new FileOutputStream(file)){
            CompressedStreamTools.writeCompressed(nbt.getUnderlyingTag(), fileStream);
            return true;
        }catch(Exception ex){
            //Log.throwable(ex, "Error writing NBT file $0", file);
            return false;
        }
    }

    private void loadEndCrystals(WorldEvent.Load e){
        try {
            if (enderCrystalFile.createNewFile()) {
            } else {
                Scanner sc = new Scanner(enderCrystalFile);
                while (sc.hasNextLine()){
                    if (!sc.hasNext())
                        return;
                    double x = sc.nextDouble();
                    double y = sc.nextDouble();
                    double z = sc.nextDouble();
                    EntityBlockEnderCrystal crystal = new EntityBlockEnderCrystal(e.getWorld());
                    crystal.setPosition(x,y,z);
                    e.getWorld().spawnEntity(crystal);
                }
            }
        } catch (IOException ex) {
            System.out.println("An error occurred.");
            ex.printStackTrace();
        }
    }
}
