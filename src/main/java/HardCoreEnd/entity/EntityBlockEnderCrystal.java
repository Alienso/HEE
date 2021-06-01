package HardCoreEnd.entity;

import HardCoreEnd.entity.dragon.DragonUtil;
import HardCoreEnd.init.BlockInit;
import HardCoreEnd.random.Facing6;
import HardCoreEnd.random.Pos;
import HardCoreEnd.util.MathUtil;
import HardCoreEnd.util.Vec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;

import java.util.List;

public class EntityBlockEnderCrystal extends EntityEnderCrystal {
    public enum Type{
        TNT, BARS, BLAST
    }

    private Type crystalType;// = Type.TNT;
    private String crystalKey = "";

    public EntityBlockEnderCrystal(World world){
        super(world);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage){
        if (isEntityInvulnerable(source))return false;
        if (world.isRemote || isDead)return true;

        if (world.provider.getDimension() != 1){
            // TODO
        }
        final Pos crystalPos = Pos.at(this).offset(Facing6.DOWN_NEGY);
        int terY = 1+DragonUtil.getTopBlockY(world, Blocks.END_STONE, crystalPos.getX()+5, crystalPos.getZ()+5, crystalPos.getY()+1);
        if (crystalPos.getY()-terY>20)
            crystalType = Type.TNT;
        else crystalType = Type.BLAST;

        if (!isDead && !world.isRemote){
            this.setDead();
            this.world.createExplosion(null, this.posX, this.posY, this.posZ, 6.0F, true);
            // TODO if (worldObj.provider.dimensionId == 1)WorldDataHandler.<DragonSavefile>get(DragonSavefile.class).destroyCrystal(crystalKey);

            Entity tar = source.getTrueSource();

            if (crystalType == Type.TNT){
                crystalPos.setAir(world);
                if (tar instanceof EntityPlayer){
                    int limiter = 4+world.getDifficulty().getDifficultyId(), topblock = DragonUtil.getTopBlockY(world, Blocks.END_STONE, MathUtil.floor(posX), MathUtil.floor(posZ), MathUtil.floor(posY));

                    for(EntityEnderman enderman: world.getEntitiesWithinAABB(EntityEnderman.class, new AxisAlignedBB(posX-10D, topblock-5D, posZ-10D, posX+10D, topblock+5D, posZ+10D))){
                        if (enderman.getDistance(posX, topblock, posZ) < 20D){
                            // TODO no longer works enderman.setTarget(tar);
                            enderman.setAttackTarget((EntityPlayer)tar);
                            if (--limiter <= 0)break;
                        }
                    }
                }

                for(int a = 0; a < 8; a++){
                    float v = 0.15F+(rand.nextFloat()/8F);
                    float tx = 0F, tz = 0F;

                    switch(a){
                        case 0:case 1:case 2: tx = v; break;
                        case 5:case 6:case 7: tx = -v; break;
                    }
                    switch(a){
                        case 0:case 3:case 5: tz = -v; break;
                        case 2:case 4:case 7: tz = v; break;
                    }

                    EntityTNTPrimed tnt = new EntityTNTPrimed(world, posX+0.5F, posY+1F, posZ+0.5F, null);
                    tnt.addVelocity(tx, 1F, tz);
                    tnt.setFuse((int)(58+(posY-DragonUtil.getTopBlockY(world, Blocks.END_STONE, MathUtil.floor(posX), MathUtil.floor(posZ), MathUtil.floor(posY)))/2));
                    world.spawnEntity(tnt);
                    tnt.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.tnt.primed")), 1F, 1F);
                }
            }
            else if (crystalType == Type.BLAST){
                crystalPos.setAir(world);
                //source.getTrueSource().sendMessage(new TextComponentString("terY: " + String.valueOf(terY)));
                //source.getTrueSource().sendMessage(new TextComponentString("crysal: " + String.valueOf(crystalPos.getY())));
                Pos.forEachBlock(crystalPos.offset(-5, terY-crystalPos.getY()-5, -5), crystalPos.offset(5, 0, 5), pos -> {
                    if (pos.getBlock(world) == Blocks.OBSIDIAN){
                        pos.setAir(world);
                        Vec vec = Vec.xz(crystalPos.getX()-pos.x, crystalPos.getZ()-pos.z).normalized();

                        EntityBlockFallingObsidian obsidian = new EntityBlockFallingObsidian(world, pos.x+0.5D, pos.y+0.1D, pos.z+0.5D);
                        obsidian.motionX = (vec.x+(rand.nextFloat()*0.5F-0.25F))*2.25F*rand.nextFloat();
                        obsidian.motionZ = (vec.z+(rand.nextFloat()*0.5F-0.25F))*2.25F*rand.nextFloat();
                        obsidian.motionY = -0.25F-rand.nextFloat()*0.4F;
                        world.spawnEntity(obsidian);
                    }
                });
            }
            this.onCrystalDestroyed(source);
        }
        return true;
    }

    public void setCrystalType(Type type){
        this.crystalType = type;
    }

    public void setCrystalKey(String key){
        this.crystalKey = key;
    }

    private void onCrystalDestroyed(DamageSource source)
    {
        if (this.world.provider instanceof WorldProviderEnd)
        {
            WorldProviderEnd worldproviderend = (WorldProviderEnd)this.world.provider;
            DragonFightManager dragonfightmanager = worldproviderend.getDragonFightManager();

            if (dragonfightmanager != null)
            {
                dragonfightmanager.onCrystalDestroyed(this, source);
            }
        }
    }
    public void writeEntityToNBT(NBTTagCompound nbt){
        super.writeEntityToNBT(nbt);
        nbt.setByte("crystalType", (byte)crystalType.ordinal());
        nbt.setString("crystalKey", crystalKey);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt){
        super.readEntityFromNBT(nbt);
        crystalType = Type.values()[MathUtil.clamp(nbt.getByte("crystalType"), 0, Type.values().length-1)];
        crystalKey = nbt.getString("crystalKey");
    }
    
}
