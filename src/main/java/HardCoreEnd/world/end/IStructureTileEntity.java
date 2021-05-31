package HardCoreEnd.world.end;

import net.minecraft.tileentity.TileEntity;

import java.util.Random;

@FunctionalInterface
public interface IStructureTileEntity{
    void generateTile(TileEntity tile, Random rand);
}
