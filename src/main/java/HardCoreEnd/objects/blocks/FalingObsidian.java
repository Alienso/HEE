package HardCoreEnd.objects.blocks;

import HardCoreEnd.Main;
import HardCoreEnd.init.BlockInit;
import HardCoreEnd.init.ItemInit;
import HardCoreEnd.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;

public class FalingObsidian extends BlockObsidian implements IHasModel {

    public FalingObsidian(){
        setUnlocalizedName("obsidian_end");
        setRegistryName("obsidian_end");
        setHardness(50F);
        setResistance(2000F);
        setSoundType(SoundType.STONE);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHarvestLevel("pickaxe", 3);

       BlockInit.BLOCKS.add(this);
    }

    @Override
    public void registerModels() {
        Main.proxy.registerBlockRenderer(this,0,"inventory");
    }
}
