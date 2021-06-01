package HardCoreEnd.objects.items;

import HardCoreEnd.collections.BitStream;
import HardCoreEnd.collections.EmptyEnumSet;
import HardCoreEnd.init.ItemInit;
import HardCoreEnd.random.NBTCompound;
import HardCoreEnd.random.Pos;
import HardCoreEnd.save.SaveData;
import HardCoreEnd.save.WorldFile;
import HardCoreEnd.util.CollectionUtil;
import HardCoreEnd.world.end.EndTerritory;
import HardCoreEnd.random.NBT;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;

public class ItemPortalToken extends Item {
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


    public ItemPortalToken(){
        String name = "ingot_token";
        setUnlocalizedName(name);
        setRegistryName(name);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.MISC);
        setHasSubtypes(true);

        ItemInit.ITEMS.add(this);
    }

    @Override
    public String getUnlocalizedName(ItemStack is){
        return isRare(is) ? getUnlocalizedName()+".rare" : getUnlocalizedName();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        ItemStack is = player.getActiveItemStack();
        if (!world.isRemote && player.capabilities.isCreativeMode && getTerritory(is) != null){
            generateTerritory(is,world);
            NBT.item(is, true).setInt("variations", getTerritory(is).properties.generateVariationsSerialized(world.rand, isRare(is)));
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS,is);
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

    /*@Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int damage){
        return damage == 1 ? 3 : 2;
    }*/

}
