package HardCoreEnd.util.handlers;

import HardCoreEnd.sound.MusicManager;
import HardCoreEnd.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundsHandler {

    public static SoundEvent MUSIC_DRAGON_ANGRY;
    public static SoundEvent MUSIC_DRAGON_CALM;
    public static SoundEvent MUSIC_END;

    public static void registerSounds(){
        MUSIC_DRAGON_ANGRY = registerSound("music.music_dragon_angry");
        MUSIC_DRAGON_CALM = registerSound("music.music_dragon_calm");
        MUSIC_END = registerSound("music.game_end");
    }

    private static SoundEvent registerSound(String name){
        ResourceLocation location = new ResourceLocation(Reference.MODID,name);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(name);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
