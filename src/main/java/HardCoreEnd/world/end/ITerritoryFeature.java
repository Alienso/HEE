package HardCoreEnd.world.end;

import java.util.Random;

public interface ITerritoryFeature{
    boolean generate(EndTerritory territory, StructureWorld world, Random rand);
}
