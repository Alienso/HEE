package HardCoreEnd.event;

import HardCoreEnd.entity.EntityBlockEnderCrystal;
import HardCoreEnd.entity.EntityBossDragon;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MyEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void replaceVanillaEndEntities(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = event.getWorld();

        if (entity instanceof EntityBossDragon){
            return;
        }
        if (entity instanceof EntityBlockEnderCrystal){
            return;
        }

        if (entity instanceof EntityEnderCrystal){
            entity.setDead();
            EntityBlockEnderCrystal e = new EntityBlockEnderCrystal(world);
            e.setPosition(entity.posX,entity.posY,entity.posZ);
            world.spawnEntity(e);
        }
        if (entity instanceof EntityDragon) {
            event.setCanceled(true);
            entity.setDead();
            EntityBossDragon dragon = new EntityBossDragon(world);
            world.spawnEntity(dragon);
        }
    }

    /*@SubscribeEvent(priority = EventPriority.NORMAL)
    public void replaceEndMusic(PlaySoundEvent event){
        Sound s = event.getSound().getSound();
        System.out.println(s.getSoundLocation().getResourcePath());
    }*/
}
