package HardCoreEnd.entity;

import java.util.List;

import HardCoreEnd.init.BlockInit;
import HardCoreEnd.random.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
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
        move(MoverType.SELF,motionX, motionY, motionZ);
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
            for(Entity entity:(List<Entity>)world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())){
                entity.attackEntityFrom(DamageSource.FALLING_BLOCK, Math.min(MathHelper.floor(i*5F), 60));
            }
        }
    }

    public Block func_145805_f(){
        return BlockInit.obsidian_falling;
    }
}