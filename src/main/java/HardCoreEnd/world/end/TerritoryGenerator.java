package HardCoreEnd.world.end;


import java.util.EnumSet;
import java.util.Random;

public abstract class TerritoryGenerator{
    protected final EndTerritory territory;
    protected final EnumSet<?> variations;

    protected final StructureWorld world;
    protected final Random rand;
    protected final int size, height;

    public TerritoryGenerator(EndTerritory territory, EnumSet variations, StructureWorld world, Random rand){
        this.territory = territory;
        this.variations = variations;
        this.world = world;
        this.rand = rand;
        this.size = (int)world.getArea().x2;
        this.height = (int)world.getArea().y2;
    }

    public abstract void generate();

    @FunctionalInterface
    static interface ITerritoryGeneratorConstructor{
        TerritoryGenerator construct(EndTerritory territory, EnumSet variations, StructureWorld world, Random rand);
    }
}
