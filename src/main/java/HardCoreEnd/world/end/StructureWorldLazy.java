package HardCoreEnd.world.end;

import HardCoreEnd.random.Pos;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

import java.util.Random;

/**
 * Special structure world that uses chunk methods directly, which makes the generation very fast but only suitable for generating in remote, unloaded locations.
 */
public class StructureWorldLazy extends StructureWorld{
    private boolean sendToWatchers;

    public StructureWorldLazy(World world, int radX, int sizeY, int radZ){
        super(world, radX, sizeY, radZ);
    }

    public StructureWorldLazy(int radX, int sizeY, int radZ){
        super(null, radX, sizeY, radZ);
    }

    /**
     * Updates the chunks for players close to them at the expense of performance.
     */
    public void setSendToWatchers(){
        this.sendToWatchers = true;
    }

    @Override
    protected void generateBlocksInWorld(World world, Random rand, int centerX, int bottomY, int centerZ){
        Pos.PosMutable pos = new Pos.PosMutable();
        int x, y, z, index = -1;

        for(z = -radZ; z <= radZ; z++){
            for(x = -radX; x <= radX; x++){
                for(y = 0; y < sizeY; y++){
                    if (blocks[++index] != null){
                        pos.set(centerX+x, bottomY+y, centerZ+z);

                        world.getChunkFromBlockCoords(new BlockPos(pos.x,0, pos.z)).setBlockState(new BlockPos(pos.x&15, pos.y, pos.z&15), blocks[index].getDefaultState());
                        if ( sendToWatchers){
                            //world.markBlockForUpdate(pos.x, pos.y, pos.z);
                            //world.scheduleBlockUpdate(new BlockPos(pos.x, pos.y, pos.z),new BlockPos(pos.x, pos.y, pos.z));
                            world.scheduleBlockUpdate(new BlockPos(centerX+pos.getX(), bottomY+pos.getY(), centerZ+pos.getZ()),getBlock(pos),0,1);
                        }
                    }
                }
            }
        }
    }
}
