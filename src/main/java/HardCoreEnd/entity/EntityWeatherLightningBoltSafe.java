package HardCoreEnd.entity;

import HardCoreEnd.random.Pos;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class EntityWeatherLightningBoltSafe extends EntityLightningBolt{
    private int lightningState;
    private int boltLivingTime;

    public EntityWeatherLightningBoltSafe(World world){
        super(world,0,0,0,false);
    }

    public EntityWeatherLightningBoltSafe(World world, double x, double y, double z){
        super(world, x, y, z,false);
        lightningState = 2;
        boltVertex = rand.nextLong();
        boltLivingTime = rand.nextInt(3)+1;

        Pos pos = Pos.at(this);
        if (!world.isRemote && world.getDifficulty().getDifficultyId() >= 2 && world.getChunkProvider().isChunkGeneratedAt(pos.getX(),pos.getZ())){
            Pos.forEachBlock(pos.offset(-1, -1, -1), pos.offset(1, 1, 1), testPos -> {
                if (testPos.getBlock(world) == Blocks.FIRE)testPos.setAir(world);
            });
        }
    }

    @Override
    public void onUpdate(){
        onEntityUpdate();

        if (lightningState == 2){
            world.playSound(null,posX, posY, posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.AMBIENT,10000F, 0.8F+rand.nextFloat()*0.2F);
            world.playSound(null,posX, posY, posZ, SoundEvents.ENTITY_GENERIC_EXPLODE,SoundCategory.AMBIENT, 2F, 0.5F+rand.nextFloat()*0.2F);
        }

        --lightningState;

        if (lightningState < 0){
            if (boltLivingTime == 0)setDead();
            else if (lightningState<-rand.nextInt(10)){
                --boltLivingTime;
                lightningState = 1;
                boltVertex = rand.nextLong();
            }
        }

        if (lightningState >= 0 && world.isRemote){
            world.setLastLightningBolt(2);
        }
    }
}
