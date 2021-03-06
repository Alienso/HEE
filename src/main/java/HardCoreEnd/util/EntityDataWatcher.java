package HardCoreEnd.util;
import gnu.trove.map.hash.TObjectByteHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;

import java.io.IOException;

public final class EntityDataWatcher{
    private final EntityDataManager watcher;
    private final TObjectByteHashMap<Enum<?>> idMap;

    public EntityDataWatcher(Entity trackedEntity){
        this.watcher = trackedEntity.getDataManager();
        this.idMap = new TObjectByteHashMap<>(8);
    }

    private int getId(Enum<?> linkedEnum){
        byte value = idMap.get(linkedEnum);
        if (value == idMap.getNoEntryValue())throw new IllegalArgumentException("Enum was not registered in EntityDataWatcher!");
        return value;
    }

    // BOOLEAN

    public DataParameter<Boolean> addBoolean(int internalId){
        DataParameter<Boolean> param = new DataParameter<>(internalId,DataSerializers.BOOLEAN);
        watcher.register(param,false);
        return param;
    }

    public DataParameter<Boolean> addBoolean(int internalId,boolean initialValue){
        DataParameter<Boolean> param = new DataParameter<>(internalId,DataSerializers.BOOLEAN);
        watcher.register(param,initialValue);
        return param;
    }

    public boolean getBoolean(DataParameter<Boolean> param){
        return watcher.get(param);
    }

    public void setBoolean(DataParameter<Boolean> param, boolean newValue){
        watcher.set(param,newValue);
    }


    // BYTE

    /*public void addByte(Enum<?> linkedEnum){
        addObject(linkedEnum, Byte.valueOf((byte)0));
    }

    public void addByte(Enum<?> linkedEnum, int initialValue){
        addObject(linkedEnum, Byte.valueOf((byte)initialValue));
    }

    public byte getByte(Enum<?> linkedEnum){
        return watcher.getWatchableObjectByte(getId(linkedEnum));
    }

    public void setByte(Enum<?> linkedEnum, int newValue){
        watcher.updateObject(getId(linkedEnum), Byte.valueOf((byte)newValue));
    }

    // SHORT

    public void addShort(Enum<?> linkedEnum){
        addObject(linkedEnum, Short.valueOf((short)0));
    }

    public void addShort(Enum<?> linkedEnum, int initialValue){
        addObject(linkedEnum, Short.valueOf((short)initialValue));
    }

    public short getShort(Enum<?> linkedEnum){
        return watcher.getWatchableObjectShort(getId(linkedEnum));
    }

    public void setShort(Enum<?> linkedEnum, int newValue){
        watcher.updateObject(getId(linkedEnum), Short.valueOf((short)newValue));
    }

    // INTEGER

    public void addInt(Enum<?> linkedEnum){
        addObject(linkedEnum, Integer.valueOf(0));
    }

    public void addInt(Enum<?> linkedEnum, int initialValue){
        addObject(linkedEnum, Integer.valueOf(initialValue));
    }

    public int getInt(Enum<?> linkedEnum){
        return watcher.getWatchableObjectInt(getId(linkedEnum));
    }

    public void setInt(Enum<?> linkedEnum, int newValue){
        watcher.updateObject(getId(linkedEnum), Integer.valueOf(newValue));
    }

    // FLOAT

    public void addFloat(Enum<?> linkedEnum){
        addObject(linkedEnum, Float.valueOf(0F));
    }

    public void addFloat(Enum<?> linkedEnum, float initialValue){
        addObject(linkedEnum, Float.valueOf(initialValue));
    }

    public float getFloat(Enum<?> linkedEnum){
        return watcher.getWatchableObjectFloat(getId(linkedEnum));
    }

    public void setFloat(Enum<?> linkedEnum, float newValue){
        watcher.updateObject(getId(linkedEnum), Float.valueOf(newValue));
    }

    // STRING

    public void addString(Enum<?> linkedEnum){
        addObject(linkedEnum, "");
    }

    public void addString(Enum<?> linkedEnum, String initialValue){
        addObject(linkedEnum, initialValue);
    }

    public String getString(Enum<?> linkedEnum){
        return watcher.getWatchableObjectString(getId(linkedEnum));
    }

    public void setString(Enum<?> linkedEnum, String newValue){
        watcher.updateObject(getId(linkedEnum), newValue);
    }

    // ITEMSTACK

    public void addItemStack(Enum<?> linkedEnum){
        addObjectByType(linkedEnum, 5);
    }

    public void addItemStack(Enum<?> linkedEnum, ItemStack initialValue){
        addObject(linkedEnum, initialValue);
    }

    public @Nullable ItemStack getItemStack(Enum<?> linkedEnum){
        return watcher.getWatchableObjectItemStack(getId(linkedEnum));
    }

    public void setItemStack(Enum<?> linkedEnum, @Nullable ItemStack newValue){
        watcher.updateObject(getId(linkedEnum), newValue);
    }*/
}

