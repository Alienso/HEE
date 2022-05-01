package HardCoreEnd.init;

import HardCoreEnd.objects.items.CustomMusicDisc;
import HardCoreEnd.objects.items.ItemBase;
import HardCoreEnd.objects.items.ItemPortalToken;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemInit {
    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Item PortalToken = new ItemPortalToken("item_portal_token");

    public static final Item DragonAngryDisc = new CustomMusicDisc("music_dragon_angry", SoundsHandler.MUSIC_DRAGON_ANGRY);
    public static final Item DragonCalmDisc = new CustomMusicDisc("music_dragon_calm", SoundsHandler.MUSIC_DRAGON_CALM);
    public static final Item EndDisc = new CustomMusicDisc("game_end",SoundsHandler.MUSIC_END);
}
