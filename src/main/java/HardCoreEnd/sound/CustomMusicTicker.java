package HardCoreEnd.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public final class CustomMusicTicker extends MusicTicker {
    private static CustomMusicTicker instance;

    public static boolean canPlayMusic(){
        return instance != null;
    }

    public static void stopMusicAndPlayJukebox(ISound music){
        if (instance.currentMusic != null){
            instance.mc.getSoundHandler().stopSound(instance.currentMusic);
        }

        instance.currentMusic = music;
        instance.mc.getSoundHandler().playSound(music);
        instance.jukeboxDelay = 160;
        instance.playingCustomJukebox = true;
    }

    private final Minecraft mc;
    private final MusicTicker wrappedTicker;
    private final Random rand;

    private ISound currentMusic;
    private int waitAfterNewSong = -1;
    private int vanillaMusicTimer = 100;
    private int endMusicTimer;
    private EndMusicType playingEndMusicType = null;

    private boolean playingCustomJukebox;
    private int jukeboxDelay;

    public CustomMusicTicker(Minecraft mc, MusicTicker wrappedTicker){
        super(mc);

        if (instance != null)throw new IllegalStateException("CustomMusicTicker can only be created once!");
        instance = this;

        this.mc = mc;
        this.wrappedTicker = wrappedTicker;
        this.rand = new Random();

        //endMusicTimer = EndMusicType.EXPLORATION.getTimer(rand);
    }

    @Override
    public void update(){
        if (playingCustomJukebox)updateCustomJukeboxMusic();
        else if (jukeboxDelay > 0)--jukeboxDelay;
        else if (mc.world != null && mc.world.provider.getDimension() == 1 && MusicManager.enableCustomMusic)updateEndMusic();
        else{
            if (wrappedTicker != null)wrappedTicker.update();
            else updateVanillaMusic();
        }
    }

    private void updateCustomJukeboxMusic(){
        boolean isTooFar = mc.player != null && mc.player.getDistance(currentMusic.getXPosF(), currentMusic.getYPosF(), currentMusic.getZPosF()) > 64D;

        if (currentMusic == null || !mc.getSoundHandler().isSoundPlaying(currentMusic) || (isTooFar && --vanillaMusicTimer < 0) || mc.player == null){
            if (mc.getSoundHandler().isSoundPlaying(currentMusic))mc.getSoundHandler().stopSound(currentMusic);
            currentMusic = null;
            playingCustomJukebox = false;
            vanillaMusicTimer = Math.min(randomTimer(mc.getAmbientMusicType()), vanillaMusicTimer);
        }
        else if (!isTooFar)vanillaMusicTimer = 100;

        resetEndMusic(); // TODO move
        // TODO reset jukebox timer
    }

    private void updateEndMusic(){
        EndMusicType type = EndMusicType.validateAndGetMusicType();

        if (playingEndMusicType == null || type.getPriority() > playingEndMusicType.getPriority()){
            if (currentMusic != null)mc.getSoundHandler().stopSound(currentMusic);
            mc.getSoundHandler().playSound(currentMusic = type.getPositionedSoundRecord());
            playingEndMusicType = type;
            waitAfterNewSong = 100;
            endMusicTimer = EndMusicType.EXPLORATION.getTimer(rand);
        }

        if (currentMusic != null){
            if (!mc.getSoundHandler().isSoundPlaying(currentMusic) && (waitAfterNewSong < 0 || --waitAfterNewSong < 0)){
                currentMusic = null;
                playingEndMusicType = null;
            }
        }
        else if (endMusicTimer == 0 || --endMusicTimer == 0){
            mc.getSoundHandler().playSound(currentMusic = type.getPositionedSoundRecord());
            playingEndMusicType = type;
            waitAfterNewSong = 100;
            endMusicTimer = type.getTimer(rand);
        }
    }

    private void updateVanillaMusic(){
        MusicType type = mc.getAmbientMusicType();

        if (currentMusic != null){
            if (!type.getMusicLocation().getSoundName().equals(currentMusic.getSoundLocation())){ //TODO added getSoundName to fix equals may break something
                mc.getSoundHandler().stopSound(currentMusic);
                currentMusic = null;
                vanillaMusicTimer = MusicManager.removeVanillaDelay ? getShortDelay() : (rand.nextInt(type.getMinDelay()/8) + type.getMinDelay()/8);
            }

            if (!mc.getSoundHandler().isSoundPlaying(currentMusic) && (waitAfterNewSong < 0 || --waitAfterNewSong < 0)){
                currentMusic = null;
                vanillaMusicTimer = MusicManager.removeVanillaDelay ? getShortDelay() : Math.min(randomTimer(type), vanillaMusicTimer);
            }
        }

        if (currentMusic == null && vanillaMusicTimer-- <= 0){
            mc.getSoundHandler().playSound(currentMusic = PositionedSoundRecord.getMusicRecord(type.getMusicLocation()));
            waitAfterNewSong = 100;
            vanillaMusicTimer = Integer.MAX_VALUE;
        }

        resetEndMusic();
    }

    public void resetEndMusic(){
        endMusicTimer = 3600;
        playingEndMusicType = null;
    }

    private int randomTimer(MusicType musicType){
        int min = musicType.getMinDelay(), max = musicType.getMaxDelay();
        return min >= max ? max : rand.nextInt(max-min+1)+min;
    }

    private int getShortDelay(){
        return 15+rand.nextInt(8)*5;
    }
}
