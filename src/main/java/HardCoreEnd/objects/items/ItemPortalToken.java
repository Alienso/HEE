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
        ItemStack is = player.getActiveItemStack();

        /*WorldServer worldServer = (WorldServer) world;
        ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();
        IChunkGenerator generator = chunkProviderServer.chunkGenerator;
        generator.generateChunk(0,0);*/
        if (world.isRemote)
            return new ActionResult<ItemStack>(EnumActionResult.PASS,is);;
        if (world.provider.getDimension() == 1) {
            refreshChunksBAD2(world);
            generatePortal(world,false);
            //((WorldProviderEnd)world.provider).getDragonFightManager().respawnDragon();
        }
        /*if (!world.isRemote && player.capabilities.isCreativeMode && getTerritory(is) != null){
            generateTerritory(is,world);
            NBT.item(is, true).setInt("variations", getTerritory(is).properties.generateVariationsSerialized(world.rand, isRare(is)));
        }*/

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS,is);
    }

    public static void refreshChunksBAD2(World world) {
        try {
            ChunkProviderServer chunkServer = (ChunkProviderServer) world.getChunkProvider();
            List<ChunkPos> toUnload = new ArrayList<>();

            for (int i=-16;i<=16;i++)
                for (int j=-16;j<16;j++)
                    toUnload.add(new ChunkPos(i,j));

            int i = toUnload.size();
            for (ChunkPos pair : toUnload) {
                i--;
                Chunk oldChunk = world.getChunkFromChunkCoords(pair.x, pair.z);
                WorldServer worldServer = (WorldServer) world;
                ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();
                IChunkProvider chunkProviderGenerate = chunkProviderServer.world.getChunkProvider();
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

    public static final ItemStack forTerritory(EndTerritory territory, boolean isRare){
        ItemStack is = new ItemStack(ItemInit.PortalToken, 1, isRare ? 1 : 0);
        NBT.item(is, true).setByte("territory", (byte)territory.ordinal());
        return is;
    }

    public static final ItemStack forTerritory(EndTerritory territory, boolean isRare, Random rand){
        ItemStack is = forTerritory(territory, isRare);
        NBT.item(is, true).setInt("variations", territory.properties.generateVariationsSerialized(rand, isRare));
        return is;
    }

    public static final @Nullable EndTerritory getTerritory(ItemStack is){
        return CollectionUtil.get(EndTerritory.values, NBT.item(is, false).getByte("territory")).orElse(null);
    }

    public static final boolean isRare(ItemStack is){
        return is.getItemDamage() == 1;
    }

    public static final boolean isExpired(ItemStack is){
        return is.getItemDamage() == 2;
    }

    public static final EnumSet<? extends Enum<?>> getVariations(ItemStack is){
        EndTerritory territory = getTerritory(is);
        if (territory == null)return EmptyEnumSet.get();

        return territory.properties.deserialize(NBT.item(is, false).getInt("variations"));
    }

    public static final Optional<Pos> generateTerritory(ItemStack is, World world){
        WorldFile file = SaveData.global(WorldFile.class);

        NBTCompound tag = NBT.item(is, true);
        //tmp if (tag.hasKey("thash"))return Optional.of(file.getTerritoryPos(tag.getLong("thash")));

        EndTerritory territory = getTerritory(is);
        if (territory == null)return Optional.empty();

        final int index = file.increment(territory);
        final long hash = territory.getHashFromIndex(index);

        final EnumSet<? extends Enum<?>> variations = getVariations(is);
        final Pos spawnPos = territory.generateTerritory(index, world, territory.createRandom(world.getSeed(), index), variations, isRare(is));

        if (isRare(is)){
            file.setTerritoryRare(hash);
            is.setItemDamage(2);
        }

        file.setTerritoryPos(hash, spawnPos);
        file.setTerritoryVariations(hash, variations);

        tag.setLong("thash", hash);
        return Optional.of(spawnPos);
    }

    /*@Override
    public String getUnlocalizedName(ItemStack is){
        return isRare(is) ? getUnlocalizedName()+".rare" : getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World worldIn, List<String> textLines, ITooltipFlag flagIn){
        final int territory = NBT.item(is, false).getByte("territory");
        textLines.add(I18n.format("territory."+territory));

        final int variations = NBT.item(is, false).getInt("variations");

        if (variations != 0){
            BitStream.forInt(variations).forEach(ordinal -> {
                textLines.add(I18n.format("territory."+territory+".variation."+ordinal));
            });
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
        for(EndTerritory territory:EndTerritory.values){
            if (territory.canGenerate()){
                list.add(forTerritory(territory, false));
                list.add(forTerritory(territory, true));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses(){
        return true;
    }
*/
    @Override
    public void registerModels(){
        Main.proxy.registerItemRenderer(this,0,"inventory");
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int damage){
        return damage == 1 ? 3 : 2;
    }*/

}
