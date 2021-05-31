package HardCoreEnd.entity;

import java.util.List;

import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.random.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityMobVampiricBat extends EntityBat{
    public Entity target;

    public EntityMobVampiricBat(World world){
        super(world);
    }

    @Override
    public void onLivingUpdate(){
        super.onLivingUpdate();
        for(int a = 0; a < 3; a++)world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY-0.45D, posZ, 0D, 0D, 0D);
    }

    @Override
    protected void updateAITasks(){
        super.updateAITasks();

        if (target == null || (!Pos.at(target).isAir(world) || target.isDead || target.posY < 1)){
            if ((target = world.getClosestPlayerToEntity(this, 32D)) == null){
                setDead();
                return;
            }
        }

        double xDiff = target.posX+0.5D-posX;
        double yDiff = target.posY+0.1D-posY;
        double zDiff = target.posZ+0.5D-posZ;
        motionX += (Math.signum(xDiff)*0.5D-motionX)*0.1D;
        motionY += (Math.signum(yDiff)*0.7D-motionY)*0.1D;
        motionZ += (Math.signum(zDiff)*0.5D-motionZ)*0.1D;
        rotationYaw += MathHelper.wrapDegrees((float)(Math.atan2(motionZ, motionX)*180D/Math.PI)-90F-rotationYaw);
        moveForward = 0.5F;
    }

    @Override
    public boolean canBePushed(){
        return true;
    }

    @Override
    protected void collideWithNearbyEntities(){
        for(Entity entity:(List<Entity>)world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(0.2D, 0D, 0.2D))){
            if (entity.canBePushed())collideWithEntity(entity);
        }
    }

    @Override
    protected void collideWithEntity(Entity entity){
        entity.applyEntityCollision(this);

        if (entity instanceof EntityPlayer){
            if (!world.isRemote){
                EntityPlayer player = (EntityPlayer)entity;
                player.attackEntityFrom(DamageSource.causeMobDamage(this), CommonProxy.opMobs ? 4F : 2F);

                EntitySelector.any(world).stream().filter(e -> e instanceof EntityBossDragon).findAny().ifPresent(dragon -> {
                    ((EntityBossDragon)dragon).heal(1);
                    world.addWeatherEffect(new EntityWeatherLightningBoltSafe(world, dragon.posX, dragon.posY+dragon.height*0.25F, dragon.posZ));
                });

                setDead();
            }

            for(int a = 0; a < 6; a++){
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY+0.4D, posZ, 0D, 0D, 0D);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY+0.4D, posZ, 0D, 0D, 0D);
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount){
        if (!world.isRemote)setDead();

        for(int a = 0; a < 6; a++){
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY+0.4D, posZ, 0D, 0D, 0D);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY+0.4D, posZ, 0D, 0D, 0D);
        }

        return true;
    }

    @Override
    protected float getSoundVolume(){
        return 0.4F;
    }

    @Override
    public void setIsBatHanging(boolean isHanging){}

    @Override
    public boolean getIsBatHanging(){
        return false;
    }

    /*@Override
    public String getCommandSenderName(){
        return hasCustomNameTag() ? getCustomNameTag() : StatCollector.translateToLocal(Baconizer.mobName("entity.vampireBat.name"));
    }*/
}
