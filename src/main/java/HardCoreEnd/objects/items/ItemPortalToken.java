package HardCoreEnd.objects.items;

import HardCoreEnd.Main;
import HardCoreEnd.collections.BitStream;
import HardCoreEnd.collections.EmptyEnumSet;
import HardCoreEnd.init.ItemInit;
import HardCoreEnd.random.NBTCompound;
import HardCoreEnd.random.Pos;
import HardCoreEnd.save.SaveData;
import HardCoreEnd.save.WorldFile;
import HardCoreEnd.util.CollectionUtil;
import HardCoreEnd.util.IHasModel;
import HardCoreEnd.world.end.EndTerritory;
import HardCoreEnd.random.NBT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import javax.annotation.Nullable;

public class ItemPortalToken extends Item implements IHasModel {

    public ItemPortalToken(String name){
        setUnlocalizedName(name);
        setRegistryName(name);
        setCreativeTab(CreativeTabs.MISC);

        ItemInit.ITEMS.add(this);

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        ItemStack is = player.getHeldItem(hand);
        is.setCount(is.getCount()-1);

        if (world.isRemote)
            return new ActionResult<>(EnumActionResult.PASS, is);;
        if (world.provider.getDimension() == 1) {
            player.sendMessage(new TextComponentString("Rebuilding End... Don't do anything stupid."));
            refreshChunksBAD2(world);
            generatePortal(world,false);
            player.sendMessage(new TextComponentString("Done!"));
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, is);
    }

    public static void refreshChunksBAD2(World world) {
        try {
            List<ChunkPos> toUnload = new ArrayList<>();

            for (int i=-16;i<=16;i++)
                for (int j=-16;j<16;j++)
                    toUnload.add(new ChunkPos(i,j));

            for (ChunkPos pair : toUnload) {
                Chunk oldChunk = world.getChunkFromChunkCoords(pair.x, pair.z);
                WorldServer worldServer = (WorldServer) world;
                ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();
                IChunkGenerator generator = chunkProviderServer.chunkGenerator;
                Chunk newChunk = generator.generateChunk(oldChunk.x, oldChunk.z);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < world.getHeight(); y++) {
                            BlockPos pos = new BlockPos(x + oldChunk.x*16,y,z+oldChunk.z*16);
                            IBlockState state = newChunk.getBlockState(x,y,z);
                            worldServer.setBlockState(pos,state);
                            TileEntity tileEntity = newChunk.getTileEntity(new BlockPos(x, y, z),Chunk.EnumCreateEntityType.QUEUED);
                            if (tileEntity != null) {
                                worldServer.setTileEntity(pos, tileEntity);
                            }
                        }
                    }
                }
                generator.populate(oldChunk.x, oldChunk.z);
                //oldChunk.setTerrainPopulated(false);
                //newChunk.populate(chunkProviderGenerate,generator);
                //chunkProviderGenerate.populate(chunkProviderGenerate, oldChunk.xPosition, oldChunk.zPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generatePortal(World world,boolean active)
    {
        WorldGenEndPodium worldgenendpodium = new WorldGenEndPodium(active);
        BlockPos exitPortalLocation;
        for (exitPortalLocation = world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION).down(); world.getBlockState(exitPortalLocation).getBlock() == Blocks.BEDROCK && exitPortalLocation.getY() > world.getSeaLevel(); exitPortalLocation = exitPortalLocation.down()){}

        worldgenendpodium.generate(world, new Random(), exitPortalLocation);
    }

    @Override
    public void registerModels(){
        Main.proxy.registerItemRenderer(this,0,"inventory");
    }


}
