package HardCoreEnd.init;

import HardCoreEnd.Main;
import HardCoreEnd.entity.*;
import HardCoreEnd.util.GameRegistryUtil;
import HardCoreEnd.util.Reference;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;


public class EntityInit {

    public static void registerEntity(){
        registerEntity("Dragon", EntityBossDragon.class,Reference.ENTITY_DRAGON,320,11437146,00000000);
        registerEntity("AngryEnderman", EntityMobAngryEnderman.class,Reference.ENTITY_ANGRY_ENDERMAN,100,11437146,00000000);
        registerEntity("VampiricBat", EntityMobVampiricBat.class,Reference.ENTITY_VAMPIRIC_BAT,100,11437146,00000000);
        registerEntity("LightningBoltSafe", EntityWeatherLightningBoltSafe.class,Reference.ENTITY_LIGHTNING_BOLT_SAFE,100,11437146,00000000);
    }
    private static void registerEntity(String name, Class<? extends Entity> entity,int id,int range,int color1,int color2){
        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MODID + ":" + name),entity,name,id, Main.instance,range,1,true,color1,color2);
    }
}
