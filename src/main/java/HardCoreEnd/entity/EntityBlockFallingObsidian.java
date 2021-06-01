package HardCoreEnd.entity;

import java.util.Arrays;
import java.util.List;

import HardCoreEnd.init.BlockInit;
import HardCoreEnd.random.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityBlockFallingObsidian extends EntityFallingBlock{
    public EntityBlockFallingObsidian(World world){
        super(world);
    }

    public EntityBlockFallingObsidian(World world, double x, double y, double z){
        super(world, x, y, z, BlockInit.obsidian_falling.getDefaultState());
    }

    @Override
    public void onUpdate(){
        if (func_145805_f().getMaterial(func_145805_f().getDefaultState()) == Material.AIR){ // OBFUSCATED get block
            setDead();
            return;
        }

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        fallTime++;
        motionY -= 0.15D;
        fast_move(MoverType.SELF,motionX, motionY, motionZ);
        motionX *= 0.9D;
        motionY *= 0.9D;
        motionZ *= 0.9D;
        Pos pos = Pos.at(this);

        if (fallTime == 1 && pos.getBlock(world) == func_145805_f()){ // OBFUSCATED get block
            pos.setAir(world);
        }

        if (onGround){
            motionX *= 0.7D;
            motionZ *= 0.7D;
            motionY *= -0.5D;

            if (fallTime > 5 && pos.getBlock(world) != Blocks.PISTON_EXTENSION && world.getEntitiesWithinAABB(EntityBossDragon.class, getEntityBoundingBox().expand(1,1,1)).isEmpty()){
                if (pos.setBlock(world, func_145805_f().getDefaultState()))setDead();
            }
        }
        else if (!world.isRemote && (pos.getY() < 1 || fallTime > 600)){
            dropItem(Item.getItemFromBlock(Blocks.OBSIDIAN), 1);
            setDead();
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier){
        int i = MathHelper.ceil(distance-1F);

        if (i > 0){
            for(Entity entity: world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())){
                entity.attackEntityFrom(DamageSource.FALLING_BLOCK, Math.min(MathHelper.floor(i*5F), 60));
            }
        }
    }

    public Block func_145805_f(){
        return BlockInit.obsidian_falling;
    }

    public void fast_move(MoverType type, double x, double y, double z) {

            this.world.profiler.startSection("move");

            double d2 = x;
            double d3 = y;
            double d4 = z;

            List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(x, y, z));

            if (y != 0.0D)
            {
                int k = 0;

                for (int l = list1.size(); k < l; ++k)
                {
                    y = list1.get(k).calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            }

            if (x != 0.0D)
            {
                int j5 = 0;

                for (int l5 = list1.size(); j5 < l5; ++j5)
                {
                    x = list1.get(j5).calculateXOffset(this.getEntityBoundingBox(), x);
                }

                if (x != 0.0D)
                {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
                }
            }

            if (z != 0.0D)
            {
                int k5 = 0;

                for (int i6 = list1.size(); k5 < i6; ++k5)
                {
                    z = list1.get(k5).calculateZOffset(this.getEntityBoundingBox(), z);
                }

                if (z != 0.0D)
                {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
                }
            }

            this.world.profiler.endSection();
            this.world.profiler.startSection("rest");
            this.resetPositionToBB();
            this.collidedHorizontally = d2 != x || d4 != z;
            this.collidedVertically = d3 != y;
            this.onGround = this.collidedVertically && d3 < 0.0D;
            this.collided = this.collidedHorizontally || this.collidedVertically;
            int j6 = MathHelper.floor(this.posX);
            int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
            int k6 = MathHelper.floor(this.posZ);
            BlockPos blockpos = new BlockPos(j6, i1, k6);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            this.updateFallState(y, this.onGround, iblockstate, blockpos);

            if (d2 != x)
            {
                this.motionX = 0.0D;
            }

            if (d4 != z)
            {
                this.motionZ = 0.0D;
            }

            Block block = iblockstate.getBlock();

            if (d3 != y)
            {
                block.onLanded(this.world, this);
            }

            try
            {
                this.doBlockCollisions();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }
            this.world.profiler.endSection();
    }
}