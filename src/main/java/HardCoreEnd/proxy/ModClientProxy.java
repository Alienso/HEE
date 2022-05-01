package HardCoreEnd.proxy;


import HardCoreEnd.Main;
import HardCoreEnd.entity.EntityBlockFallingObsidian;
import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.EntityWeatherLightningBoltSafe;
import HardCoreEnd.entity.projectile.EntityProjectileDragonFireball;
import HardCoreEnd.random.Stopwatch;
import HardCoreEnd.render.RenderBossDragon;
import HardCoreEnd.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import org.lwjgl.opengl.Display;

import java.util.Calendar;
import java.util.Random;

public class ModClientProxy extends ModCommonProxy{
    public static final Random seedableRand = new Random();
    public static RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

    @Override
    public EntityPlayer getClientSidePlayer(){
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void registerRenderers(){
        Stopwatch.time("ModClientProxy - renderers");

        /*renderIdObsidianSpecial = RenderingRegistry.getNextAvailableRenderId();

        RenderingRegistry.registerBlockHandler(new RenderBlockObsidianSpecial());
        RenderingRegistry.registerBlockHandler(new RenderBlockEndFlowerPot());
        RenderingRegistry.registerBlockHandler(new RenderBlockSpookyLeaves());
        RenderingRegistry.registerBlockHandler(new RenderBlockCrossedDecoration());
        RenderingRegistry.registerBlockHandler(new RenderBlockRavishBell());
        RenderingRegistry.registerBlockHandler(new RenderBlockLootChest());
        RenderingRegistry.registerBlockHandler(new RenderBlockGloomtorch());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEssenceAltar.class, new RenderTileEssenceAltar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEndermanHead.class, new RenderTileEndermanHead());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCustomSpawner.class, new RenderTileCustomSpawner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserBeam.class, new RenderTileLaserBeam());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEndPortal.class, new RenderTileEndPortal());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVoidPortal.class, new RenderTileVoidPortal());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLootChest.class, new RenderTileLootChest());

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockList.loot_chest), new RenderItemLootChest());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BlockList.enderman_head), new RenderItemEndermanHead());*/

        //RenderingRegistry.registerEntityRenderingHandler(EntityBossDragon.class, new RenderBossDragon(renderManager));

       /* RenderingRegistry.registerEntityRenderingHandler(EntityMiniBossEnderEye.class, new RenderMiniBossEnderEye());
        RenderingRegistry.registerEntityRenderingHandler(EntityMiniBossFireFiend.class, new RenderMiniBossFireFiend());

        RenderingRegistry.registerEntityRenderingHandler(EntityMobEnderman.class, new RenderMobEnderman());
        RenderingRegistry.registerEntityRenderingHandler(EntityMobBabyEnderman.class, new RenderMobBabyEnderman());
        RenderingRegistry.registerEntityRenderingHandler(EntityMobEnderGuardian.class, new RenderTexturedMob(new ModelEnderGuardian(), 0.3F, "ender_guardian.png"));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobVampiricBat.class, new RenderTexturedMob(new ModelBat(), 0.25F, "bat_vampiric.png", 0.35F));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobInfestedBat.class, new RenderMobInfestedBat());
        RenderingRegistry.registerEntityRenderingHandler(EntityMobLouse.class, new RenderMobLouse());
        RenderingRegistry.registerEntityRenderingHandler(EntityMobFireGolem.class, new RenderTexturedMob(new ModelFireGolem(), 0.3F, "fire_golem.png"));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobScorchingLens.class, new RenderTexturedMob(new ModelScorchingLens(), 0.3F, "scorching_lens.png"));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobHauntedMiner.class, new RenderTexturedMob(new ModelHauntedMiner(), 0.5F, "haunted_miner.png", 1.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobEndermage.class, new RenderTexturedMob(new ModelEndermage(), 0.3F, "endermage.png"));
        RenderingRegistry.registerEntityRenderingHandler(EntityMobSilverfish.class, new RenderTexturedMob(new ModelSilverfish(), 0.3F, "minecraft", "silverfish.png"));*/

        //RenderingRegistry.registerEntityRenderingHandler(EntityBlockEnderCrystal.class, new RenderEnderCrystal());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlockFallingObsidian.class, new RenderFallingBlock(renderManager));
        /*RenderingRegistry.registerEntityRenderingHandler(EntityBlockFallingDragonEgg.class, new RenderFallingBlock());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlockEnhancedTNTPrimed.class, new RenderBlockEnhancedTNTPrimed());
        RenderingRegistry.registerEntityRenderingHandler(EntityBlockTokenHolder.class, new RenderBlockTokenHolder());*/

        /*RenderingRegistry.registerEntityRenderingHandler(EntityProjectileFlamingBall.class, new RenderNothing());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileMinerShot.class, new RenderNothing());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileGolemFireball.class, new RenderFireball(0.5F));*/
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileDragonFireball.class, new RenderFireball(renderManager,1F));
        /*RenderingRegistry.registerEntityRenderingHandler(EntityProjectilePotion.class, new RenderProjectilePotion());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileSpatialDash.class, new RenderNothing());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileCorruptedEnergy.class, new RenderNothing());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileFiendFireball.class, new RenderProjectileFiendFireball(0.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileCurse.class, new RenderProjectileCurse());
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileExpBottleConsistent.class, new RenderSnowball(ItemList.exp_bottle));
        RenderingRegistry.registerEntityRenderingHandler(EntityProjectileEyeOfEnder.class, new RenderProjectileEyeOfEnder());*/

        RenderingRegistry.registerEntityRenderingHandler(EntityWeatherLightningBoltSafe.class, new RenderLightningBolt(renderManager));

        //RenderingRegistry.registerEntityRenderingHandler(EntityTechnicalBase.class, new RenderNothing());

        Stopwatch.finish("ModClientProxy - renderers");

        //Baconizer.load();
    }

    @Override
    public void registerSidedEvents(){
        Stopwatch.time("ModClientProxy - events");


        //OverlayManager.register();
       // CompendiumEventsClient.register();
        // TODO CharmPouchHandlerClient.register();
        //MusicManager.register();
        //FXEvents.register();
        //HeeClientCommand.register();

        Stopwatch.finish("ModClientProxy - events");
    }

    @Override
    public void sendMessage(MessageType msgType, int...data){
        switch(msgType){
            case DEBUG_TITLE_SET:
                Display.setTitle(Display.getTitle()+" - HardcoreEnderExpansion - "+"dev"+' '+ Reference.VERSION);
                break;

            case SPEED_UP_PLAYER:
                Minecraft.getMinecraft().player.capabilities.setFlySpeed(0.3F);
                break;

            /*case VIEW_MOD_CONTENT:
                Minecraft.getMinecraft().displayGuiScreen(new GuiItemViewer());
                break;*/
        }
    }

    /**
     * Mode 0: enable on April Fools
     * Mode 1: always enabled
     * Mode 2: never enabled
     */
    public static void loadEnderbacon(int mode){
        if (mode == 1)hardcoreEnderbacon = true;
        else if (mode == 0){
            Calendar cal = Calendar.getInstance();
            hardcoreEnderbacon = cal.get(Calendar.MONTH) == 3 && cal.get(Calendar.DAY_OF_MONTH) == 1;
        }
    }
}
