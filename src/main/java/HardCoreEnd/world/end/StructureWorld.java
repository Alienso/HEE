package HardCoreEnd.world.end;


import HardCoreEnd.random.Pos;
import HardCoreEnd.util.BoundingBox;
import HardCoreEnd.util.MathUtil;
import com.google.common.base.MoreObjects;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import javafx.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.BlockInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StructureWorld{
    protected final World world;
    protected final int radX, radZ, sizeX, sizeY, sizeZ;
    protected final Block[] blocks;
    protected final byte[] metadata;
    protected final TIntHashSet scheduledUpdates = new TIntHashSet(32);
    protected final TIntObjectHashMap<BlockInfo> attentionWhores = new TIntObjectHashMap<>(16);
    protected final TIntObjectHashMap<IStructureTileEntity> tileEntityMap = new TIntObjectHashMap<>(32);
    protected final List<Pair<Entity, Consumer<Entity>>> entityList = new ArrayList<>(8);

    public StructureWorld(World world, int radX, int sizeY, int radZ){
        this.world = world;
        this.radX = radX;
        this.radZ = radZ;
        this.sizeX = radX*2+1;
        this.sizeY = sizeY;
        this.sizeZ = radZ*2+1;
        this.blocks = new Block[sizeX*sizeY*sizeZ];
        this.metadata = new byte[sizeX*sizeY*sizeZ];
    }

    public StructureWorld(int radX, int sizeY, int radZ){
        this(null, radX, sizeY, radZ);
    }

    public final World getParentWorld(){
        return world;
    }

    public final BoundingBox getArea(){
        return new BoundingBox(Pos.at(-radX, 0, -radZ), Pos.at(radX, sizeY, radZ));
    }

    public final boolean isInside(int x, int y, int z){
        x += radX;
        z += radZ;
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
    }

    // Internal methods

    protected final int toIndex(int x, int y, int z){
        return y+sizeY*(x+radX)+sizeY*sizeX*(z+radZ);
    }

    protected final void toPos(int index, Pos.PosMutable pos){
        pos.setZ(MathUtil.floor((double)index/(sizeY*sizeX)));
        pos.setX(MathUtil.floor((double)(index-(pos.getZ()*sizeY*sizeX))/sizeY));
        pos.setY(index-(sizeY*sizeX*pos.getZ())-(sizeY*pos.getX()));
        pos.x -= radX;
        pos.z -= radZ;
    }

    // Setting block information

    public final boolean setBlock(int x, int y, int z, Block block){
        return setBlock(x, y, z, block, 0);
    }

    public boolean setBlock(int x, int y, int z, Block block, int metadata){
        if (!isInside(x, y, z))return false;

        int index = toIndex(x, y, z);
        this.blocks[index] = block;
        this.metadata[index] = (byte)metadata;
        return true;
    }

    public boolean setBlock(int x, int y, int z, Block block, int metadata, boolean scheduleUpdate){
        if (setBlock(x, y, z, block, metadata)){
            if (scheduleUpdate)this.scheduledUpdates.add(toIndex(x, y, z));
            return true;
        }
        else return false;
    }

    /*public final boolean setBlock(int x, int y, int z, BlockInfo blockInfo){
        return setBlock(x, y, z, blockInfo.block, blockInfo.meta);
    }*/

    public final boolean setAir(int x, int y, int z){
        return setBlock(x, y, z, Blocks.AIR, 0);
    }

    public void setAttentionWhore(int x, int y, int z, @Nullable BlockInfo info){
        if (isInside(x, y, z)){
            if (info == null)attentionWhores.remove(toIndex(x, y, z));
            else attentionWhores.put(toIndex(x, y, z), info);
        }
    }

    public boolean setTileEntity(int x, int y, int z, IStructureTileEntity provider){
        if (!isInside(x, y, z))return false;

        tileEntityMap.put(toIndex(x, y, z), provider);
        return true;
    }

    public final void clearArea(){
        clearArea(null, 0);
    }

    public void clearArea(@Nullable Block block, int metadata){
        final int limit = this.blocks.length;

        for(int index = 0; index < limit; index++){
            this.blocks[index] = block;
            this.metadata[index] = (byte)metadata;
        }
    }

    // Getting block information

    public Block getBlock(int x, int y, int z){
        return isInside(x, y, z) ? MoreObjects.firstNonNull(this.blocks[toIndex(x, y, z)], Blocks.AIR) : Blocks.AIR;
    }

    public int getMetadata(int x, int y, int z){
        return isInside(x, y, z) ? this.metadata[toIndex(x, y, z)] : 0;
    }

    public boolean isAir(int x, int y, int z){
        return !isInside(x, y, z) || MoreObjects.firstNonNull(this.blocks[toIndex(x, y, z)], Blocks.AIR) == Blocks.AIR;
    }

    public final int getTopY(int x, int z){
        return getTopY(x, z, sizeY-1);
    }

    public int getTopY(int x, int z, int startY){
        int y = startY;
        while(isAir(x, y, z) && --y >= 0);
        return y;
    }

    public final int getTopY(int x, int z, Block block){
        return getTopY(x, z, block, sizeY-1);
    }

    public int getTopY(int x, int z, Block block, int startY){
        int y = startY;
        while(getBlock(x, y, z) != block && --y >= 0);
        return y;
    }

    // Pos utility methods

    public final boolean setBlock(Pos pos, Block block){
        return setBlock(pos.getX(), pos.getY(), pos.getZ(), block, 0);
    }

    public final boolean setBlock(Pos pos, Block block, int metadata){
        return setBlock(pos.getX(), pos.getY(), pos.getZ(), block, metadata);
    }

    /*public final boolean setBlock(Pos pos, BlockInfo blockInfo){
        return setBlock(pos.getX(), pos.getY(), pos.getZ(), blockInfo.block, blockInfo.meta);
    }*/

    public final boolean setAir(Pos pos){
        return setAir(pos.getX(), pos.getY(), pos.getZ());
    }

    public final boolean setTileEntity(Pos pos, IStructureTileEntity provider){
        return setTileEntity(pos.getX(), pos.getY(), pos.getZ(), provider);
    }

    public final Block getBlock(Pos pos){
        return getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    public final int getMetadata(Pos pos){
        return getMetadata(pos.getX(), pos.getY(), pos.getZ());
    }

    public final boolean isAir(Pos pos){
        return isAir(pos.getX(), pos.getY(), pos.getZ());
    }

    // Entities

    public void addEntity(Entity entity){
        entityList.add(new Pair(entity, null));
    }

    public void addEntity(Entity entity, Consumer<Entity> callback){
        entityList.add(new Pair(entity, callback));
    }

    public <T extends Entity> Stream<T> getEntities(final Class<T> exactClassToMatch){
        return (Stream<T>)entityList.stream().filter(info -> info.getKey().getClass() == exactClassToMatch).map(info -> info.getKey());
    }

    // Generating

    public void generateInWorld(World world, Random rand, int centerX, int bottomY, int centerZ){
        generateBlocksInWorld(world, rand, centerX, bottomY, centerZ);

        Pos.PosMutable pos = new Pos.PosMutable();

        attentionWhores.forEachEntry((ind, value) -> {
            toPos(ind, pos);
            pos.move(centerX, bottomY, centerZ).setBlock(world, value.getState());
            return true;
        });

        tileEntityMap.forEachEntry((ind, value) -> {
            toPos(ind, pos);
            pos.move(centerX, bottomY, centerZ);

            TileEntity tile = world.getTileEntity(new BlockPos(pos.x,pos.y,pos.z));

            if (tile != null)value.generateTile(tile, rand);
            //else Log.reportedError("TileEntity is null at $0 - $1.", pos, pos.getBlock(world));

            return true;
        });

        scheduledUpdates.forEach(ind -> {
            toPos(ind, pos);
            world.scheduleBlockUpdate(new BlockPos(centerX+pos.getX(), bottomY+pos.getY(), centerZ+pos.getZ()),getBlock(pos),0,1);
            return true;
        });

        entityList.forEach(info -> {
            Entity entity = info.getKey();

            entity.setPosition(centerX+entity.posX, bottomY+entity.posY, centerZ+entity.posZ);
            entity.setWorld(world);
            world.spawnEntity(entity);

            if (info.getValue() != null)info.getValue().accept(entity);
        });
    }

    protected void generateBlocksInWorld(World world, Random rand, int centerX, int bottomY, int centerZ){
        Pos.PosMutable pos = new Pos.PosMutable();
        int x, y, z, index = -1;

        for(z = -radZ; z <= radZ; z++){
            for(x = -radX; x <= radX; x++){
                for(y = 0; y < sizeY; y++){
                    if (blocks[++index] != null)pos.set(centerX+x, bottomY+y, centerZ+z).setBlock(world, blocks[index].getDefaultState());
                }
            }
        }
    }
}
