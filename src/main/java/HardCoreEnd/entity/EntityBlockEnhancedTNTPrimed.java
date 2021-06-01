package HardCoreEnd.entity;

import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.random.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO add a global madness indicator that will control how many packets, particles, and such are going to be handled to reduce lag
public class EntityBlockEnhancedTNTPrimed extends EntityTNTPrimed {
    //private final EnhancementList<TNTEnhancements> enhancements;
    private boolean wentIntoWall = false;
    private boolean extraPower;
    private float extraExtraPower;

    public EntityBlockEnhancedTNTPrimed(World world){
        super(world);
        setFuse(80);
        //yOffset = 0;

        if (world.isRemote){
            int count = world.loadedEntityList.size();
            if (count > 500)setDead();
        }

        //this.enhancements = new EnhancementList<>(TNTEnhancements.class);
    }

    public EntityBlockEnhancedTNTPrimed(World world, double x, double y, double z, EntityLivingBase igniter){
        super(world, x, y, z, igniter);
        //this.enhancements = enhancements;
        this.extraPower = rand.nextBoolean();
        this.extraExtraPower = CommonProxy.opMobs ? 3F : 0F;

		/* TODO if (tntEnhancements.contains(TNTEnhancements.NOCLIP)){
			noClip = true;
			fuse = 40;
		}

		if (tntEnhancements.contains(TNTEnhancements.NO_FUSE))fuse = 1;*/
        noClip = true;
    }

    @Override
    public void onUpdate(){
        lastTickPosX = posX;
        lastTickPosY = posY;
        lastTickPosZ = posZ;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        motionY -= 0.04D;
        move(MoverType.SELF,motionX, motionY, motionZ);
        motionX *= 0.98D;
        motionY *= 0.98D;
        motionZ *= 0.98D;

        if (!world.isRemote && noClip){
            Block block = Pos.at(this).getDown().getBlock(world);

            if (!wentIntoWall && !block.isAir(block.getDefaultState(),world,new BlockPos(this.posX,this.posY,this.posZ)))wentIntoWall = true;
            else if (wentIntoWall && block.getMaterial(block.getDefaultState()) == Material.AIR){
                setFuse(1);
            }
        }

        if (onGround && !noClip){
            motionX *= 0.7D;
            motionZ *= 0.7D;
            motionY *= -0.5D;
        }

        setFuse(getFuse()-1);
        if (getFuse() <= 0 && !world.isRemote){
            setDead();
            Explosion explosion = new Explosion(world,this, posX, posY, posZ, extraPower ? 5.2F + extraExtraPower : 4F + extraExtraPower, true,true);
            explosion.doExplosionA();
            //this.world.createExplosion(this, this.posX, this.posY, this.posZ, extraPower ? 5.2F + extraExtraPower : 4F + extraExtraPower, true,);
        }
        /*else{
            HardcoreEnderExpansion.fx.setLimiter();
            HardcoreEnderExpansion.fx.global("smoke", posX, posY+0.5D, posZ, 0D, 0D, 0D);
            HardcoreEnderExpansion.fx.reset();
        }*/

        setPosition(posX, posY, posZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch){
        if (world.loadedEntityList.size() > 500)return; // let client handle motion if there's too much TNT, looks a bit better
        setPosition(x, y, z);
        setRotation(yaw, pitch);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt){
        super.writeEntityToNBT(nbt);
        //nbt.setString("enhancements2", enhancements.serialize());
        nbt.setBoolean("wentIntoWall", wentIntoWall);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt){
        super.readEntityFromNBT(nbt);
        //enhancements.deserialize(nbt.getString("enhancements2"));
        wentIntoWall = nbt.getBoolean("wentIntoWall");
    }
}
