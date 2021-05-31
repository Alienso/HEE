package HardCoreEnd.sound;

import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public enum EndMusicType{
    EXPLORATION("music.game.end", 3600, 8400),
    DRAGON_CALM("music.game.end.dragoncalm"),
    DRAGON_ANGRY("music.game.end.dragonangry");

    // TODO public final boolean isBossMusic;
    private final MusicTicker.MusicType type;

    EndMusicType(String resourceName){
        this(resourceName, 0, 0);
    }

    EndMusicType(String resourceName, int minDelay, int maxDelay){
        this.type = createMusicType(this, new ResourceLocation("hardcoreenderexpansion", resourceName), minDelay, maxDelay);
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
        //return PositionedSoundRecord.func_147673_a(type.getMusicLocation());
        return PositionedSoundRecord.getMusicRecord(type.getMusicLocation());
    }

    private static final MusicTicker.MusicType createMusicType(EndMusicType parent, ResourceLocation resource, int minDelay, int maxDelay){
        return EnumHelper.addEnum(MusicTicker.MusicType.class, "HEE_"+parent.name(), new Class[]{ ResourceLocation.class, int.class, int.class }, new Object[]{ resource, minDelay, maxDelay });
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
