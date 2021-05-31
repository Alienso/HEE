package HardCoreEnd.world.end;


import HardCoreEnd.random.NBTCompound;
import net.minecraft.world.World;

@FunctionalInterface
public interface ITerritoryBehavior{
    void tick(World world, NBTCompound nbt);
}
