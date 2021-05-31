package HardCoreEnd.save;


import HardCoreEnd.util.GameRegistryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataHandler implements ISaveDataHandler{
    private static final String dataIdentifier = "HardcoreEnderExpansion2";

    /*public static final String getID(EntityPlayer player){
        return ((PlayerIdProperty)player.getExtendedProperties(dataIdentifier)).id; // server only
    }*/

    private final Map<String, PlayerFile> cache = new HashMap<>();
    private File root;

    // ISaveDataHandler

    @Override
    public void register(){
        GameRegistryUtil.registerEventHandler(this);
    }

    @Override
    public void clear(File root){
        cache.clear();
        this.root = new File(root, "players");
        if (!this.root.exists())this.root.mkdirs();
    }

    /*public <T extends PlayerFile> T get(EntityPlayer player, Class<T> cls){
        return get(getID(player), cls);
    }*/

    public <T extends PlayerFile> T get(String playerID, Class<T> cls){
        String cacheKey = cls.getSimpleName()+"~"+playerID;

        PlayerFile savefile = cache.get(cacheKey);

        if (savefile == null){
            try{
                cache.put(cacheKey, savefile = cls.getConstructor(String.class).newInstance(playerID+".nbt"));
                savefile.loadFromNBT(root);
            }catch(Exception e){
                throw new RuntimeException("Could not construct a new instance of PlayerFile - "+cls.getName(), e);
            }
        }

        return (T)savefile;
    }

    @Override
    public void save(){
        cache.values().stream().filter(savefile -> savefile.wasModified()).forEach(savefile -> savefile.saveToNBT(root));
    }

    // Events

    /*@SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing e){
        if (e.getEntity().world != null && !e.getEntity().world.isRemote && e.getEntity() instanceof EntityPlayer){
            if (!e.getEntity().registerExtendedProperties(dataIdentifier, new PlayerIdProperty()).equals(dataIdentifier)){
                throw new IllegalStateException("Could not register extended player properties, likely due to the properties already being registered by another mod!");
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone e){
        NBTTagCompound nbt = new NBTTagCompound();
        e.getOriginal().getExtendedProperties(dataIdentifier).saveNBTData(nbt);
        e.getEntityPlayer().getExtendedProperties(dataIdentifier).loadNBTData(nbt);
    }

    // IExtendedEntityProperties

    private static class PlayerIdProperty implements IExtendedEntityProperties{
        private String id;

        @Override
        public void init(Entity entity, World world){
            id = StringUtils.remove(UUID.randomUUID().toString(), '-');
        }

        @Override
        public void saveNBTData(NBTTagCompound nbt){
            nbt.setString("HEE2_PID", id);
        }

        @Override
        public void loadNBTData(NBTTagCompound nbt){
            if (nbt.hasKey("HEE2_PID"))id = nbt.getString("HEE2_PID");
        }
    }*/
}
