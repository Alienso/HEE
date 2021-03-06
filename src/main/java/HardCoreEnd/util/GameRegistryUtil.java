package HardCoreEnd.util;


import HardCoreEnd.Main;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class GameRegistryUtil{
    /*public static void registerBlock(Block block, String name, Class<? extends ItemBlock> itemBlockClass){
        GameRegistry.registerBlock(block, itemBlockClass, name);
    }

    public static void registerItem(Item item, String name){
        GameRegistry.registerItem(item, name, "HardcoreEnderExpansion");
    }

    public static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String name){
        GameRegistry.registerTileEntity(tileEntityClass, "HardcoreEnderExpansion:"+name);
    }
*/
    public static void registerEntity(ResourceLocation rs,Class<? extends Entity> entityClass, String entityName, int id, int trackingRange){
        EntityRegistry.registerModEntity(rs,entityClass, entityName, id, Main.instance, trackingRange, 1, true);
    }

    public static void registerEntity(ResourceLocation rs,Class<? extends Entity> entityClass, String entityName, int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates){
        EntityRegistry.registerModEntity(rs,entityClass, entityName, id, Main.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
    }
/*
    public static void replaceVanillaEntity(Class<? extends Entity> newEntityClass, int entityId){
        String name = EntityList.getStringFromID(entityId);

        if (name == null || EntityList.stringToClassMapping.remove(name) == null || EntityList.IDtoClassMapping.remove(Integer.valueOf(entityId)) == null){
            throw new IllegalStateException("Error replacing entity with ID "+entityId+", entity entry missing!");
        }

        EntityList.addMapping(newEntityClass, name, entityId);
    }

    public static void addSmeltingRecipe(ItemStack input, ItemStack output, float experience){
        FurnaceRecipes.smelting().func_151394_a(input, output, experience);
    }

    public static Iterable<Block> getBlocks(){
        return new Iterable<Block>(){
            @Override
            public Iterator<Block> iterator(){
                return Block.blockRegistry.iterator();
            }
        };
    }

    public static Iterable<Item> getItems(){
        return new Iterable<Item>(){
            @Override
            public Iterator<Item> iterator(){
                return Item.itemRegistry.iterator();
            }
        };
    }
*/
    // might not fit, but eh

    public static void registerEventHandler(Object o){
        MinecraftForge.EVENT_BUS.register(o);
        FMLCommonHandler.instance().bus().register(o);
    }

    /*public static void unregisterEventHandler(Object o){
        MinecraftForge.EVENT_BUS.unregister(o);
        FMLCommonHandler.instance().bus().unregister(o);
    }

    // protection against idiots who can't register their shit properly

    public static UniqueIdentifier findIdentifier(Block block){
        try{
            return GameRegistry.findUniqueIdentifierFor(block);
        }
        catch(Exception e){
            return null;
        }
    }

    public static UniqueIdentifier findIdentifier(Item item){
        try{
            return GameRegistry.findUniqueIdentifierFor(item);
        }
        catch(Exception e){
            return null;
        }
    }*/

    private GameRegistryUtil(){}
}
