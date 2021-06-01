package HardCoreEnd.event;

import HardCoreEnd.entity.EntityBlockEnderCrystal;
import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.random.Pos;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
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

    @SubscribeEvent
    public static void restoreEndCrystals(WorldEvent.Load event){

        if (event.getWorld().provider.getDimension() == 1)
            event.getWorld().getClosestPlayer(0,0,0,10000,false).sendMessage(new TextComponentString("U endu"));
        /*World world = event.getWorld();
        if (world.provider.getDimension() == 1){
            BlockPos pos = new BlockPos(entity.posX,entity.posY,entity.posZ);
            if (world.getBlockState(pos).getBlock() == Blocks.FIRE){
                world.setBlockToAir(pos);
                EntityBlockEnderCrystal crystal = new EntityBlockEnderCrystal(world);
                crystal.setPosition(pos.getX(),pos.getY(),pos.getZ());
                world.spawnEntity(crystal);
            }
        }*/

    }

    /*@SubscribeEvent(priority = EventPriority.NORMAL)
    public void replaceEndMusic(PlaySoundEvent event){
        Sound s = event.getSound().getSound();
        System.out.println(s.getSoundLocation().getResourcePath());
    }*/
}
