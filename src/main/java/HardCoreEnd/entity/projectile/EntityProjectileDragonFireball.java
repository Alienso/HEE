package HardCoreEnd.entity.projectile;

import HardCoreEnd.util.MathUtil;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityProjectileDragonFireball extends EntityFireball{
    private final float power;

    public EntityProjectileDragonFireball(World world){
        super(world);
        setSize(1F, 1F);
        this.power = 2.5F;
    }

    public EntityProjectileDragonFireball(World world, EntityLiving shooter, double xDiff, double yDiff, double zDiff, float speedMp, boolean random, float power){
        super(world, shooter, xDiff, yDiff, zDiff);
        xDiff += rand.nextGaussian()*(random ? 0.8D : 0.2D);
        yDiff += rand.nextGaussian()*(random ? 0.8D : 0.2D);
        zDiff += rand.nextGaussian()*(random ? 0.8D : 0.2D);

        double dist = MathUtil.distance(xDiff, yDiff, zDiff);
        accelerationX = (xDiff/dist)*0.21D*speedMp;
        accelerationY = (yDiff/dist)*0.21D*speedMp;
        accelerationZ = (zDiff/dist)*0.21D*speedMp;

        this.power = power;
    }

    @Override
    public void onUpdate(){
        super.onUpdate();
        if (ticksExisted > 100 && Math.abs(motionX) < 0.01D && Math.abs(motionY) < 0.01D && Math.abs(motionZ) < 0.01D)onImpact(null);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote){
            setDead();
            Explosion explosion = new Explosion(world,shootingEntity, posX, posY, posZ,power,false, true);
            explosion.doExplosionA();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount){
        if (source.isFireDamage() || isDead)return false;

        markVelocityChanged();
        setDead();

        Explosion explosion = new Explosion(world,shootingEntity, posX, posY, posZ, power*0.7F,true,true);
        explosion.doExplosionA();

        if (source.getTrueSource() != null)source.getTrueSource().setFire(3);

        return true;
    }

    @Override
    public boolean isBurning(){
        return true;
    }
}