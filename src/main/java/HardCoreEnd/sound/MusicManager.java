package HardCoreEnd.sound;

import java.util.HashSet;
import java.util.Set;

import HardCoreEnd.util.GameRegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;

import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jline.utils.Log;


public final class MusicManager{
    public static boolean enableCustomMusic = true;
    public static boolean removeVanillaDelay = false;

    private static final Set<String> ignoredTickers = new HashSet<>(1); // TODO add vazkii.ambience.NilMusicTicker

    public static boolean addIgnoredTicker(String fullClassName){
        return ignoredTickers.add(fullClassName);
    }

    public static boolean isMusicAvailable(ResourceLocation resource){
        SoundEventAccessor sound = Minecraft.getMinecraft().getSoundHandler().getAccessor(resource);
        return sound != null && sound.cloneEntry() != SoundHandler.MISSING_SOUND; // OBFUSCATED getSoundEntry
    }

    public static void register(){
        GameRegistryUtil.registerEventHandler(new MusicManager());
    }

    private boolean hasLoaded;

    private MusicManager(){}

    /*@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSoundLoad(SoundLoadEvent e){
        if (hasLoaded || (!enableCustomMusic && !removeVanillaDelay))return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.getMusicTicker() != null){
            Class<? extends MusicTicker> tickerClass = mc.getMusicTicker().getClass();

            if (ignoredTickers.contains(tickerClass.getName())){
                Log.warn("Another mod has already replaced the music system: $0", tickerClass.getName());
            }
            else if (tickerClass == MusicTicker.class){
                //mc.getMusicTicker() = new CustomMusicTicker(mc, null);
                Log.info("Successfully replaced music system.");
            }
            else{
                //mc.mcMusicTicker = new CustomMusicTicker(mc, mc.getMusicTicker());
                Log.info("Successfully wrapped a music system replaced by another mod: $0", tickerClass.getName());
            }

            hasLoaded = true;
        }
    }*/

    public static void switchMusic(SoundEvent e){
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().stopSounds();
        //mc.getMusicTicker().playMusic(MusicTicker.MusicType.MENU);
        mc.player.playSound(e,10000,1);
    }

    /*@Mod.EventBusSubscriber(modid = Atmospheric.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    @Mod("atmospheric")
    public class Atmospheric {
        public static final String MOD_ID = "atmospheric";

        Minecraft minecraft = Minecraft.getInstance();
        private static int musicTimer = 10;

        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.CLIENT) {
                if (musicTimer-- <= 0 && Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                    musicTimer = 10;
                    Minecraft.getInstance().player.playSound(AtmosphericSoundEvents.SOLACE_1, 2f, 1f);
                    Minecraft.getInstance().getMusicTicker().stop();
                }
            }
        }
    }*/
}
