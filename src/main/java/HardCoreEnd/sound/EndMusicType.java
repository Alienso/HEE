package HardCoreEnd.sound;

import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.client.audio.*;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public enum EndMusicType{
    EXPLORATION(SoundsHandler.MUSIC_END, 3600, 8400),
    DRAGON_CALM(SoundsHandler.MUSIC_DRAGON_CALM),
    DRAGON_ANGRY(SoundsHandler.MUSIC_DRAGON_ANGRY);

    // TODO public final boolean isBossMusic;
    private final MusicTicker.MusicType type;

    EndMusicType(SoundEvent e){
        this(e, 0, 0);
    }

    EndMusicType(SoundEvent soundEvent, int minDelay, int maxDelay){
        //SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(new ResourceLocation("hardcoreenderexpansion",location));
        //SoundEvent soundEvent = new SoundEvent(new ResourceLocation(location));
        this.type = createMusicType(this, soundEvent, minDelay, maxDelay);
    }

    public int getTimer(Random rand){
        //int min = type.func_148634_b(), max = type.func_148633_c();
        int min = type.getMinDelay(), max = type.getMaxDelay();
        return min >= max ? max : rand.nextInt(max-min+1)+min;
    }

    public int getPriority(){
        return -ordinal();
    }

    public PositionedSoundRecord getPositionedSoundRecord(){
        return PositionedSoundRecord.getMusicRecord(type.getMusicLocation());
    }

    private static MusicTicker.MusicType createMusicType(EndMusicType parent, SoundEvent soundEvent, int minDelay, int maxDelay){
        return EnumHelper.addEnum(MusicTicker.MusicType.class, "HEE_"+parent.name(), new Class[]{ SoundEvent.class, int.class, int.class }, new Object[]{ soundEvent, minDelay, maxDelay });
    }

    private static EndMusicType cachedType = null;
    private static long lastUpdateMillis;

    public static void update(EndMusicType type){
        cachedType = type;
        lastUpdateMillis = System.currentTimeMillis();
    }

    public static EndMusicType validateAndGetMusicType(){
        if (cachedType == null || (cachedType != EXPLORATION && System.currentTimeMillis()-lastUpdateMillis > 3000))cachedType = EXPLORATION;
        return cachedType;
    }
}
